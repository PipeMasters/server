package com.pipemasters.server.service;

import com.pipemasters.server.dto.FileUploadRequestDTO;

public interface FileService {
    String generatePresignedUploadUrl(FileUploadRequestDTO fileUploadRequestDTO);
    String generatePresignedDownloadUrl(Long mediaFileId);
}
