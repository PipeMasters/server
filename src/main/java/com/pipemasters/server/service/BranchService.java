package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;

import java.util.List;

public interface BranchService {
    BranchResponseDto createBranch(BranchRequestDto branchRequestDto);
    BranchResponseDto updateBranchName(Long id, String newName);
    BranchResponseDto reassignParent(Long id, Long newParentId);
    BranchResponseDto getBranchById(Long id, boolean includeParent);
    BranchResponseDto getBranchByName(String name, boolean includeParent);
    List<BranchResponseDto> getAllBranches(boolean includeParent);
    List<BranchResponseDto> getChildBranches(Long parentId, boolean includeParent);
    List<BranchResponseDto> getParentBranches();
    List<BranchResponseDto> getBranchesByLevel(int level);
}