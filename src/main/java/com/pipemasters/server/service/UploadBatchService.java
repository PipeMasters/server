package com.pipemasters.server.service;

import java.util.List;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {



    UploadBatchDto save(UploadBatchDto uploadBatchDto);
    UploadBatchDto getById(Long id);
    List<UploadBatchDto> getAll();
    UploadBatchDto updateUploadBatchDto(Long uploadBatchId, UploadBatchDto dto);
    Page<UploadBatchDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
}
