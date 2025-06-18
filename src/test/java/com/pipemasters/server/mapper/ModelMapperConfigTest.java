package com.pipemasters.server.mapper;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class ModelMapperConfigTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void whenModelMapperConfigured_thenNoConfigurationErrors() {
        assertDoesNotThrow(() -> modelMapper.validate());
    }
}
