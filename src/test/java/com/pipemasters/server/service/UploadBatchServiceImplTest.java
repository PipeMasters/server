package com.pipemasters.server.service;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.response.MediaFileResponseDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.impl.UploadBatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;

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

    private UploadBatchRequestDto testDto;
    private UploadBatch testEntity;
    private UploadBatchResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testDto = new UploadBatchRequestDto();
        testDto.setId(1L);
        testDto.setComment("Test comment");

        testEntity = new UploadBatch();
        testEntity.setId(1L);
        testEntity.setComment("Test comment");
        testEntity.setDirectory(UUID.randomUUID());
        testEntity.setCreatedAt(Instant.now());

        testResponseDto = new UploadBatchResponseDto();
        testResponseDto.setId(1L);
        testResponseDto.setComment("Test comment");
        testResponseDto.setDirectory(testEntity.getDirectory().toString());
        testResponseDto.setCreatedAt(testEntity.getCreatedAt());
        testResponseDto.setDeletedAt(testEntity.getCreatedAt().plus(180, ChronoUnit.DAYS));
        testResponseDto.setDeleted(false);
    }

    @Test
    void save_ShouldSetDefaultValuesAndSave() {
        // Arrange
        when(modelMapper.map(any(UploadBatchRequestDto.class), eq(UploadBatch.class)))
                .thenAnswer(invocation -> {
                    UploadBatchRequestDto dto = invocation.getArgument(0);
                    UploadBatch entity = new UploadBatch();
                    entity.setDirectory(UUID.fromString(dto.getDirectory() != null ? dto.getDirectory() : UUID.randomUUID().toString()));
                    entity.setComment(dto.getComment());
                    entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now());
                    entity.setDeletedAt(dto.getDeletedAt());
                    entity.setDeleted(dto.isDeleted());
                    if (dto.getId() != null) {
                        entity.setId(dto.getId());
                    }
                    return entity;
                });


        ArgumentCaptor<UploadBatch> captor = ArgumentCaptor.forClass(UploadBatch.class);
        when(uploadBatchRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchResponseDto.class)))
                .thenReturn(testResponseDto);

        // Act
        UploadBatchResponseDto result = uploadBatchService.save(testDto);

        // Assert
        assertNotNull(result);

        UploadBatch savedEntity = captor.getValue();
        assertNotNull(savedEntity.getDirectory());
        assertNotNull(savedEntity.getCreatedAt());
        assertNotNull(savedEntity.getDeletedAt());
        assertFalse(savedEntity.isDeleted());

        assertEquals(
                savedEntity.getCreatedAt().plus(180, ChronoUnit.DAYS),
                savedEntity.getDeletedAt()
        );

        assertEquals(testResponseDto.getId(), result.getId());
        assertEquals(testResponseDto.getComment(), result.getComment());
        assertEquals(testResponseDto.getDirectory(), result.getDirectory());
        assertEquals(testResponseDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(testResponseDto.getDeletedAt(), result.getDeletedAt());
        assertEquals(testResponseDto.isDeleted(), result.isDeleted());

        verify(modelMapper).map(testDto, UploadBatch.class);
        verify(uploadBatchRepository).save(any(UploadBatch.class));
        verify(modelMapper).map(any(UploadBatch.class), eq(UploadBatchResponseDto.class));
    }

    @Test
    void getById_ShouldReturnDtoWhenExists() {
        // Arrange
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(modelMapper.map(testEntity, UploadBatchResponseDto.class)).thenReturn(testResponseDto);

        // Act
        UploadBatchResponseDto result = uploadBatchService.getById(1L);

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
        List<UploadBatchResponseDto> expectedResponseDtos = Arrays.asList(testResponseDto, testResponseDto);

        when(uploadBatchRepository.findAll()).thenReturn(entities);
        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchResponseDto.class))).thenReturn(testResponseDto);

        // Act
        List<UploadBatchResponseDto> result = uploadBatchService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedResponseDtos, result);
    }

    @Test
    void updateUploadBatchDto_ShouldUpdateExistingEntity() {
        // Arrange
        UploadBatchRequestDto updatedDto = new UploadBatchRequestDto();
        updatedDto.setId(1L);
        updatedDto.setComment("Updated comment");
        updatedDto.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        updatedDto.setDirectory(UUID.randomUUID().toString());
        updatedDto.setDeleted(false);

        UploadBatch existingUploadBatch = new UploadBatch();
        existingUploadBatch.setId(1L);
        existingUploadBatch.setComment("Original comment");
        existingUploadBatch.setDirectory(UUID.randomUUID());
        existingUploadBatch.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        existingUploadBatch.setDeleted(false);
        existingUploadBatch.setDeletedAt(existingUploadBatch.getCreatedAt().plus(180, ChronoUnit.DAYS));


        UploadBatch mappedFromDto = new UploadBatch();
        mappedFromDto.setComment(updatedDto.getComment());
        mappedFromDto.setDirectory(UUID.fromString(updatedDto.getDirectory()));
        mappedFromDto.setCreatedAt(updatedDto.getCreatedAt());
        mappedFromDto.setDeleted(updatedDto.isDeleted());

        UploadBatch savedUploadBatch = new UploadBatch();
        savedUploadBatch.setId(existingUploadBatch.getId());
        savedUploadBatch.setComment(updatedDto.getComment());
        savedUploadBatch.setDirectory(UUID.fromString(updatedDto.getDirectory()));
        savedUploadBatch.setCreatedAt(updatedDto.getCreatedAt());
        savedUploadBatch.setDeleted(updatedDto.isDeleted());
        savedUploadBatch.setDeletedAt(savedUploadBatch.getCreatedAt().plus(180, ChronoUnit.DAYS));

        UploadBatchResponseDto expectedResponseDto = new UploadBatchResponseDto();
        expectedResponseDto.setId(savedUploadBatch.getId());
        expectedResponseDto.setComment(savedUploadBatch.getComment());
        expectedResponseDto.setDirectory(savedUploadBatch.getDirectory().toString());
        expectedResponseDto.setCreatedAt(savedUploadBatch.getCreatedAt());
        expectedResponseDto.setDeleted(savedUploadBatch.isDeleted());
        expectedResponseDto.setDeletedAt(savedUploadBatch.getDeletedAt());


        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(existingUploadBatch));

        when(modelMapper.map(eq(updatedDto), eq(UploadBatch.class))).thenReturn(mappedFromDto);

        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(savedUploadBatch);


        when(modelMapper.map(eq(savedUploadBatch), eq(UploadBatchResponseDto.class))).thenReturn(expectedResponseDto);

        // Act
        UploadBatchResponseDto result = uploadBatchService.update(1L, updatedDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponseDto, result);
        assertEquals("Updated comment", result.getComment());
        assertEquals(1L, result.getId());
        assertEquals(updatedDto.getDirectory(), result.getDirectory());
        assertEquals(updatedDto.getCreatedAt(), result.getCreatedAt());
        assertFalse(result.isDeleted());


        verify(uploadBatchRepository).findById(1L);
        verify(modelMapper).map(eq(updatedDto), eq(UploadBatch.class));
        verify(uploadBatchRepository).save(any(UploadBatch.class));
        verify(modelMapper).map(any(UploadBatch.class), eq(UploadBatchResponseDto.class));
    }

    @Test
    void updateUploadBatchDto_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> uploadBatchService.update(1L, new UploadBatchRequestDto()));
        verify(uploadBatchRepository).findById(1L);
        verifyNoMoreInteractions(modelMapper, uploadBatchRepository);
    }

    @Test
    void getFilteredBatches_ShouldReturnPagedListOfResponseDtosWithVideoFile() {
        UploadBatchFilter filter = new UploadBatchFilter();
        Pageable pageable = PageRequest.of(0, 10);

        UploadBatch batch1 = new UploadBatch();
        batch1.setId(1L);
        batch1.setComment("Batch 1 with video");
        batch1.setDirectory(UUID.randomUUID());
        batch1.setCreatedAt(Instant.now());
        MediaFile videoFile1 = new MediaFile("video1.mp4", FileType.VIDEO, Instant.now(), null, batch1);
        MediaFile audioFile1 = new MediaFile("audio1.mp3", FileType.AUDIO, Instant.now(), null, batch1);
        batch1.setFiles(Arrays.asList(videoFile1, audioFile1));

        UploadBatch batch2 = new UploadBatch();
        batch2.setId(2L);
        batch2.setComment("Batch 2 without video");
        batch2.setDirectory(UUID.randomUUID());
        batch2.setCreatedAt(Instant.now());
        MediaFile imageFile2 = new MediaFile("image2.jpg", FileType.IMAGE, Instant.now(), null, batch2);
        batch2.setFiles(Collections.singletonList(imageFile2));

        UploadBatch batch3 = new UploadBatch();
        batch3.setId(3L);
        batch3.setComment("Batch 3 with video");
        batch3.setDirectory(UUID.randomUUID());
        batch3.setCreatedAt(Instant.now());
        MediaFile videoFile3 = new MediaFile("video3.mp4", FileType.VIDEO, Instant.now(), null, batch3);
        batch3.setFiles(Collections.singletonList(videoFile3));

        List<UploadBatch> entities = Arrays.asList(batch1, batch2, batch3);
        Page<UploadBatch> entityPage = new PageImpl<>(entities, pageable, entities.size());

        when(uploadBatchRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);

        UploadBatchResponseDto responseDto1 = new UploadBatchResponseDto();
        responseDto1.setId(1L);
        responseDto1.setComment("Batch 1 with video");
        responseDto1.setDirectory(batch1.getDirectory().toString());
        responseDto1.setCreatedAt(batch1.getCreatedAt());
        responseDto1.setDeleted(batch1.isDeleted());
        responseDto1.setDeletedAt(batch1.getDeletedAt());

        when(modelMapper.map(batch1, UploadBatchResponseDto.class)).thenReturn(responseDto1);

        UploadBatchResponseDto responseDto2 = new UploadBatchResponseDto();
        responseDto2.setId(2L);
        responseDto2.setComment("Batch 2 without video");
        responseDto2.setDirectory(batch2.getDirectory().toString());
        responseDto2.setCreatedAt(batch2.getCreatedAt());
        responseDto2.setDeleted(batch2.isDeleted());
        responseDto2.setDeletedAt(batch2.getDeletedAt());
        when(modelMapper.map(batch2, UploadBatchResponseDto.class)).thenReturn(responseDto2);

        UploadBatchResponseDto responseDto3 = new UploadBatchResponseDto();
        responseDto3.setId(3L);
        responseDto3.setComment("Batch 3 with video");
        responseDto3.setDirectory(batch3.getDirectory().toString());
        responseDto3.setCreatedAt(batch3.getCreatedAt());
        responseDto3.setDeleted(batch3.isDeleted());
        responseDto3.setDeletedAt(batch3.getDeletedAt());
        when(modelMapper.map(batch3, UploadBatchResponseDto.class)).thenReturn(responseDto3);

        MediaFileResponseDto videoDto1 = new MediaFileResponseDto();
        videoDto1.setFilename("video1.mp4");
        videoDto1.setFileType(FileType.VIDEO);
        videoDto1.setUploadedAt(videoFile1.getUploadedAt());
        when(modelMapper.map(videoFile1, MediaFileResponseDto.class)).thenReturn(videoDto1);

        MediaFileResponseDto videoDto3 = new MediaFileResponseDto();
        videoDto3.setFilename("video3.mp4");
        videoDto3.setFileType(FileType.VIDEO);
        videoDto3.setUploadedAt(videoFile3.getUploadedAt());
        when(modelMapper.map(videoFile3, MediaFileResponseDto.class)).thenReturn(videoDto3);

        PageDto<UploadBatchResponseDto> resultPage = uploadBatchService.getFilteredBatches(filter, pageable);

        assertNotNull(resultPage);
        assertEquals(3, resultPage.getTotalElements());
        assertEquals(3, resultPage.getContent().size());

        UploadBatchResponseDto dto1Result = resultPage.getContent().get(0);
        assertEquals(1L, dto1Result.getId());
        assertEquals("Batch 1 with video", dto1Result.getComment());
        assertNotNull(dto1Result.getFile());
        assertEquals("video1.mp4", dto1Result.getFile().getFilename());
        assertEquals(FileType.VIDEO, dto1Result.getFile().getFileType());

        UploadBatchResponseDto dto2Result = resultPage.getContent().get(1);
        assertEquals(2L, dto2Result.getId());
        assertEquals("Batch 2 without video", dto2Result.getComment());
        assertNull(dto2Result.getFile());

        UploadBatchResponseDto dto3Result = resultPage.getContent().get(2);
        assertEquals(3L, dto3Result.getId());
        assertEquals("Batch 3 with video", dto3Result.getComment());
        assertNotNull(dto3Result.getFile());
        assertEquals("video3.mp4", dto3Result.getFile().getFilename());
        assertEquals(FileType.VIDEO, dto3Result.getFile().getFileType());

        verify(uploadBatchRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, times(3)).map(any(UploadBatch.class), eq(UploadBatchResponseDto.class));
        verify(modelMapper, times(2)).map(any(MediaFile.class), eq(MediaFileResponseDto.class));
    }

    @Test
    void getFilteredBatches_ShouldReturnEmptyPageWhenNoBatchesFound() {
        UploadBatchFilter filter = new UploadBatchFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UploadBatch> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(uploadBatchRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PageDto<UploadBatchResponseDto> resultPage = uploadBatchService.getFilteredBatches(filter, pageable);

        assertNotNull(resultPage);
        assertEquals(0, resultPage.getTotalElements());
        assertEquals(0, resultPage.getContent().size());
        verify(uploadBatchRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, never()).map(any(UploadBatch.class), eq(UploadBatchResponseDto.class));
        verify(modelMapper, never()).map(any(MediaFile.class), eq(MediaFileResponseDto.class));
    }
}