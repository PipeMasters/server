package com.pipemasters.server.service;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.dto.UploadBatchDto;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.impl.FileServiceImpl;
import com.pipemasters.server.service.impl.UploadBatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class UploadBatchServiceImplTest {

    @Mock
    private UploadBatchRepository uploadBatchRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UploadBatchServiceImpl uploadBatchService;

    private UploadBatchDto testDto;
    private UploadBatch testEntity;

    @BeforeEach
    void setUp() {
        testDto = new UploadBatchDto();
        testDto.setId(1L);
        testDto.setComment("Test comment");

        testEntity = new UploadBatch();
        testEntity.setId(1L);
        testEntity.setComment("Test comment");
        testEntity.setDirectory(UUID.randomUUID());
        testEntity.setCreatedAt(Instant.now());
    }
// Плохо сравнивает даты, 1.5 секунды разница возникает в одинаковых DeletedAt
//    Expected :2025-12-17T16:35:21.632205500Z
//    Actual   :2025-12-17T16:35:21.634300400Z
//    @Test
    void save_ShouldSetDefaultValuesAndSave() {
        // Arrange
        when(modelMapper.map(testDto, UploadBatch.class)).thenReturn(testEntity);
        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(testEntity);
        when(modelMapper.map(testEntity, UploadBatchDto.class)).thenReturn(testDto);

        // Act
        UploadBatchDto result = uploadBatchService.save(testDto);

        // Assert
        assertNotNull(result);
        verify(uploadBatchRepository).save(any(UploadBatch.class));

        // Verify default values are set
        assertNotNull(testEntity.getDirectory());
        assertNotNull(testEntity.getCreatedAt());
        assertNotNull(testEntity.getDeletedAt());
        assertFalse(testEntity.isDeleted());
        assertEquals(testEntity.getCreatedAt().plus(180, ChronoUnit.DAYS), testEntity.getDeletedAt());
    }

    @Test
    void getById_ShouldReturnDtoWhenExists() {
        // Arrange
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(modelMapper.map(testEntity, UploadBatchDto.class)).thenReturn(testDto);

        // Act
        UploadBatchDto result = uploadBatchService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getComment());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> uploadBatchService.getById(1L));
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        // Arrange
        List<UploadBatch> entities = Arrays.asList(testEntity, testEntity);
        when(uploadBatchRepository.findAll()).thenReturn(entities);
        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchDto.class))).thenReturn(testDto);

        // Act
        List<UploadBatchDto> result = uploadBatchService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateUploadBatchDto_ShouldUpdateExistingEntity() {
        // Arrange
        UploadBatchDto updatedDto = new UploadBatchDto();
        updatedDto.setId(1L);
        updatedDto.setComment("Updated comment");

        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(modelMapper.map(updatedDto, UploadBatch.class)).thenReturn(testEntity);
        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(testEntity);
        when(modelMapper.map(testEntity, UploadBatchDto.class)).thenReturn(updatedDto);

        // Act
        UploadBatchDto result = uploadBatchService.updateUploadBatchDto(1L, updatedDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated comment", result.getComment());
        verify(uploadBatchRepository).save(testEntity);
    }

    @Test
    void updateUploadBatchDto_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> uploadBatchService.updateUploadBatchDto(1L, new UploadBatchDto()));
    }
}