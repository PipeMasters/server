package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.response.UploadBatchDtoResponse;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.repository.specifications.UploadBatchSpecifications;
import com.pipemasters.server.service.UploadBatchService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = {"filteredBatches", "batches"}, allEntries = true)
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
    @Cacheable(cacheNames = "filteredBatches", keyGenerator = "uploadBatchFilterKeyGenerator")
    @Transactional(readOnly = true)
    public PageDto<UploadBatchDtoResponse> getFilteredBatches(UploadBatchFilter filter, Pageable pageable) {
        Page<UploadBatch> page = uploadBatchRepository.findAll(UploadBatchSpecifications.withFilter(filter), pageable);

        List<UploadBatchDtoResponse> dtoList = page.stream()
                .map(batch -> modelMapper.map(batch, UploadBatchDtoResponse.class))
                .toList();

//        List<UploadBatchResponseDto> dtoList = page.stream().map(batch -> {
//            UploadBatchResponseDto dto = modelMapper.map(batch, UploadBatchResponseDto.class);
//            batch.getFiles().stream()
//                    .filter(mediaFile -> FileType.VIDEO.equals(mediaFile.getFileType()))
//                    .findFirst()
//                    .ifPresent(videoMediaFile -> {
//                        MediaFileResponseDto mediaFileDto = modelMapper.map(videoMediaFile, MediaFileResponseDto.class);
//                        dto.setFile(mediaFileDto);
//                    });
//
//            return dto;
//        }).toList();

        return new PageDto<>(dtoList, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UploadBatchDto getById(Long id) {
        var uploadBatch = uploadBatchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UploadBatch not found"));
        return modelMapper.map(uploadBatch,UploadBatchDto.class);
    }

    @Override
    @Cacheable("batches")
    @Transactional(readOnly = true)
    public List<UploadBatchDtoResponse> getAll() {
//        List<UploadBatch> uploadBatchList = uploadBatchRepository.findAll();
//
//        List<UploadBatchDtoResponse> dtoList = uploadBatchList.stream()
//                .map(batch -> new UploadBatchDtoResponse(
//                        batch.getId(),
//                        batch.getTrainDeparted(),
//                        batch.getTrainDeparted(),
//                        batch.getTrain().getTrainNumber(),
//                        batch.getTrain().getChief()
//                )).toList();

        return uploadBatchRepository.findAll().stream()
                .map(uploadBatch -> modelMapper.map(uploadBatch,UploadBatchDtoResponse.class)).toList();
    }

    @Override
    @CacheEvict(value = {"filteredBatches", "batches"}, allEntries = true)
    @Transactional
    public UploadBatchDto update(Long uploadBatchId, UploadBatchDto dto) {
        var uploadBatchOrigin = uploadBatchRepository.findById(uploadBatchId)
                .orElseThrow(() -> new RuntimeException("UploadBatch not found with ID: " + uploadBatchId));
        var uploadBatch = modelMapper.map(dto, UploadBatch.class);
        uploadBatch.setId(uploadBatchOrigin.getId());

        return modelMapper.map(uploadBatchRepository.save(uploadBatch), UploadBatchDto.class);
    }
}
