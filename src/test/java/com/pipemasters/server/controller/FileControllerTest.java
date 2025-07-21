package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.FileUploadRequestDto;
import com.pipemasters.server.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @Test
    void getPresignedUploadUrlForVideo_ReturnsUrl() {
        String expectedUrl = "http://example.com/video";
        when(fileService.generatePresignedUploadUrlForVideo(any(FileUploadRequestDto.class)))
                .thenReturn(expectedUrl);

        FileUploadRequestDto dto = new FileUploadRequestDto();
        ResponseEntity<String> response = fileController.getPresignedUploadUrlForVideo(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUrl, response.getBody());
        verify(fileService).generatePresignedUploadUrlForVideo(dto);
    }

    @Test
    void getPresignedUploadUrlForAudio_ReturnsUrl() {
        String expectedUrl = "http://example.com/audio";
        when(fileService.generatePresignedUploadUrlForAudio("dir/file.mp4",null,null,null))
                .thenReturn(expectedUrl);

        ResponseEntity<String> response = fileController.getPresignedUploadUrlForAudio("dir/file.mp4",null,null,null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUrl, response.getBody());
        verify(fileService).generatePresignedUploadUrlForAudio("dir/file.mp4",null,null,null);
    }

    @Test
    void getPresignedDownloadUrl_ByMediaFileId() {
        String expectedUrl = "http://example.com/download";
        when(fileService.generatePresignedDownloadUrl(1L)).thenReturn(expectedUrl);

        ResponseEntity<String> response = fileController.getPresignedDownloadUrl(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUrl, response.getBody());
        verify(fileService).generatePresignedDownloadUrl(1L);
    }

    @Test
    void getPresignedDownloadUrl_BySourceKey() {
        String expectedUrl = "http://example.com/download";
        when(fileService.getDownloadUrl("dir/file.mp4")).thenReturn(expectedUrl);

        ResponseEntity<String> response = fileController.getPresignedDownloadUrl(null, "dir/file.mp4");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUrl, response.getBody());
        verify(fileService).getDownloadUrl("dir/file.mp4");
    }

    @Test
    void getPresignedDownloadUrl_InvalidParams() {
        assertThrows(IllegalArgumentException.class, () ->
                fileController.getPresignedDownloadUrl(null, null));
        assertThrows(IllegalArgumentException.class, () ->
                fileController.getPresignedDownloadUrl(1L, "key"));
    }
}