package com.pipemasters.server.service;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UploadBatchService {



    UploadBatchDto save(UploadBatchDto uploadBatchDto);
    UploadBatchDto getById(Long id);
    List<UploadBatchDto> getAll();
    UploadBatchDto updateUploadBatchDto(Long uploadBatchId, UploadBatchDto dto);
  Page<UploadBatchDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable);
}
