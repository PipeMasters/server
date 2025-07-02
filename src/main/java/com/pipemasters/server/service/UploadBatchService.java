package com.pipemasters.server.service;

import java.util.List;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {
    UploadBatchDto save(UploadBatchDto uploadBatchDto);
    PageDto<UploadBatchDtoSmallResponse> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
    UploadBatchDto getById(Long id);
    List<UploadBatchDtoSmallResponse> getAll();
    UploadBatchDto update(Long uploadBatchId, UploadBatchDto dto);
}
