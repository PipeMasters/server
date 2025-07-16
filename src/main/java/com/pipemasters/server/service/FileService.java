package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.FileUploadRequestDto;

import java.util.UUID;

public interface FileService {
    String generatePresignedUploadUrlForVideo(FileUploadRequestDto fileUploadRequestDTO);

    String generatePresignedUploadUrlForAudio(String sourceKey);

    String generatePresignedDownloadUrl(Long mediaFileId);

    void deleteUploadBatchDirectory(UUID directoryUuid);
}