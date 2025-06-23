package com.pipemasters.server.service;

import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {
    Page<UploadBatchDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
}