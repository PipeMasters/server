package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.MediaFileResponseDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.dto.UploadBatchResponseDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.repository.specifications.UploadBatchSpecifications;
import com.pipemasters.server.service.UploadBatchService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class UploadBatchServiceImpl implements UploadBatchService {

    private final UploadBatchRepository uploadBatchRepository;
    private final ModelMapper modelMapper;

    public UploadBatchServiceImpl(UploadBatchRepository uploadBatchRepository, ModelMapper modelMapper) {
        this.uploadBatchRepository = uploadBatchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public UploadBatchDto save(UploadBatchDto uploadBatchDto) {
        var now = Instant.now();
        uploadBatchDto.setDirectory(String.valueOf(UUID.randomUUID()));
        uploadBatchDto.setCreatedAt(now);
        uploadBatchDto.setDeletedAt(now.plus(180, ChronoUnit.DAYS));
        uploadBatchDto.setDeleted(false);
        return modelMapper.map(uploadBatchRepository
                        .save(modelMapper.map(uploadBatchDto,UploadBatch.class)),UploadBatchDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UploadBatchResponseDto> getFilteredBatches(UploadBatchFilter filter, Pageable pageable) {
        Page<UploadBatch> page = uploadBatchRepository.findAll(UploadBatchSpecifications.withFilter(filter), pageable);
        return page.map(batch -> {
            UploadBatchResponseDto dto = modelMapper.map(batch, UploadBatchResponseDto.class);
            batch.getFiles().stream()
                    .filter(mediaFile -> FileType.VIDEO.equals(mediaFile.getFileType()))
                    .findFirst()
                    .ifPresent(videoMediaFile -> {
                        MediaFileResponseDto mediaFileDto = modelMapper.map(videoMediaFile, MediaFileResponseDto.class);
                        dto.setFile(mediaFileDto);
                    });

            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public UploadBatchDto getById(Long id) {
        var uploadBatch = uploadBatchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UploadBatch not found"));
        return modelMapper.map(uploadBatch,UploadBatchDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadBatchDto> getAll() {
        return uploadBatchRepository.findAll().stream()
                .map(uploadBatch -> modelMapper.map(uploadBatch,UploadBatchDto.class)).toList();
    }

    @Override
    @Transactional
    public UploadBatchDto updateUploadBatchDto(Long uploadBatchId, UploadBatchDto dto) {
        var uploadBatchOrigin = uploadBatchRepository.findById(uploadBatchId)
                .orElseThrow(() -> new RuntimeException("UploadBatch not found with ID: " + uploadBatchId));
        var uploadBatch = modelMapper.map(dto, UploadBatch.class);
        uploadBatch.setId(uploadBatchOrigin.getId());

        return modelMapper.map(uploadBatchRepository.save(uploadBatch), UploadBatchDto.class);
    }
}
