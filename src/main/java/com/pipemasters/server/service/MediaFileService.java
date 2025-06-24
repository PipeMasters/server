package com.pipemasters.server.service;


import com.pipemasters.server.dto.MediaFileResponseDto;

import java.util.List;

public interface MediaFileService {
    List<MediaFileResponseDto> getMediaFilesByUploadBatchId(Long uploadBatchId);
}
