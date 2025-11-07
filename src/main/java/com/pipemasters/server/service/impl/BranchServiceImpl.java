package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.exceptions.branch.BranchHasChildrenException;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.branch.BranchParsingException;
import com.pipemasters.server.exceptions.branch.InvalidBranchHierarchyException;
import com.pipemasters.server.exceptions.branch.InvalidBranchLevelException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.service.BranchService;
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
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private final Logger log = LoggerFactory.getLogger(BranchServiceImpl.class);
    private final BranchRepository branchRepository;
    private final ExcelExportService excelExportService;
    private final ModelMapper modelMapper;

    public BranchServiceImpl(BranchRepository branchRepository, ExcelExportService excelExportService, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
        this.excelExportService = excelExportService;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(value = {"branches", "branches_parent","branches_child", "branches_level", "branches_pages"}, allEntries = true)
    @Transactional
    public BranchResponseDto createBranch(BranchRequestDto branchRequestDto) {
        Branch parent = null;
        if (branchRequestDto.getParentId() != null) {
            parent = branchRepository.findById(branchRequestDto.getParentId())
                    .orElseThrow(() -> new BranchNotFoundException("Parent branch not found with ID: " + branchRequestDto.getParentId()));
        }

        Branch branch = new Branch(branchRequestDto.getName(), parent);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    @CacheEvict(value = {"branches", "branches_parent","branches_child", "branches_level", "branches_pages"}, allEntries = true)
    @Transactional
    public BranchResponseDto updateBranchName(Long id, String newName) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));

        branch.setName(newName);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    @CacheEvict(value = {"branches", "branches_parent","branches_child", "branches_level", "branches_pages"}, allEntries = true)
    @Transactional
    public BranchResponseDto reassignParent(Long id, Long newParentId) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));

        Branch newParent = null;
        if (newParentId != null) {
            newParent = branchRepository.findById(newParentId)
                    .orElseThrow(() -> new BranchNotFoundException("New parent branch not found with ID: " + newParentId));
        }

        if (newParent != null && newParent.getId().equals(branch.getId())) {
            throw new InvalidBranchHierarchyException("A branch cannot be its own parent.");
        }

        branch.setParent(newParent);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponseDto getBranchById(Long id, boolean includeParent) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));
        return toDto(branch, includeParent);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponseDto getBranchByName(String name, boolean includeParent) {
        Branch branch = branchRepository.findByName(name)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with name: " + name));
        return toDto(branch, includeParent);
    }

    @Override
    @Cacheable("branches")
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getAllBranches(boolean includeParent) {
        List<Branch> branches = branchRepository.findAll();
        return branches.stream()
                .map(entity -> toDto(entity, includeParent))
                .toList();
    }

    @Override
    @Cacheable("branches_pages")
    @Transactional(readOnly = true)
    public PageDto<BranchResponseDto> getPaginatedBranches(boolean includeParent, Pageable pageable) {
        Page<Branch> page = branchRepository.findAll(pageable);

        List<BranchResponseDto> dtoList = page.getContent().stream()
                .map(entity -> toDto(entity, includeParent))
                .toList();

        return new PageDto<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Cacheable("branches_child")
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getChildBranches(Long parentId, boolean includeParent) {
        List<Branch> childBranches;
        if (!branchRepository.existsById(parentId)) {
            throw new BranchNotFoundException("Parent branch not found with ID: " + parentId);
        }

        childBranches = branchRepository.findByParentId(parentId);

        return childBranches.stream()
                .map(entity -> toDto(entity, includeParent))
                .toList();
    }

    @Override
    @Cacheable("branches_parent")
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getParentBranches() {
        List<Branch> rootBranches = branchRepository.findByParentIsNull();
        return rootBranches.stream()
                .map(this::toDto)
                .toList();
    }


    @Override
    @Cacheable("branches_level")
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getBranchesByLevel(int level) {
        if (level < 0) {
            throw new InvalidBranchLevelException("Branch level cannot be negative.");
        }
        List<Branch> branches = branchRepository.findByLevel(level);

        return branches.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @CacheEvict(value = {"branches", "branches_parent", "branches_child", "branches_level", "branches_pages"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        Branch branchToDelete = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));

        List<Branch> children = branchRepository.findByParentId(branchToDelete.getId());

        if (children != null && !children.isEmpty()) {
            throw new BranchHasChildrenException("Cannot delete branch with ID " + id + " because it has active children.");
        }

        branchToDelete.setDeleted(true);
        branchRepository.save(branchToDelete);
    }

    @Override
    @Transactional
    public ParsingStatsDto parseExcelFile(MultipartFile file) throws IOException {
        log.info("Starting Excel file parsing for branches. File: {}", file.getOriginalFilename());

        List<String> errorMessages = new ArrayList<>();
        int totalRecords = 0;
        int successfullyParsedNew = 0;
        int recordsWithError = 0;
        int existingRecordsInDbFound = 0;
        int updatedRecords = 0;

        Map<String, String> branchParentMap = new HashMap<>();
        Set<String> allBranchNamesFromFile = new HashSet<>();

        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            log.debug("Processing sheet '{}'. Last row number: {}", sheet.getSheetName(), sheet.getLastRowNum());

            for (Row row : sheet) {
                if (row.getRowNum() < 1) {
                    continue;
                }

                if (row.getCell(0) == null || getCellValueAsString(row.getCell(0), formatter).trim().isEmpty()) {
                    log.trace("Skipping empty or blank row (first cell is empty): {}", row.getRowNum() + 1);
                    continue;
                }

                totalRecords++;

                try {
                    String name = getCellValueAsString(row.getCell(0), formatter);
                    String parentName = getCellValueAsString(row.getCell(1), formatter);

                    if (name.isEmpty()) {
                        throw new BranchParsingException("Branch name cannot be empty.");
                    }

                    log.trace("Row {}: Read branch '{}' with parent '{}'", row.getRowNum() + 1, name, parentName);

                    allBranchNamesFromFile.add(name);
                    if (!parentName.isEmpty()) {
                        branchParentMap.put(name, parentName);
                        allBranchNamesFromFile.add(parentName);
                    }
                } catch (BranchParsingException e) {
                    recordsWithError++;
                    errorMessages.add("Error parsing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    log.warn("Parsing error in row {}: {}", row.getRowNum() + 1, e.getMessage());
                } catch (Exception e) {
                    recordsWithError++;
                    errorMessages.add("Unexpected error processing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    log.error("Unexpected error in row {}: {}", row.getRowNum() + 1, e.getMessage(), e);
                }
            }

            log.info("Finished reading file. Total rows to process: {}. Unique branch names found: {}", totalRecords, allBranchNamesFromFile.size());

            Map<String, Branch> existingBranchesMap = branchRepository.findByNameIn(allBranchNamesFromFile)
                    .stream()
                    .collect(Collectors.toMap(Branch::getName, Function.identity()));
            log.debug("Found {} existing branches in the database from the list of {} names.", existingBranchesMap.size(), allBranchNamesFromFile.size());
            existingRecordsInDbFound = existingBranchesMap.size();

            List<Branch> branchesToSave = new ArrayList<>();
            for (String branchName : allBranchNamesFromFile) {
                if (!existingBranchesMap.containsKey(branchName)) {
                    Branch newBranch = new Branch(branchName, null);
                    branchesToSave.add(newBranch);
                    existingBranchesMap.put(branchName, newBranch);
                    log.debug("New branch '{}' prepared for creation.", branchName);
                }
            }

            if (!branchesToSave.isEmpty()) {
                branchRepository.saveAll(branchesToSave);
                successfullyParsedNew = branchesToSave.size();
                log.info("{} new branches have been saved to the database.", successfullyParsedNew);
            }

            List<Branch> branchesToUpdate = new ArrayList<>();
            for (Map.Entry<String, String> entry : branchParentMap.entrySet()) {
                String childName = entry.getKey();
                String parentName = entry.getValue();

                Branch childBranch = existingBranchesMap.get(childName);
                Branch parentBranch = existingBranchesMap.get(parentName);

                if (childBranch == null) {
                    log.warn("Child branch '{}' not found in the map while setting parent. Skipping.", childName);
                    continue;
                }

                if (parentBranch == null) {
                    recordsWithError++;
                    errorMessages.add("Parent branch '" + parentName + "' for child '" + childName + "' not found.");
                    log.warn("Parent branch '{}' for child '{}' not found. Link cannot be established.", parentName, childName);
                    continue;
                }

                boolean needsUpdate = childBranch.getParent() == null || !childBranch.getParent().getId().equals(parentBranch.getId());

                if (needsUpdate) {
                    childBranch.setParent(parentBranch);
                    branchesToUpdate.add(childBranch);
                    updatedRecords++;
                    log.debug("Updating parent for branch '{}' to '{}'.", childName, parentName);
                }
            }

            if (!branchesToUpdate.isEmpty()) {
                branchRepository.saveAll(branchesToUpdate);
                log.info("{} branches have been updated with new parent information.", updatedRecords);
            }

        } catch (IOException e) {
            log.error("Failed to read or process the Excel file: {}", e.getMessage(), e);
            throw e;
        }

        ParsingStatsDto stats = new ParsingStatsDto(
                totalRecords,
                successfullyParsedNew,
                recordsWithError,
                existingRecordsInDbFound,
                updatedRecords,
                errorMessages
        );
        log.info("Excel parsing for branches finished. Stats: {}", stats);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream exportBranchesToExcel() throws IOException {
        List<Branch> branches = branchRepository.findAllByOrderByNameAsc();
        log.debug("Found {} branches to export.", branches.size());
        ByteArrayOutputStream outputStream = excelExportService.exportBranchesToExcel(branches);
        return outputStream;
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }


    private BranchResponseDto toDto(Branch entity, boolean includeParent) {
        BranchResponseDto dto = modelMapper.map(entity, BranchResponseDto.class);

        if (includeParent && entity.getParent() != null) {
            BranchResponseDto parentDto = new BranchResponseDto();
            parentDto.setId(entity.getParent().getId());
            parentDto.setName(entity.getParent().getName());
            dto.setParentId(parentDto.getId());
        } else {
            dto.setParentId(null);
        }

        return dto;
    }

    private BranchResponseDto toDto(Branch entity) {
        return toDto(entity, false);
    }
}