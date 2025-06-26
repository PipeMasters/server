package com.pipemasters.server.service;

import java.util.List;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.UploadBatchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UploadBatchService {
    UploadBatchDto save(UploadBatchDto uploadBatchDto);
    Page<UploadBatchResponseDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
    UploadBatchDto getById(Long id);
    List<UploadBatchDto> getAll();
    UploadBatchDto updateUploadBatchDto(Long uploadBatchId, UploadBatchDto dto);
}
