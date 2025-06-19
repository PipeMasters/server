package com.pipemasters.server.config;

import com.pipemasters.server.dto.DelegationDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.MediaFileDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import org.modelmapper.ModelMapper;
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
//        configureRecordMapping(modelMapper);
//        configureRecordMapping(modelMapper);


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
        modelMapper.typeMap(MediaFile.class, MediaFileDto.class)
                .addMappings(mapper -> {
                    mapper.skip(MediaFileDto::setSource);
                });
    }

    private void configureRecordMapping(ModelMapper modelMapper) {
        modelMapper.typeMap(UploadBatch.class, UploadBatchDto.class)
                .addMappings(mapper -> {
                    mapper.skip(UploadBatchDto::setAbsence);
                    mapper.skip(UploadBatchDto::setFiles);
                });
    }
}
