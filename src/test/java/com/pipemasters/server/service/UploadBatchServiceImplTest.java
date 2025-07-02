package com.pipemasters.server.service;

import com.pipemasters.server.dto.*;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.request.UploadBatchRequestDto;
import com.pipemasters.server.dto.request.create.UploadBatchCreateDto;
import com.pipemasters.server.dto.request.create.UploadBatchCreateDto.VideoAbsenceCreateDto;
import com.pipemasters.server.dto.response.UploadBatchResponseDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.*;
import com.pipemasters.server.service.impl.UploadBatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadBatchServiceImplTest {

    @Mock
    private UploadBatchRepository uploadBatchRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainRepository trainRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private VideoAbsenceRepository videoAbsenceRepository;

    @InjectMocks
    private UploadBatchServiceImpl uploadBatchService;

    private UploadBatchCreateDto createDto;
    private UploadBatchCreateDto createDtoWithAbsence;
    private UploadBatch uploadBatch;
    private UploadBatchResponseDto responseDto;
    private User user;
    private Train train;
    private Branch branch;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(40L);

        train = mock(Train.class);
        branch = mock(Branch.class);

        createDto = new UploadBatchCreateDto(
                null, // absence
                20L, // branchId
                "Test comment",
                LocalDate.now().minusDays(1), // trainArrived
                LocalDate.now(), // trainDeparted
                30L, // trainId
                40L // uploadedById
        );

        createDtoWithAbsence = new UploadBatchCreateDto(
                new VideoAbsenceCreateDto("No video", AbsenceCause.DEVICE_FAILURE),
                20L,
                "Test comment",
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                30L,
                40L
        );

        uploadBatch = new UploadBatch(
                user,
                createDto.getTrainDeparted(),
                createDto.getTrainArrived(),
                train,
                createDto.getComment(),
                branch
        );
        uploadBatch.setId(1L);
        uploadBatch.setCreatedAt(Instant.now());
        uploadBatch.setDirectory(UUID.randomUUID());

        responseDto = new UploadBatchResponseDto();
        responseDto.setId(1L);
        responseDto.setComment("Test comment");
        responseDto.setDirectory(uploadBatch.getDirectory().toString());
        responseDto.setCreatedAt(uploadBatch.getCreatedAt());
        responseDto.setDeletedAt(uploadBatch.getCreatedAt().plus(180, ChronoUnit.DAYS));
        responseDto.setDeleted(false);
    }

    @Test
    void save_ShouldSetDefaultValuesAndSave_WithoutAbsence() {
        when(userRepository.findById(40L)).thenReturn(Optional.of(user));
        when(trainRepository.findById(30L)).thenReturn(Optional.of(train));
        when(branchRepository.findById(20L)).thenReturn(Optional.of(branch));
        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(uploadBatch);
        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchResponseDto.class))).thenReturn(responseDto);

        UploadBatchResponseDto result = uploadBatchService.save(createDto);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getComment(), result.getComment());
        assertEquals(responseDto.getDirectory(), result.getDirectory());
        assertEquals(responseDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(responseDto.getDeletedAt(), result.getDeletedAt());
        assertEquals(responseDto.isDeleted(), result.isDeleted());

        verify(userRepository).findById(40L);
        verify(trainRepository).findById(30L);
        verify(branchRepository).findById(20L);
        verify(uploadBatchRepository).save(any(UploadBatch.class));
        verify(modelMapper).map(any(UploadBatch.class), eq(UploadBatchResponseDto.class));
        verifyNoInteractions(videoAbsenceRepository);
    }

    @Test
    void save_ShouldSetDefaultValuesAndSave_WithAbsence() {
        when(userRepository.findById(40L)).thenReturn(Optional.of(user));
        when(trainRepository.findById(30L)).thenReturn(Optional.of(train));
        when(branchRepository.findById(20L)).thenReturn(Optional.of(branch));
        when(uploadBatchRepository.save(any(UploadBatch.class))).thenReturn(uploadBatch);
        when(modelMapper.map(any(UploadBatch.class), eq(UploadBatchResponseDto.class))).thenReturn(responseDto);

        UploadBatchResponseDto result = uploadBatchService.save(createDtoWithAbsence);

        assertNotNull(result);
        verify(videoAbsenceRepository).save(any(VideoAbsence.class));
    }

    @Test
    void getById_ShouldReturnDtoWhenExists() {
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.of(uploadBatch));
        when(modelMapper.map(uploadBatch, UploadBatchResponseDto.class)).thenReturn(responseDto);

        UploadBatchResponseDto result = uploadBatchService.getById(1L);

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
    void updateUploadBatchDto_ShouldUpdateExistingEntity() {
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

        UploadBatchResponseDto result = uploadBatchService.update(1L, updatedDto);

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
        when(uploadBatchRepository.findById(1L)).thenReturn(Optional.empty());

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