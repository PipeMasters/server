package com.pipemasters.server.service;

import java.util.UUID;

import com.pipemasters.server.dto.MediaFileResponseDto;

import java.util.List;

public interface MediaFileService {
    void handleMinioFileDeletion(UUID batchUuid, String filename);
    List<MediaFileResponseDto> getMediaFilesByUploadBatchId(Long uploadBatchId);
}
