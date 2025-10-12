package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.MediaFileResponseDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.impl.MediaFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaFileServiceImplTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MediaFileServiceImpl mediaFileService;

    private UUID testUploadBatchDirectory;
    private String testFilename;
    private MediaFile testMediaFile;
    private Long testMediaFileId;

    private Long testUploadBatchId;
    private MediaFile mockMediaFile1;
    private MediaFile mockMediaFile2;
    private MediaFileResponseDto mockDto1;
    private MediaFileResponseDto mockDto2;

    @BeforeEach
    void setUp() {
        testUploadBatchDirectory = UUID.randomUUID();
        testFilename = "test_file.mp4";
        testMediaFileId = 123L;

        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.setDirectory(testUploadBatchDirectory);

        testMediaFile = new MediaFile();
        testMediaFile.setId(testMediaFileId);
        testMediaFile.setFilename(testFilename);
        testMediaFile.setUploadBatch(uploadBatch);
        testMediaFile.setFileType(FileType.VIDEO);
        testMediaFile.setStatus(MediaFileStatus.UPLOADED);
        testMediaFile.setUploadedAt(Instant.now());

        testUploadBatchId = 1L;

        mockMediaFile1 = new MediaFile();
        mockMediaFile1.setId(101L);
        mockMediaFile1.setFilename("file1.mp4");

        mockMediaFile2 = new MediaFile();
        mockMediaFile2.setId(102L);
        mockMediaFile2.setFilename("file2.mp3");

        mockDto1 = new MediaFileResponseDto();
        mockDto1.setId(101L);
        mockDto1.setFilename("file1.mp4");

        mockDto2 = new MediaFileResponseDto();
        mockDto2.setId(102L);
        mockDto2.setFilename("file2.mp3");
    }

    @Test
    void handleS3FileDeletion_shouldDeleteMediaFileWhenFound() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, testUploadBatchDirectory))
                .thenReturn(Optional.of(testMediaFile));

        mediaFileService.handleS3FileDeletion(testUploadBatchDirectory, testFilename);

        verify(mediaFileRepository, times(1)).deleteById(testMediaFileId);
    }

    @Test
    void handleS3FileDeletion_shouldNotDeleteMediaFileWhenNotFound() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, testUploadBatchDirectory))
                .thenReturn(Optional.empty());

        mediaFileService.handleS3FileDeletion(testUploadBatchDirectory, testFilename);
        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleS3FileDeletion_shouldHandleNullUploadBatchDirectory() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, null))
                .thenReturn(Optional.empty());

        mediaFileService.handleS3FileDeletion(null, testFilename);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleS3FileDeletion_shouldHandleNullFilename() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(null, testUploadBatchDirectory))
                .thenReturn(Optional.empty());

        mediaFileService.handleS3FileDeletion(testUploadBatchDirectory, null);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleS3FileDeletion_shouldHandleBothNull() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(null, null))
                .thenReturn(Optional.empty());

        mediaFileService.handleS3FileDeletion(null, null);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void getMediaFilesByUploadBatchId_shouldReturnMappedMediaFiles() {
        List<MediaFile> repoReturnList = Arrays.asList(mockMediaFile1, mockMediaFile2);
        when(mediaFileRepository.findByUploadBatchId(testUploadBatchId))
                .thenReturn(repoReturnList);

        when(modelMapper.map(mockMediaFile1, MediaFileResponseDto.class))
                .thenReturn(mockDto1);
        when(modelMapper.map(mockMediaFile2, MediaFileResponseDto.class))
                .thenReturn(mockDto2);

        List<MediaFileResponseDto> result = mediaFileService.getMediaFilesByUploadBatchId(testUploadBatchId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockDto1, result.get(0));
        assertEquals(mockDto2, result.get(1));
    }

    @Test
    void getMediaFilesByUploadBatchId_shouldReturnEmptyListWhenNoFilesFound() {
        when(mediaFileRepository.findByUploadBatchId(testUploadBatchId))
                .thenReturn(Collections.emptyList());

        List<MediaFileResponseDto> result = mediaFileService.getMediaFilesByUploadBatchId(testUploadBatchId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}