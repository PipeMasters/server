package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.branch.InvalidBranchHierarchyException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.service.BranchService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    public BranchServiceImpl(BranchRepository branchRepository, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public BranchDto createBranch(BranchDto branchDto) {
        Branch parent = null;
        if (branchDto.getParent() != null && branchDto.getParent().getId() != null) {
            parent = branchRepository.findById(branchDto.getParent().getId())
                    .orElseThrow(() -> new BranchNotFoundException("Parent branch not found with ID: " + branchDto.getParent().getId()));
        }

        Branch branch = new Branch(branchDto.getName(), parent);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public BranchDto updateBranchName(Long id, String newName) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + id));

        branch.setName(newName);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public BranchDto reassignParent(Long id, Long newParentId) {
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

    private BranchDto toDto(Branch entity) {
        BranchDto dto = modelMapper.map(entity, BranchDto.class);

        if (entity.getParent() != null) {  // отдельный маппинг для избежания рекурсии
            BranchDto parentDto = new BranchDto();
            parentDto.setId(entity.getParent().getId());
            parentDto.setName(entity.getParent().getName());
            dto.setParent(parentDto);
        }

        return dto;
    }
}