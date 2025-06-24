package com.pipemasters.server.controller;

import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> getBranchById(@PathVariable Long id) {
        BranchDto branch = branchService.getBranchById(id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/by-name")
    public ResponseEntity<BranchDto> getBranchByName(@RequestParam String name) {
        BranchDto branch = branchService.getBranchByName(name);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BranchDto>> getAllBranches() {
        List<BranchDto> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<BranchDto>> getChildBranches(@PathVariable Long parentId) {
        List<BranchDto> childBranches = branchService.getChildBranches(parentId);
        return ResponseEntity.ok(childBranches);
    }
}