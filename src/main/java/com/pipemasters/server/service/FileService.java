package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.FileUploadRequestDto;

import java.time.Duration;
import java.util.UUID;

public interface FileService {
    String generatePresignedUploadUrlForVideo(FileUploadRequestDto fileUploadRequestDTO);

    String generatePresignedUploadUrlForAudio(String sourceKey, Long duration, String hash);

    String generatePresignedDownloadUrl(Long mediaFileId);

    void deleteUploadBatchDirectory(UUID directoryUuid);

    String getDownloadUrl(String s3Key);

    void deleteMediaFileById(Long mediaFileId);
}