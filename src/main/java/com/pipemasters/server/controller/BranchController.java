package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.ParsingStatsDto;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.response.BranchResponseDto;
import com.pipemasters.server.service.BranchService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/branch")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    public ResponseEntity<BranchResponseDto> create(
            @RequestBody BranchRequestDto dto) {
        BranchResponseDto created = branchService.createBranch(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<BranchResponseDto> rename(
            @PathVariable Long id,
            @RequestParam String name) {
        BranchResponseDto updated = branchService.updateBranchName(id, name);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PatchMapping("/{id}/reassign")
    public ResponseEntity<BranchResponseDto> reassignParent(
            @PathVariable Long id,
            @RequestParam(required = false) Long parentId) {
        BranchResponseDto updated = branchService.reassignParent(id, parentId);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDto> getBranchById(
            @PathVariable Long id,
            @RequestParam(value = "includeParent", defaultValue = "false") boolean includeParent) {
        BranchResponseDto branch = branchService.getBranchById(id, includeParent);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/by-name")
    public ResponseEntity<BranchResponseDto> getBranchByName(
            @RequestParam String name,
            @RequestParam(value = "includeParent", defaultValue = "false") boolean includeParent) {
        BranchResponseDto branch = branchService.getBranchByName(name, includeParent);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BranchResponseDto>> getAllBranches(
            @RequestParam(value = "includeParent", defaultValue = "false") boolean includeParent) {
        List<BranchResponseDto> branches = branchService.getAllBranches(includeParent);
        return ResponseEntity.ok(branches);
    }

    @GetMapping
    public ResponseEntity<Page<BranchResponseDto>> getPaginatedBranches(
            @RequestParam(value = "includeParent", defaultValue = "false") boolean includeParent,
            @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageDto<BranchResponseDto> dtoPage = branchService.getPaginatedBranches(includeParent, pageable);
        return new ResponseEntity<>(dtoPage.toPage(pageable), HttpStatus.OK);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<BranchResponseDto>> getChildBranches(
            @PathVariable Long parentId,
            @RequestParam(value = "includeParent", defaultValue = "false") boolean includeParent) {
        List<BranchResponseDto> childBranches = branchService.getChildBranches(parentId, includeParent);
        return ResponseEntity.ok(childBranches);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<BranchResponseDto>> getParentsBranches() {
        return ResponseEntity.ok(branchService.getParentBranches());
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<BranchResponseDto>> getBranchesByLevel(
            @PathVariable int level) {
        return ResponseEntity.ok(branchService.getBranchesByLevel(level));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsingStatsDto> uploadExcelFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return new ResponseEntity<>(
                    new ParsingStatsDto(
                            0, 0, 0, 0, 0,
                            Collections.singletonList("File to download is missing or empty.")
                    ),
                    HttpStatus.BAD_REQUEST
            );
        }
        ParsingStatsDto result = branchService.parseExcelFile(file);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        ByteArrayOutputStream out = branchService.exportBranchesToExcel();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "branches_" + timestamp + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}