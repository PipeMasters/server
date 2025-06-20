package com.pipemasters.server.controller;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branch")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public ResponseEntity<BranchDto> create(@RequestBody BranchDto dto) {
        BranchDto created = branchService.createBranch(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<BranchDto> rename(@PathVariable Long id, @RequestParam String name) {
        BranchDto updated = branchService.updateBranchName(id, name);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PatchMapping("/{id}/reassign")
    public ResponseEntity<BranchDto> reassignParent(@PathVariable Long id, @RequestParam(required = false) Long parentId) {
        BranchDto updated = branchService.reassignParent(id, parentId);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
}