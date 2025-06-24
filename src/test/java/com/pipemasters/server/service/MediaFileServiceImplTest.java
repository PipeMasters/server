package com.pipemasters.server.service;

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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaFileServiceImplTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @InjectMocks
    private MediaFileServiceImpl mediaFileService;

    private UUID testUploadBatchDirectory;
    private String testFilename;
    private MediaFile testMediaFile;
    private Long testMediaFileId;

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
    }

    @Test
    void handleMinioFileDeletion_shouldDeleteMediaFileWhenFound() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, testUploadBatchDirectory))
                .thenReturn(Optional.of(testMediaFile));

        mediaFileService.handleMinioFileDeletion(testUploadBatchDirectory, testFilename);

        verify(mediaFileRepository, times(1)).deleteById(testMediaFileId);
    }

    @Test
    void handleMinioFileDeletion_shouldNotDeleteMediaFileWhenNotFound() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, testUploadBatchDirectory))
                .thenReturn(Optional.empty());

        mediaFileService.handleMinioFileDeletion(testUploadBatchDirectory, testFilename);
        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleMinioFileDeletion_shouldHandleNullUploadBatchDirectory() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(testFilename, null))
                .thenReturn(Optional.empty());

        mediaFileService.handleMinioFileDeletion(null, testFilename);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleMinioFileDeletion_shouldHandleNullFilename() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(null, testUploadBatchDirectory))
                .thenReturn(Optional.empty());

        mediaFileService.handleMinioFileDeletion(testUploadBatchDirectory, null);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }

    @Test
    void handleMinioFileDeletion_shouldHandleBothNull() {
        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(null, null))
                .thenReturn(Optional.empty());

        mediaFileService.handleMinioFileDeletion(null, null);

        verify(mediaFileRepository, never()).deleteById(anyLong());
    }
}