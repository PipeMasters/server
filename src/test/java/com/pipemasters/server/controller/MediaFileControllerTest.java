package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.MediaFileResponseDto;
import com.pipemasters.server.service.MediaFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaFileControllerTest {

    @Mock
    private MediaFileService mediaFileService;

    @InjectMocks
    private MediaFileController mediaFileController;

    private Long testUploadBatchId;
    private MediaFileResponseDto file1;
    private MediaFileResponseDto file2;

    @BeforeEach
    void setUp() {
        testUploadBatchId = 123L;
        file1 = new MediaFileResponseDto();
        file2 = new MediaFileResponseDto();

         file1.setId(1L);
         file1.setFilename("video1.mp4");
         file2.setId(2L);
         file2.setFilename("video2.mp4");
    }

    @Test
    void getAllMediaFilesByUploadBatch_shouldReturnListOfMediaFiles() {
        List<MediaFileResponseDto> expectedFiles = Arrays.asList(file1, file2);
        when(mediaFileService.getMediaFilesByUploadBatchId(testUploadBatchId))
                .thenReturn(expectedFiles);

        ResponseEntity<List<MediaFileResponseDto>> response =
                mediaFileController.getAllMediaFilesByUploadBatch(testUploadBatchId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedFiles, response.getBody());
    }

    @Test
    void getAllMediaFilesByUploadBatch_shouldReturnEmptyListWhenNoFilesFound() {
        when(mediaFileService.getMediaFilesByUploadBatchId(testUploadBatchId))
                .thenReturn(List.of());

        ResponseEntity<List<MediaFileResponseDto>> response =
                mediaFileController.getAllMediaFilesByUploadBatch(testUploadBatchId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        assertEquals(List.of(), response.getBody());
    }
}