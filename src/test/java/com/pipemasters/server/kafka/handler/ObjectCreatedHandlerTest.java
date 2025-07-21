package com.pipemasters.server.kafka.handler;

import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.impl.ObjectCreatedHandler;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.service.ImotioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectCreatedHandlerTest {

    private MediaFileRepository repository;
    private KafkaProducerService producer;
    private ObjectCreatedHandler handler;
    private ImotioService imotioService;

    @BeforeEach
    void setUp() {
        repository = mock(MediaFileRepository.class);
        producer = mock(KafkaProducerService.class);
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
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey);
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
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey);
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
        MinioEvent event = new MinioEvent("s3:ObjectCreated:Put", batchId, filename, rawKey);

        when(repository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.empty());

        handler.handle(event);

        verify(repository, never()).save(any());
        verify(producer, never()).send(anyString(), anyString());
    }
}