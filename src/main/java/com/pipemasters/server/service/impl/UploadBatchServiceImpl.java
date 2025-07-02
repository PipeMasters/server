package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.create.UploadBatchCreateDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.*;
import com.pipemasters.server.repository.specifications.UploadBatchSpecifications;
import com.pipemasters.server.service.UploadBatchService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UploadBatchServiceImpl implements UploadBatchService {

    private final UploadBatchRepository uploadBatchRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final BranchRepository branchRepository;
    private final VideoAbsenceRepository videoAbsenceRepository;

    public UploadBatchServiceImpl(UploadBatchRepository uploadBatchRepository, ModelMapper modelMapper, UserRepository userRepository, TrainRepository trainRepository, BranchRepository branchRepository, VideoAbsenceRepository videoAbsenceRepository) {
        this.uploadBatchRepository = uploadBatchRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.branchRepository = branchRepository;
        this.videoAbsenceRepository = videoAbsenceRepository;
    }

    @Override
    @CacheEvict(value = {"filteredBatches", "batches"}, allEntries = true)
    @Transactional
    public UploadBatchResponseDto save(UploadBatchCreateDto uploadBatchRequestDto) {
        User user = userRepository.findById(uploadBatchRequestDto.getUploadedById())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + uploadBatchRequestDto.getUploadedById()));
        Train train = trainRepository.findById(uploadBatchRequestDto.getTrainId())
                .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + uploadBatchRequestDto.getTrainId()));
        Branch branch = branchRepository.findById(uploadBatchRequestDto.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + uploadBatchRequestDto.getBranchId()));


        UploadBatch uploadBatch = new UploadBatch(
                user,
                uploadBatchRequestDto.getTrainDeparted(),
                uploadBatchRequestDto.getTrainArrived(),
                train,
                uploadBatchRequestDto.getComment(),
                branch);

        uploadBatchRepository.save(uploadBatch);

        if (uploadBatchRequestDto.getAbsence() != null) {
            VideoAbsence videoAbsence = new VideoAbsence(uploadBatch, uploadBatchRequestDto.getAbsence().getCause(), uploadBatchRequestDto.getAbsence().getComment());
            videoAbsenceRepository.save(videoAbsence);
        }

        return modelMapper.map(uploadBatch, UploadBatchResponseDto.class);
    }

    @Override
    @Cacheable(cacheNames = "filteredBatches", keyGenerator = "uploadBatchFilterKeyGenerator")
    @Transactional(readOnly = true)
    public PageDto<UploadBatchDtoSmallResponse> getFilteredBatches(UploadBatchFilter filter, Pageable pageable) {
        Page<UploadBatch> page = uploadBatchRepository.findAll(UploadBatchSpecifications.withFilter(filter), pageable);

        List<UploadBatchDtoSmallResponse> dtoList = page.stream()
                .map(batch -> modelMapper.map(batch, UploadBatchDtoSmallResponse.class))
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
    public UploadBatchResponseDto getById(Long id) {
        var uploadBatch = uploadBatchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UploadBatch not found"));
        return modelMapper.map(uploadBatch, UploadBatchResponseDto.class);
    }

    @Override
    @Cacheable("batches")
    @Transactional(readOnly = true)
    public List<UploadBatchDtoSmallResponse> getAll() {
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
                .map(uploadBatch -> modelMapper.map(uploadBatch, UploadBatchDtoSmallResponse.class)).toList();
    }

    @Override
    @CacheEvict(value = {"filteredBatches", "batches"}, allEntries = true)
    @Transactional
    public UploadBatchResponseDto update(Long uploadBatchId, UploadBatchRequestDto dto) {
        var uploadBatchOrigin = uploadBatchRepository.findById(uploadBatchId)
                .orElseThrow(() -> new RuntimeException("UploadBatch not found with ID: " + uploadBatchId));
        var uploadBatch = modelMapper.map(dto, UploadBatch.class);
        uploadBatch.setId(uploadBatchOrigin.getId());

        return modelMapper.map(uploadBatchRepository.save(uploadBatch), UploadBatchResponseDto.class);
    }
}
