package com.pipemasters.server.kafka.handler;

import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.impl.ObjectCreatedHandler;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.ImotioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ObjectCreatedHandlerTest {

    private MediaFileRepository repository;
    private KafkaProducerService producer;
    private ImotioService imotioService;
    private ObjectCreatedHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(MediaFileRepository.class);
        producer = mock(KafkaProducerService.class);
        imotioService = mock(ImotioService.class);
        handler = new ObjectCreatedHandler(repository, producer, imotioService);
    }

    @Test
    void supports_returnsTrueForObjectCreatedEvents() {
        assertTrue(handler.supports("s3:ObjectCreated:Put"));
        assertTrue(handler.supports("s3:ObjectCreated:Post"));
        assertFalse(handler.supports("s3:ObjectRemoved:Delete"));
    }

    @Test
    void handle_updatesFileStatusAndSendsToProcessingQueueForVideo() {
        UUID batchId = UUID.randomUUID();
        String filename = "video.mp4";
        String rawKey = batchId + "/" + filename;
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey, null);
        MediaFile file = new MediaFile();
        file.setId(1L);
        file.setFileType(FileType.VIDEO);
        file.setFilename("audio.mp3");

        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.setDirectory(batchId);
        file.setUploadBatch(uploadBatch);

        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.of(file));

        handler.handle(event);

        assertEquals(MediaFileStatus.UPLOADED, file.getStatus());
        verify(repository).save(file);
        verify(producer).send("audio-extraction", batchId + "/" + "audio.mp3");
    }

    @Test
    void handle_updatesFileStatusWithoutSendingToQueueForNonVideo() {
        UUID batchId = UUID.randomUUID();
        String filename = "image.jpg";
        String rawKey = batchId + "/" + filename;
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey, null);
        MediaFile file = new MediaFile();
        file.setId(2L);
        file.setFileType(FileType.IMAGE);

        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.of(file));

        handler.handle(event);

        assertEquals(MediaFileStatus.UPLOADED, file.getStatus());
        verify(repository).save(file);
        verify(producer, never()).send(anyString(), anyString());
    }

    @Test
    void handle_logsWarningWhenFileNotFound() {
        UUID batchId = UUID.randomUUID();
        String filename = "missing.mp4";
        String rawKey = batchId + "/" + filename;
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey, null);

        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.empty());

        handler.handle(event);

        verify(repository, never()).save(any());
        verify(producer, never()).send(anyString(), anyString());
    }

//    @Test
//    @Disabled
//    @Deprecated
//    void handle_updatesFileStatusAndCallsImotioServiceForAudioWhenEnabled() {
//        UUID batchId = UUID.randomUUID();
//        String filename = "audio.mp3";
//        String rawKey = batchId + "/" + filename;
//        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey, null);
//        MediaFile file = new MediaFile();
//        file.setId(3L);
//        file.setFileType(FileType.AUDIO);
//        file.setFilename(filename);
//
//        UploadBatch uploadBatch = new UploadBatch();
//        uploadBatch.setDirectory(batchId);
//        file.setUploadBatch(uploadBatch);
//
//        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.of(file));
//        when(imotioService.isImotioIntegrationEnabled()).thenReturn(true);
//
//        handler.handle(event);
//
//        assertEquals(MediaFileStatus.UPLOADED, file.getStatus());
//        verify(repository).save(file);
//        verify(imotioService).isImotioIntegrationEnabled();
//        verify(imotioService).processImotioFileUpload(file.getId());
//        verify(producer, never()).send(anyString(), anyString());
//    }
//
//    @Test
//    @Disabled
//    @Deprecated
//    void handle_updatesFileStatusAndDoesNotCallImotioServiceForAudioWhenDisabled() {
//        UUID batchId = UUID.randomUUID();
//        String filename = "audio.wav";
//        String rawKey = batchId + "/" + filename;
//        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey, null);
//        MediaFile file = new MediaFile();
//        file.setId(4L);
//        file.setFileType(FileType.AUDIO);
//        file.setFilename(filename);
//
//        UploadBatch uploadBatch = new UploadBatch();
//        uploadBatch.setDirectory(batchId);
//        file.setUploadBatch(uploadBatch);
//
//        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.of(file));
//        when(imotioService.isImotioIntegrationEnabled()).thenReturn(false);
//
//        handler.handle(event);
//
//        assertEquals(MediaFileStatus.UPLOADED, file.getStatus());
//        verify(repository).save(file);
//        verify(imotioService).isImotioIntegrationEnabled();
//        verify(imotioService, never()).processImotioFileUpload(anyLong());
//        verify(producer, never()).send(anyString(), anyString());
//    }
}