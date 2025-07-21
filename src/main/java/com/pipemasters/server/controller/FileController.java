package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.FileUploadRequestDto;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.service.FileService;
import com.pipemasters.server.service.ImotioPollingSchedulerService;
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

    public FileController(FileService fileService, ImotioService imotioService, ImotioPollingSchedulerService imotioPollingSchedulerService) {
        this.fileService = fileService;
        this.imotioService = imotioService;
        this.imotioPollingSchedulerService = imotioPollingSchedulerService;
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
    public ResponseEntity<String> getPresignedDownloadUrl(
            @RequestParam(required = false) Long mediaFileId,
            @RequestParam(required = false) String sourceKey) {
        String url;
        boolean mediaFileIdIsPresent = mediaFileId != null;
        boolean sourceKeyIsPresent = sourceKey != null && !sourceKey.isBlank();
        if (!mediaFileIdIsPresent && !sourceKeyIsPresent) {
            throw new IllegalArgumentException("MediaFileId or SourceKey must be specified");
        }
        if (mediaFileIdIsPresent && sourceKeyIsPresent) {
            throw new IllegalArgumentException("Only one of MediaFileId or SourceKey should be specified");
        }
        if (mediaFileIdIsPresent) {
            url = fileService.generatePresignedDownloadUrl(mediaFileId);
        } else {
            url = fileService.getDownloadUrl(sourceKey);
        }
        return ResponseEntity.ok(url);
    }

    // временно для тестов
    private final ImotioService imotioService;
    private final ImotioPollingSchedulerService imotioPollingSchedulerService;
    @GetMapping("/test/{mediaFileId}")
    public void test(@PathVariable Long mediaFileId) {
        imotioService.processImotioFileUpload(mediaFileId);
    }

    @GetMapping("/test2/{mediaFileId}")
    public void test2(@PathVariable Long mediaFileId) {
        imotioPollingSchedulerService.addTaskToPoll("687d9d476eeb411aa7daedf9", mediaFileId);
        imotioService.processImotioFileUpload(mediaFileId);
    }
}