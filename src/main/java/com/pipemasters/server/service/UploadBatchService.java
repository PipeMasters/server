package com.pipemasters.server.service;

import java.util.List;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.response.UploadBatchDtoResponse;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {
    UploadBatchDto save(UploadBatchDto uploadBatchDto);
    PageDto<UploadBatchDtoResponse> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
    UploadBatchDto getById(Long id);
    List<UploadBatchDtoResponse> getAll();
    UploadBatchDto update(Long uploadBatchId, UploadBatchDto dto);
}
