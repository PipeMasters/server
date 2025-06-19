package com.pipemasters.server.controller;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload-url")
    public ResponseEntity<String> getPresignedUploadUrl(@RequestBody FileUploadRequestDto request) {
        String url = fileService.generatePresignedUploadUrl(request);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/download-url")
    public ResponseEntity<String> getPresignedDownloadUrl(@RequestParam Long mediaFileId) {
        String url = fileService.generatePresignedDownloadUrl(mediaFileId);
        return ResponseEntity.ok(url);
    }
}