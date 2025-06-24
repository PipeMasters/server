package com.pipemasters.server.service;

import com.pipemasters.server.dto.MediaFileResponseDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.impl.MediaFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaFileServiceTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MediaFileServiceImpl mediaFileService;

    private Long testUploadBatchId;
    private MediaFile mockMediaFile1;
    private MediaFile mockMediaFile2;
    private MediaFileResponseDto mockDto1;
    private MediaFileResponseDto mockDto2;

    @BeforeEach
    void setUp() {
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