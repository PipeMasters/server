package com.pipemasters.server.config;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.request.BranchRequestDto;
import com.pipemasters.server.dto.request.DelegationRequestDto;
import com.pipemasters.server.dto.request.MediaFileRequestDto;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.update.UserUpdateDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.entity.*;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Delegation.class, DelegationRequestDto.class)
                .addMapping(e -> e.getDelegator().getId(), DelegationRequestDto::setDelegatorId)
                .addMapping(e -> e.getSubstitute().getId(), DelegationRequestDto::setSubstituteId);

        modelMapper.typeMap(Train.class, TrainResponseDto.class)
                .addMapping(e -> e.getChief().getId(), TrainResponseDto::setChiefId)
                .addMapping(e -> e.getChief().getId(), TrainResponseDto::setChiefId)
                .addMapping(e -> e.getBranch().getId(), TrainResponseDto::setBranchId);

        modelMapper.typeMap(UploadBatch.class, UploadBatchResponseDto.class)
                .addMapping(u -> u.getTrain().getChief(), UploadBatchResponseDto::setChief);
//                .addMapping(UploadBatch::getUploadedBy, UploadBatchResponseDto::setUploadedBy);


        configureBranchMapping(modelMapper);
        configureUploadBatchDtoResponseMapping(modelMapper);

        configureUserMapping(modelMapper);
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        configureFragmentMapping(modelMapper);
//        configureMediaFileMapping(modelMapper);
//        configureUploadBatchMapping(modelMapper);
//        configureVideoAbsenceDtoMapping(modelMapper);


//        modelMapper.validate();
        return modelMapper;
    }

    private void configureBranchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(Branch.class, BranchRequestDto.class)
                .addMappings(mapper -> {
                    mapper.skip(BranchRequestDto::setParentId);
                });
    }

    private void configureUserMapping(ModelMapper modelMapper) {
        modelMapper.createTypeMap(UserUpdateDto.class, User.class)
                .addMappings(mapper -> {
                    mapper.skip(User::setId);
                });
    }

    private void configureMediaFileMapping(ModelMapper modelMapper) {
        TypeMap<MediaFile, MediaFileRequestDto> typeMap = modelMapper.createTypeMap(MediaFile.class, MediaFileRequestDto.class);

        typeMap.addMappings(mapper -> {
            mapper.map(MediaFile::getFilename, MediaFileRequestDto::setFilename);
            mapper.map(MediaFile::getFileType, MediaFileRequestDto::setFileType);
            mapper.map(MediaFile::getUploadedAt, MediaFileRequestDto::setUploadedAt);

            mapper.skip(MediaFileRequestDto::setSourceId);
            mapper.skip(MediaFileRequestDto::setUploadBatchId);
        });
    }

    private void configureUploadBatchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchRequestDto.class)
                .addMappings(mapper -> {
                    mapper.map(UploadBatch::getDeletedAt, UploadBatchRequestDto::setDeletedAt);
                });
        modelMapper.typeMap(UploadBatchRequestDto.class, UploadBatch.class)
                .addMappings(mapper -> {
                    mapper.map(UploadBatchRequestDto::getDeletedAt, UploadBatch::setDeletedAt);
                });
    }

    private void configureVideoAbsenceDtoMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(VideoAbsence.class, VideoAbsenceDto.class)
                .addMappings(mapper -> {
                    mapper.map(VideoAbsence::getId, VideoAbsenceDto::setId);
                    mapper.map(VideoAbsence::getComment, VideoAbsenceDto::setComment);
                    mapper.map(VideoAbsence::getCause, VideoAbsenceDto::setCause);
                });
    }

    private void configureUploadBatchDtoResponseMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchDtoSmallResponse.class)
                .addMappings(mapper -> {
                    mapper.map(tn -> tn.getTrain().getTrainNumber(), UploadBatchDtoSmallResponse::setTrainNumber);
                    mapper.map(UploadBatch::getTrainDeparted, UploadBatchDtoSmallResponse::setDateDeparted);
                    mapper.map(UploadBatch::getTrainArrived, UploadBatchDtoSmallResponse::setDateArrived);
                    mapper.map(chf -> chf.getTrain().getChief().getFullName(), UploadBatchDtoSmallResponse::setChiefName);
                    mapper.map(br -> br.getBranch().getName(), UploadBatchDtoSmallResponse::setBranchName);
                });
        modelMapper.typeMap(UploadBatch.class, UploadBatchSearchDto.class)
                .addMappings(mapper -> {
                    mapper.map(tn -> tn.getTrain().getTrainNumber(), UploadBatchSearchDto::setTrainNumber);
                    mapper.map(UploadBatch::getTrainDeparted, UploadBatchSearchDto::setDateDeparted);
                    mapper.map(UploadBatch::getTrainArrived, UploadBatchSearchDto::setDateArrived);
                    mapper.map(chf -> chf.getTrain().getChief().getFullName(), UploadBatchSearchDto::setChiefName);
                    mapper.map(br -> br.getBranch().getName(), UploadBatchSearchDto::setBranchName);
                });
    }

    private void configureFragmentMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(TranscriptFragment.class, SttFragmentDto.class)
                .addMappings(mapper -> {
                    mapper.map(TranscriptFragment::getBeginTime, SttFragmentDto::setBegin);
                    mapper.map(TranscriptFragment::getEndTime, SttFragmentDto::setEnd);
                    mapper.skip(SttFragmentDto::setDirection);
                    mapper.map(TranscriptFragment::getText, SttFragmentDto::setText);
                    mapper.map(TranscriptFragment::getId, SttFragmentDto::setId);
                    mapper.map(TranscriptFragment::getFragmentId, SttFragmentDto::setFragment_id);
                });
    }
}
