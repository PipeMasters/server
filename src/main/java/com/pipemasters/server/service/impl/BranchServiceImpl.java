package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.service.BranchService;
import jakarta.persistence.EntityNotFoundException;
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
                    .orElseThrow(() -> new EntityNotFoundException("Parent branch not found"));
        }

        Branch branch = new Branch(branchDto.getName(), parent);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public BranchDto updateBranchName(Long id, String newName) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        branch.setName(newName);
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public BranchDto reassignParent(Long id, Long newParentId) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        Branch newParent = null;
        if (newParentId != null) {
            newParent = branchRepository.findById(newParentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
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