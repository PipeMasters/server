package com.pipemasters.server.service;

import com.pipemasters.server.dto.BranchDto;

import java.util.List;

public interface BranchService {
    BranchDto createBranch(BranchDto branchDto);
    BranchDto updateBranchName(Long id, String newName);
    BranchDto reassignParent(Long id, Long newParentId);
    BranchDto getBranchById(Long id, boolean includeParent);
    BranchDto getBranchByName(String name, boolean includeParent);
    List<BranchDto> getAllBranches(boolean includeParent);
    List<BranchDto> getChildBranches(Long parentId, boolean includeParent);
}