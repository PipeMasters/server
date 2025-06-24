package com.pipemasters.server.controller;

import com.pipemasters.server.dto.MediaFileResponseDto;
import com.pipemasters.server.service.MediaFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media-files")
public class MediaFileController {

    private final MediaFileService mediaFileService;

    public MediaFileController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    @GetMapping("/by-batch/{uploadBatchId}")
    public ResponseEntity<List<MediaFileResponseDto>> getAllMediaFilesByUploadBatch(@PathVariable Long uploadBatchId) {
        List<MediaFileResponseDto> mediaFiles = mediaFileService.getMediaFilesByUploadBatchId(uploadBatchId);
        return ResponseEntity.ok(mediaFiles);
    }
}
