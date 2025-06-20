package com.pipemasters.server.service;

import com.pipemasters.server.dto.FileUploadRequestDto;

public interface FileService {
    String generatePresignedUploadUrl(FileUploadRequestDto fileUploadRequestDTO);

    String generatePresignedDownloadUrl(Long mediaFileId);
}
