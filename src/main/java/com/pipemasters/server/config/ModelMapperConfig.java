package com.pipemasters.server.config;

import com.pipemasters.server.dto.DelegationDTO;
import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.dto.BranchDto;
import com.pipemasters.server.dto.MediaFileDto;
import com.pipemasters.server.dto.RecordDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.MediaFile;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Delegation.class, DelegationDTO.class)
                .addMapping(e -> e.getDelegator().getId(), DelegationDTO::setDelegatorId)
                .addMapping(e -> e.getSubstitute().getId(), DelegationDTO::setSubstituteId);
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
        modelMapper.typeMap(Record.class, RecordDto.class)
                .addMappings(mapper -> {
                    mapper.skip(RecordDto::setAbsence);
                    mapper.skip(RecordDto::setFiles);
                });
    }
}
