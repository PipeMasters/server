package com.pipemasters.server.mapper;

import com.pipemasters.server.config.ModelMapperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class ModelMapperConfigTest {

    private ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
        modelMapper = new ModelMapperConfig().modelMapper();
    }

    @Test
    @Disabled
    public void whenModelMapperConfigured_thenNoConfigurationErrors() {
        assertDoesNotThrow(() -> modelMapper.validate());
    }
}
