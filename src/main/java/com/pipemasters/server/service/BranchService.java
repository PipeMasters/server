package com.pipemasters.server.service;

import com.pipemasters.server.dto.BranchDto;

public interface BranchService {
    BranchDto createBranch(BranchDto branchDto);
    BranchDto updateBranchName(Long id, String newName);
    BranchDto reassignParent(Long id, Long newParentId);
}