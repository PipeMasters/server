package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.create.UploadBatchCreateDto;
import com.pipemasters.server.dto.request.update.UploadBatchUpdateDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.file.UploadBatchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.*;
import com.pipemasters.server.repository.specifications.UploadBatchSpecifications;
import com.pipemasters.server.service.UploadBatchService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        var uploadBatch = uploadBatchRepository.findById(id).orElseThrow(() -> new UploadBatchNotFoundException("UploadBatch not found with ID: " + id));
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
    public UploadBatchResponseDto update(Long uploadBatchId, UploadBatchUpdateDto dto) {
        UploadBatch uploadBatchToUpdate = uploadBatchRepository.findById(uploadBatchId)
                .orElseThrow(() -> new UploadBatchNotFoundException("UploadBatch not found with ID: " + uploadBatchId));

        Optional.ofNullable(dto.getComment()).ifPresent(uploadBatchToUpdate::setComment);
        Optional.ofNullable(dto.getTrainDeparted()).ifPresent(uploadBatchToUpdate::setTrainDeparted);
        Optional.ofNullable(dto.getTrainArrived()).ifPresent(uploadBatchToUpdate::setTrainArrived);

        Optional.ofNullable(dto.getTrainId()).ifPresent(trainId -> {
            Train train = trainRepository.findById(trainId)
                    .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + trainId));
            uploadBatchToUpdate.setTrain(train);
        });

        Optional.ofNullable(dto.getBranchId()).ifPresent(branchId -> {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + branchId));
            uploadBatchToUpdate.setBranch(branch);
        });

        Optional.ofNullable(dto.getUploadedById()).ifPresent(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            uploadBatchToUpdate.setUploadedBy(user);
        });

        Optional.ofNullable(dto.getAbsence()).ifPresent(absenceDto -> {
            VideoAbsence absenceRecord = uploadBatchToUpdate.getAbsence();
            if (absenceRecord != null) {
                Optional.ofNullable(absenceDto.getCause()).ifPresent(absenceRecord::setCause);
                Optional.ofNullable(absenceDto.getComment()).ifPresent(absenceRecord::setComment);
            } else {
                uploadBatchToUpdate.setAbsence(new VideoAbsence(uploadBatchToUpdate, absenceDto.getCause(), absenceDto.getComment()));
            }
        });

        UploadBatch updatedBatch = uploadBatchRepository.save(uploadBatchToUpdate);
        return modelMapper.map(updatedBatch, UploadBatchResponseDto.class);
    }
}
