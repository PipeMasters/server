package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.FileUploadRequestDto;
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

    @PostMapping("/upload-url-video")
    public ResponseEntity<String> getPresignedUploadUrlForVideo(@RequestBody FileUploadRequestDto request) {
        String url = fileService.generatePresignedUploadUrlForVideo(request);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload-url-audio")
    public ResponseEntity<String> getPresignedUploadUrlForAudio(@PathVariable String sourceKey) {
        String url = fileService.generatePresignedUploadUrlForAudio(sourceKey);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/download-url")
    public ResponseEntity<String> getPresignedDownloadUrl(@RequestParam Long mediaFileId) {
        String url = fileService.generatePresignedDownloadUrl(mediaFileId);
        return ResponseEntity.ok(url);
    }
}