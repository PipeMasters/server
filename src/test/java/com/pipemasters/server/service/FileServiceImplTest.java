package com.pipemasters.server.service;

import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.exceptions.file.FileGenerationException;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private software.amazon.awssdk.services.s3.S3Client s3Client;
    @Mock
    private MediaFileRepository mediaFileRepository;
    @Mock
    private UploadBatchRepository uploadBatchRepository;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(s3Presigner, s3Client, "bucket", mediaFileRepository, uploadBatchRepository);
    }

    @Test
    void getDownloadUrl_ReturnsPresignedUrl() throws Exception {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("http://example.com/file"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        String result = fileService.getDownloadUrl("key");

        assertEquals("http://example.com/file", result);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void generatePresignedDownloadUrl_ReturnsUrl() throws Exception {
        UploadBatch batch = new UploadBatch();
        batch.setDirectory(UUID.randomUUID());
        MediaFile file = new MediaFile();
        file.setUploadBatch(batch);
        file.setFilename("video.mp4");

        when(mediaFileRepository.findById(1L)).thenReturn(Optional.of(file));
        FileServiceImpl spy = spy(fileService);
        doReturn("http://example.com/file").when(spy).getDownloadUrl(any());

        String result = spy.generatePresignedDownloadUrl(1L);

        assertEquals("http://example.com/file", result);
    }

    @Test
    void generatePresignedDownloadUrl_NotFound() {
        when(mediaFileRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(MediaFileNotFoundException.class, () -> fileService.generatePresignedDownloadUrl(1L));
    }

    @Test
    void getDownloadUrl_ExceptionWrapped() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenThrow(new RuntimeException("boom"));
        assertThrows(FileGenerationException.class, () -> fileService.getDownloadUrl("key"));
    }

    @Test
    void deleteMediaFileById_DeletesFromGarageAndDb() {
        UUID dir = UUID.randomUUID();
        UploadBatch batch = new UploadBatch();
        batch.setDirectory(dir);
        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(1L);
        mediaFile.setFilename("test.mp4");
        mediaFile.setUploadBatch(batch);

        when(mediaFileRepository.findById(1L)).thenReturn(Optional.of(mediaFile));

        fileService.deleteMediaFileById(1L);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(mediaFileRepository).deleteById(1L);
    }

    @Test
    void deleteMediaFileById_NotFound() {
        when(mediaFileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MediaFileNotFoundException.class, () -> fileService.deleteMediaFileById(1L));
    }
}
