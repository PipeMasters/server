package com.pipemasters.server.service;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
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

    @Test
    void save_ShouldSetDefaultValuesAndSave() {
        when(modelMapper.map(any(UploadBatchDto.class), eq(UploadBatch.class)))
                .thenAnswer(invocation -> {
                    UploadBatchDto dto = invocation.getArgument(0);
                    UploadBatch entity = new UploadBatch();
                    entity.setDirectory(UUID.fromString(dto.getDirectory()));
                    entity.setCreatedAt(dto.getCreatedAt());
                    entity.setDeletedAt(dto.getDeletedAt());
                    entity.setDeleted(dto.isDeleted());
                    return entity;
                });

        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchDto.class)))
                .thenReturn(testDto);

        ArgumentCaptor<UploadBatch> captor = ArgumentCaptor.forClass(UploadBatch.class);
        when(uploadBatchRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UploadBatchDto result = uploadBatchService.save(testDto);

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
    }

    @Test
    void getById_ShouldReturnDtoWhenExists() {
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(modelMapper.map(testEntity, UploadBatchDto.class)).thenReturn(testDto);

        UploadBatchDto result = uploadBatchService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getComment());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotFound() {
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> uploadBatchService.getById(1L));
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        List<UploadBatch> entities = Arrays.asList(testEntity, testEntity);
        when(uploadBatchRepository.findAll()).thenReturn(entities);
        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchDtoSmallResponse.class)))
                .thenReturn(new UploadBatchDtoSmallResponse());

        List<UploadBatchDtoSmallResponse> result = uploadBatchService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateUploadBatchDto_ShouldUpdateExistingEntity() {
        UploadBatchDto updatedDto = new UploadBatchDto();
        updatedDto.setId(1L);
        updatedDto.setComment("Updated comment");

        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(modelMapper.map(updatedDto, UploadBatch.class)).thenReturn(testEntity);
        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(testEntity);
        when(modelMapper.map(testEntity, UploadBatchDto.class)).thenReturn(updatedDto);

        UploadBatchDto result = uploadBatchService.update(1L, updatedDto);

        assertNotNull(result);
        assertEquals("Updated comment", result.getComment());
        verify(uploadBatchRepository).save(testEntity);
    }

    @Test
    void updateUploadBatchDto_ShouldThrowExceptionWhenNotFound() {
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> uploadBatchService.update(1L, new UploadBatchDto()));
    }

    @Test
    void getFilteredBatches_ShouldReturnPagedListOfResponseDtosWithVideoFile() {
        UploadBatchFilter filter = new UploadBatchFilter();
        Pageable pageable = PageRequest.of(0, 10);

        UploadBatch batch1 = new UploadBatch();
        batch1.setId(1L);
        batch1.setComment("Batch 1 with video");
        batch1.setDirectory(UUID.randomUUID());
        MediaFile videoFile1 = new MediaFile("video1.mp4", FileType.VIDEO, Instant.now(), null, batch1);
        MediaFile audioFile1 = new MediaFile("audio1.mp3", FileType.AUDIO, Instant.now(), null, batch1);
        batch1.setFiles(Arrays.asList(videoFile1, audioFile1));

        UploadBatch batch2 = new UploadBatch();
        batch2.setId(2L);
        batch2.setComment("Batch 2 without video");
        batch2.setDirectory(UUID.randomUUID());
        MediaFile imageFile2 = new MediaFile("image2.jpg", FileType.IMAGE, Instant.now(), null, batch2);
        batch2.setFiles(Collections.singletonList(imageFile2));

        UploadBatch batch3 = new UploadBatch();
        batch3.setId(3L);
        batch3.setComment("Batch 3 with video");
        batch3.setDirectory(UUID.randomUUID());
        MediaFile videoFile3 = new MediaFile("video3.mp4", FileType.VIDEO, Instant.now(), null, batch3);
        batch3.setFiles(Collections.singletonList(videoFile3));

        List<UploadBatch> entities = Arrays.asList(batch1, batch2, batch3);
        Page<UploadBatch> entityPage = new PageImpl<>(entities, pageable, entities.size());

        when(uploadBatchRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);

        UploadBatchDtoSmallResponse responseDto1 = new UploadBatchDtoSmallResponse();
        responseDto1.setId(1L);
        when(modelMapper.map(batch1, UploadBatchDtoSmallResponse.class)).thenReturn(responseDto1);

        UploadBatchDtoSmallResponse responseDto2 = new UploadBatchDtoSmallResponse();
        responseDto2.setId(2L);
        when(modelMapper.map(batch2, UploadBatchDtoSmallResponse.class)).thenReturn(responseDto2);

        UploadBatchDtoSmallResponse responseDto3 = new UploadBatchDtoSmallResponse();
        responseDto3.setId(3L);
        when(modelMapper.map(batch3, UploadBatchDtoSmallResponse.class)).thenReturn(responseDto3);

        PageDto<UploadBatchDtoSmallResponse> resultPage = uploadBatchService.getFilteredBatches(filter, pageable);

        assertNotNull(resultPage);
        assertEquals(3, resultPage.getTotalElements());
        assertEquals(3, resultPage.getContent().size());

        UploadBatchDtoSmallResponse dto1Result = resultPage.getContent().get(0);
        assertEquals(1L, dto1Result.getId());

        UploadBatchDtoSmallResponse dto2Result = resultPage.getContent().get(1);
        assertEquals(2L, dto2Result.getId());

        UploadBatchDtoSmallResponse dto3Result = resultPage.getContent().get(2);
        assertEquals(3L, dto3Result.getId());

        verify(uploadBatchRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, times(3)).map(any(UploadBatch.class), eq(UploadBatchDtoSmallResponse.class));
    }

    @Test
    void getFilteredBatches_ShouldReturnEmptyPageWhenNoBatchesFound() {
        UploadBatchFilter filter = new UploadBatchFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UploadBatch> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(uploadBatchRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PageDto<UploadBatchDtoSmallResponse> resultPage = uploadBatchService.getFilteredBatches(filter, pageable);

        assertNotNull(resultPage);
        assertEquals(0, resultPage.getTotalElements());
        assertEquals(0, resultPage.getContent().size());
        verify(uploadBatchRepository).findAll(any(Specification.class), eq(pageable));
        verify(modelMapper, never()).map(any(UploadBatch.class), eq(UploadBatchDtoSmallResponse.class));
    }
}