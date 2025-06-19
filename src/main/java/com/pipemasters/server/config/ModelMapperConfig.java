package com.pipemasters.server.config;

import com.pipemasters.server.dto.DelegationDTO;
import com.pipemasters.server.entity.Delegation;
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

        return modelMapper;
    }
}
