package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
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
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, BranchRepository branchRepository, ExcelExportService excelExportService, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.excelExportService = excelExportService;
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
            log.debug("Processing sheet '{}'. Last row number: {}", sheet.getSheetName(), sheet.getLastRowNum());

            for (Row row : sheet) {
                if (row.getRowNum() < 3) {
                    continue;
                }

                if (row.getCell(0) == null || getCellValueAsString(row.getCell(0), formatter).trim().isEmpty()) {
                    continue;
                }

                totalRecords++;
                try {
                    Map<String, String> rowData = new HashMap<>();

                    String surname = getCellValueAsString(row.getCell(0), formatter);
                    String name = getCellValueAsString(row.getCell(1), formatter);
                    String patronymic = getCellValueAsString(row.getCell(2), formatter);
                    String branchName = getCellValueAsString(row.getCell(3), formatter);
                    String roles = getCellValueAsString(row.getCell(4), formatter);

                    if (surname.isEmpty() || name.isEmpty()) {
                        throw new UserParsingException("Surname and Name cannot be empty.");
                    }

                    rowData.put("surname", surname);
                    rowData.put("name", name);
                    rowData.put("patronymic", patronymic);
                    rowData.put("branchName", branchName);
                    rowData.put("roles", roles);

                    fileData.add(rowData);
                    if (!branchName.isEmpty()) {
                        branchNamesFromFile.add(branchName);
                    }
                } catch (Exception e) {
                    recordsWithError++;
                    String errorMessage = "Error reading row " + (row.getRowNum() + 1) + ": " + e.getMessage();
                    errorMessages.add(errorMessage);
                    log.error(errorMessage, e);
                }
            }

            log.info("Finished reading file. Rows to process: {}. Unique branch names: {}", totalRecords, branchNamesFromFile.size());

            Map<String, Branch> branchesMap = branchRepository.findByNameIn(branchNamesFromFile)
                    .stream().collect(Collectors.toMap(Branch::getName, Function.identity()));
            log.debug("Found {} existing branches from the file list.", branchesMap.size());

            Map<String, User> existingUsersMap = userRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            u -> generateUserKey(u.getSurname(), u.getName(), u.getPatronymic()),
                            Function.identity(),
                            (existing, replacement) -> {
                                log.warn("Duplicate user found in DB for key: '{}'. Using the first one found (ID: {}).",
                                        generateUserKey(existing.getSurname(), existing.getName(), existing.getPatronymic()), existing.getId());
                                return existing;
                            }
                    ));
            log.debug("Loaded {} unique users from DB for comparison.", existingUsersMap.size());

            List<User> usersToSaveOrUpdate = new ArrayList<>();

            for(Map<String, String> rowData : fileData) {
                String userIdentifier = rowData.get("surname") + " " + rowData.get("name");
                try {
                    String key = generateUserKey(rowData.get("surname"), rowData.get("name"), rowData.get("patronymic"));
                    User existingUser = existingUsersMap.get(key);

                    Branch branch = null;
                    String branchName = rowData.get("branchName");
                    if (!branchName.isEmpty()) {
                        branch = branchesMap.get(branchName);
                        if (branch == null) {
                            throw new UserParsingException("Branch '" + branchName + "' not found.");
                        }
                    }

                    Set<Role> roles = parseRoles(rowData.get("roles"));

                    if (existingUser != null) {
                        if (!existingUsersMap.containsKey(key)) existingRecordsInDbFound++;

                        boolean isChanged = false;
                        if (!Objects.equals(existingUser.getBranch(), branch)) { isChanged = true; existingUser.setBranch(branch); }
                        if (!existingUser.getRoles().equals(roles)) { isChanged = true; existingUser.setRoles(roles); }

                        if(isChanged) {
                            usersToSaveOrUpdate.add(existingUser);
                            updatedRecords++;
                            log.debug("User '{}' marked for update.", key);
                        }
                    } else {
                        User newUser = new User(rowData.get("name"), rowData.get("surname"), rowData.get("patronymic"), roles, branch);
                        usersToSaveOrUpdate.add(newUser);
                        successfullyParsedNew++;
                        log.debug("New user '{}' prepared for creation.", key);
                    }
                } catch (Exception e) {
                    recordsWithError++;
                    errorMessages.add("Failed to process user '" + userIdentifier + "': " + e.getMessage());
                }
            }

            if (!usersToSaveOrUpdate.isEmpty()) {
                userRepository.saveAll(usersToSaveOrUpdate);
                log.info("{} users were saved or updated in the database.", usersToSaveOrUpdate.size());
            }

        } catch (IOException e) {
            log.error("Failed to read or process the Excel file: {}", e.getMessage(), e);
            throw e;
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
        log.info("Starting export of all users to Excel.");
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