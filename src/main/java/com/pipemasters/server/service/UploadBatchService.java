package com.pipemasters.server.service;

import java.util.List;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {
    UploadBatchResponseDto save(UploadBatchRequestDto uploadBatchRequestDto);
    PageDto<UploadBatchResponseDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
    UploadBatchResponseDto getById(Long id);
    List<UploadBatchResponseDto> getAll();
    UploadBatchResponseDto update(Long uploadBatchId, UploadBatchRequestDto dto);
}
