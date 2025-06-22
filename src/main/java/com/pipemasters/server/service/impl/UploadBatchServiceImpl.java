package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.UploadBatchService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UploadBatchServiceImpl implements UploadBatchService {

    private final UploadBatchRepository uploadBatchRepository;
    private final ModelMapper modelMapper;

    public UploadBatchServiceImpl(UploadBatchRepository uploadBatchRepository, ModelMapper modelMapper) {
        this.uploadBatchRepository = uploadBatchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<UploadBatchDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable) {
        Page<UploadBatch> page = uploadBatchRepository.findFiltered(filter, pageable);
        return page.map(batch -> modelMapper.map(batch, UploadBatchDto.class));
    }
}