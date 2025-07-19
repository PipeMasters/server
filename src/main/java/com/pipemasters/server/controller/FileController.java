package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.FileUploadRequestDto;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.service.FileService;
import com.pipemasters.server.service.ImotioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;
    private final ImotioService imotioService;

    public FileController(FileService fileService, ImotioService imotioService) {
        this.fileService = fileService;
        this.imotioService = imotioService;
    }

    @PostMapping("/upload-url-video")
    public ResponseEntity<String> getPresignedUploadUrlForVideo(@RequestBody FileUploadRequestDto request) {
        String url = fileService.generatePresignedUploadUrlForVideo(request);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload-url-audio")
    public ResponseEntity<String> getPresignedUploadUrlForAudio(@RequestParam String sourceKey) {
        String url = fileService.generatePresignedUploadUrlForAudio(sourceKey);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/download-url")
    public ResponseEntity<String> getPresignedDownloadUrl(@RequestParam Long mediaFileId) {
        String url = fileService.generatePresignedDownloadUrl(mediaFileId);
        return ResponseEntity.ok(url);
    }

    // временно для тестов
    @GetMapping("/test/{mediaFileId}")
    public String test(@PathVariable Long mediaFileId) {
        return imotioService.processImotioFileUpload(mediaFileId);
    }
}