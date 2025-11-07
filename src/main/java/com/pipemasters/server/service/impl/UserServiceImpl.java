package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.RegisterRequestDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.dto.request.create.UserCreateDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.exceptions.user.UserParsingException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.UserRepository;
import com.pipemasters.server.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ExcelExportService excelExportService;
    private final AuthenticationService authenticationService;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, BranchRepository branchRepository, ExcelExportService excelExportService, @Lazy AuthenticationService authenticationService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.excelExportService = excelExportService;
        this.authenticationService = authenticationService;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(value = {"users", "users_pages"}, allEntries = true)
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        return modelMapper.map(createAndReturnUser(dto), UserResponseDto.class);
    }

    @Override
    @CacheEvict(value = {"users", "users_pages"}, allEntries = true)
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (dto.getBranchId() != null) {
            Branch newBranch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));
            user.setBranch(newBranch);
        }

        modelMapper.map(dto, user);

         if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
             user.setRoles(dto.getRoles());
         }

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findByIdWithBranch(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Override
    @Cacheable("users")
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(u -> modelMapper.map(u, UserResponseDto.class)).toList();
    }

    @Override
    @Cacheable("users_pages")
    @Transactional(readOnly = true)
    public PageDto<UserResponseDto> getPaginatedUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponseDto> dtoList = userPage.getContent().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
        return new PageDto<>(dtoList, userPage.getNumber(), userPage.getSize(), userPage.getTotalElements());
    }

    @Override
    @CacheEvict(value = {"users", "users_pages"}, allEntries = true)
    @Transactional
    public UserResponseDto assignUserToBranch(Long userId, Long branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + branchId));

        user.setBranch(branch);
        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByBranchId(Long branchId) {
         if (!branchRepository.existsById(branchId)) {
             throw new BranchNotFoundException("Branch not found with id: " + branchId);
         }
        return userRepository.findByBranchId(branchId).stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
    }

    @Override
    @CacheEvict(value = {"users", "users_pages"}, allEntries = true)
    @Transactional
    public User createAndReturnUser(UserCreateDto dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + dto.getBranchId()));

        User user = new User();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setPatronymic(dto.getPatronymic());
        user.setBranch(branch);

        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            user.setRoles(Set.of(Role.USER));
        } else {
            user.setRoles(dto.getRoles());
        }
        return userRepository.save(user);
    }

    @Override
    @CacheEvict(value = {"users", "users_pages"}, allEntries = true)
    @Transactional
    public ParsingStatsDto parseUsersExcelFile(MultipartFile file) throws IOException {
        log.info("Starting Excel file parsing for users. File: {}", file.getOriginalFilename());

        List<String> errorMessages = new ArrayList<>();
        int totalRecords = 0;
        int successfullyParsedNew = 0;
        int recordsWithError = 0;
        int existingRecordsInDbFound = 0;
        int updatedRecords = 0;

        List<Map<String, String>> fileData = new ArrayList<>();
        Set<String> branchNamesFromFile = new HashSet<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 1 || row.getCell(0) == null || getCellValueAsString(row.getCell(0), formatter).trim().isEmpty()) {
                    continue;
                }
                totalRecords++;
                try {
                    Map<String, String> rowData = new HashMap<>();
                    rowData.put("surname", getCellValueAsString(row.getCell(0), formatter));
                    rowData.put("name", getCellValueAsString(row.getCell(1), formatter));
                    rowData.put("patronymic", getCellValueAsString(row.getCell(2), formatter));
                    String branchName = getCellValueAsString(row.getCell(3), formatter);
                    rowData.put("branchName", branchName);
                    rowData.put("roles", getCellValueAsString(row.getCell(4), formatter));
                    rowData.put("username", getCellValueAsString(row.getCell(5), formatter));
                    rowData.put("password", getCellValueAsString(row.getCell(6), formatter));

                    if (rowData.get("surname").isEmpty() || rowData.get("name").isEmpty()) {
                        throw new UserParsingException("Surname and Name cannot be empty.");
                    }
                    fileData.add(rowData);
                    if (!branchName.isEmpty()) branchNamesFromFile.add(branchName);
                } catch (Exception e) {
                    recordsWithError++;
                    errorMessages.add("Error reading row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }

        Map<String, Branch> branchesMap = branchRepository.findByNameIn(branchNamesFromFile)
                .stream().collect(Collectors.toMap(Branch::getName, Function.identity()));
        Map<String, User> existingUsersMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(u -> generateUserKey(u.getSurname(), u.getName(), u.getPatronymic()), Function.identity(), (e, r) -> e));
        existingRecordsInDbFound = existingUsersMap.size();

        List<User> usersToUpdate = new ArrayList<>();
        for (Map<String, String> rowData : fileData) {
            String userIdentifier = rowData.get("surname") + " " + rowData.get("name");
            try {
                String key = generateUserKey(rowData.get("surname"), rowData.get("name"), rowData.get("patronymic"));
                User existingUser = existingUsersMap.get(key);

                Branch branch = null;
                String branchName = rowData.get("branchName");
                if (!branchName.isEmpty()) {
                    branch = branchesMap.get(branchName);
                    if (branch == null) throw new UserParsingException("Branch '" + branchName + "' not found.");
                }

                Set<Role> roles = parseRoles(rowData.get("roles"));

                if (existingUser != null) {
                    boolean isChanged = false;
                    if (!Objects.equals(existingUser.getBranch(), branch)) {
                        isChanged = true;
                        existingUser.setBranch(branch);
                    }
                    if (!existingUser.getRoles().equals(roles)) {
                        isChanged = true;
                        existingUser.setRoles(roles);
                    }

                    if (isChanged) {
                        usersToUpdate.add(existingUser);
                        updatedRecords++;
                    }
                } else {
                    String username = rowData.get("username");
                    String password = rowData.get("password");

                    if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                        throw new UserParsingException("Username and Password are required for new user: " + userIdentifier);
                    }

                    RegisterRequestDto registerRequest = new RegisterRequestDto();
                    registerRequest.setName(rowData.get("name"));
                    registerRequest.setSurname(rowData.get("surname"));
                    registerRequest.setPatronymic(rowData.get("patronymic"));
                    registerRequest.setUsername(username);
                    registerRequest.setPassword(password);
                    registerRequest.setRoles(roles);
                    if (branch != null) {
                        registerRequest.setBranchId(branch.getId());
                    } else {
                        throw new UserParsingException("Branch is required for new user: " + userIdentifier);
                    }

                    authenticationService.registerFromImport(registerRequest);
                    successfullyParsedNew++;
                }
            } catch (Exception e) {
                recordsWithError++;
                errorMessages.add("Failed to process user '" + userIdentifier + "': " + e.getMessage());
            }
        }

        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
            log.info("{} users were updated in the database.", usersToUpdate.size());
        }

        return new ParsingStatsDto(totalRecords, successfullyParsedNew, recordsWithError, existingRecordsInDbFound, updatedRecords, errorMessages);
    }

    private Set<Role> parseRoles(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return Collections.singleton(Role.USER);
        }

        Set<Role> parsedRoles = new HashSet<>();
        String[] roleNames = rolesString.split(",");

        for (String roleName : roleNames) {
            try {
                parsedRoles.add(Role.valueOf(roleName.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new UserParsingException("Invalid role name found: '" + roleName.trim() + "'");
            }
        }
        return parsedRoles.isEmpty() ? Collections.singleton(Role.USER) : parsedRoles;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportUsersToExcel() throws IOException {
        List<User> users = userRepository.findAllWithBranch();
        log.debug("Found {} users to export.", users.size());

        return excelExportService.exportUsersToExcel(users);
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private String generateUserKey(String surname, String name, String patronymic) {
        return String.join(" ", surname, name, patronymic).toLowerCase();
    }
}