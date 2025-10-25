package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.branch.InvalidBranchHierarchyException;
import com.pipemasters.server.exceptions.branch.InvalidBranchLevelException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.service.BranchService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    public BranchServiceImpl(BranchRepository branchRepository, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
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