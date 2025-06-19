package com.pipemasters.server.config;

import com.pipemasters.server.dto.*;
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

        configureBranchMapping(modelMapper);
        configureMediaFileMapping(modelMapper);
//        configureUploadBatchMapping(modelMapper);
        configureVideoAbsenceDtoMapping(modelMapper);


        modelMapper.validate();
        return modelMapper;
    }

    private void configureBranchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(Branch.class, BranchDto.class)
                .addMappings(mapper -> {
                    mapper.skip(BranchDto::setParent);
                });
    }

    private void configureMediaFileMapping(ModelMapper modelMapper) {
        TypeMap<MediaFile, MediaFileDto> typeMap = modelMapper.createTypeMap(MediaFile.class, MediaFileDto.class);

        typeMap.addMappings(mapper -> {
            mapper.map(MediaFile::getFilename, MediaFileDto::setFilename);
            mapper.map(MediaFile::getFileType, MediaFileDto::setFileType);
            mapper.map(MediaFile::getUploadedAt, MediaFileDto::setUploadedAt);

            mapper.skip(MediaFileDto::setSource);
            mapper.skip(MediaFileDto::setUploadBatch);
        });
    }

    private void configureUploadBatchMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchDto.class)
                .addMappings(mapper -> {
                    mapper.skip(UploadBatchDto::setAbsence);
                    mapper.skip(UploadBatchDto::setFiles);
                });
    }

    private void configureVideoAbsenceDtoMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(VideoAbsence.class, VideoAbsenceDto.class)
                .addMappings(mapper -> {
                    mapper.skip(VideoAbsenceDto::setUploadBatch);
                });
    }
}
