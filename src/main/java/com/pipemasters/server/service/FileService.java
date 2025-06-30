package com.pipemasters.server.service;

import com.pipemasters.server.dto.request.FileUploadRequestDto;

import java.util.UUID;

public interface FileService {
    String generatePresignedUploadUrl(FileUploadRequestDto fileUploadRequestDTO);

    String generatePresignedDownloadUrl(Long mediaFileId);

    void deleteUploadBatchDirectory(UUID directoryUuid);
}