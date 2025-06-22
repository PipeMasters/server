package com.pipemasters.server.repository;

import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.UploadBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UploadBatchFilterRepository {
    Page<UploadBatch> findFiltered(UploadBatchFilter filter, Pageable pageable);
}