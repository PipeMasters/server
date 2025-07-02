package com.pipemasters.server.config;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.entity.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Delegation.class, DelegationDto.class)
                .addMapping(e -> e.getDelegator().getId(), DelegationDto::setDelegatorId)
                .addMapping(e -> e.getSubstitute().getId(), DelegationDto::setSubstituteId);

        modelMapper.typeMap(Train.class, TrainDto.class)
                .addMapping(e -> e.getChief().getId(), TrainDto::setChiefId)
                .addMapping(e -> e.getChief().getId(), TrainDto::setChiefId)
                .addMapping(e -> e.getBranch().getId(), TrainDto::setBranchId);

        configureBranchMapping(modelMapper);
        configureUploadBatchDtoResponseMapping(modelMapper);

//        configureMediaFileMapping(modelMapper);
//        configureUploadBatchMapping(modelMapper);
//        configureVideoAbsenceDtoMapping(modelMapper);


//        modelMapper.validate();
        return modelMapper;
    }

    private void configureBranchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(Branch.class, BranchDto.class)
                .addMappings(mapper -> {
                    mapper.skip(BranchDto::setParentId);
                });
    }

    private void configureMediaFileMapping(ModelMapper modelMapper) {
        TypeMap<MediaFile, MediaFileDto> typeMap = modelMapper.createTypeMap(MediaFile.class, MediaFileDto.class);

        typeMap.addMappings(mapper -> {
            mapper.map(MediaFile::getFilename, MediaFileDto::setFilename);
            mapper.map(MediaFile::getFileType, MediaFileDto::setFileType);
            mapper.map(MediaFile::getUploadedAt, MediaFileDto::setUploadedAt);

            mapper.skip(MediaFileDto::setSourceId);
            mapper.skip(MediaFileDto::setUploadBatchId);
        });
    }

    private void configureUploadBatchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchDto.class)
                .addMappings(mapper -> {
                    mapper.map(UploadBatch::getDeletedAt, UploadBatchDto::setDeletedAt);
                });
        modelMapper.typeMap(UploadBatchDto.class, UploadBatch.class)
                .addMappings(mapper -> {
                    mapper.map(UploadBatchDto::getDeletedAt, UploadBatch::setDeletedAt);
                });
    }

    private void configureVideoAbsenceDtoMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(VideoAbsence.class, VideoAbsenceDto.class)
                .addMappings(mapper -> {
                    mapper.skip(VideoAbsenceDto::setUploadBatch);
                    mapper.skip(VideoAbsenceDto::setCause);
                });
    }

    private void configureUploadBatchDtoResponseMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchDtoSmallResponse.class)
                .addMappings(mapper -> {
                    mapper.map(tn -> tn.getTrain().getTrainNumber(), UploadBatchDtoSmallResponse::setTrainNumber);
                    mapper.map(UploadBatch::getTrainDeparted, UploadBatchDtoSmallResponse::setDateDeparted);
                    mapper.map(UploadBatch::getTrainArrived, UploadBatchDtoSmallResponse::setDateArrived);
                    mapper.map(chf -> chf.getTrain().getChief().getFullName(), UploadBatchDtoSmallResponse::setChiefName);
                });
    }
}
