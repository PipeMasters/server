package com.pipemasters.server.service;

import java.util.List;

import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.request.create.UploadBatchCreateDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import org.springframework.data.domain.Pageable;


public interface UploadBatchService {
    UploadBatchResponseDto save(UploadBatchCreateDto uploadBatchDto);
    PageDto<UploadBatchDtoSmallResponse> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
    UploadBatchResponseDto getById(Long id);
    List<UploadBatchDtoSmallResponse> getAll();
    UploadBatchResponseDto update(Long uploadBatchId, UploadBatchRequestDto dto);
}
