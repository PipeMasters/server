package com.pipemasters.server.service;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BranchService {
    BranchResponseDto createBranch(BranchRequestDto branchRequestDto);
    BranchResponseDto updateBranchName(Long id, String newName);
    BranchResponseDto reassignParent(Long id, Long newParentId);
    BranchResponseDto getBranchById(Long id, boolean includeParent);
    BranchResponseDto getBranchByName(String name, boolean includeParent);
    List<BranchResponseDto> getAllBranches(boolean includeParent);
    PageDto<BranchResponseDto> getPaginatedBranches(boolean includeParent, Pageable pageable);
    List<BranchResponseDto> getChildBranches(Long parentId, boolean includeParent);
    List<BranchResponseDto> getParentBranches();
    List<BranchResponseDto> getBranchesByLevel(int level);
}