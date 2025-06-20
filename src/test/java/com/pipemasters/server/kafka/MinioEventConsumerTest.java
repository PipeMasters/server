package com.pipemasters.server.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.MinioEventConsumer;
import com.pipemasters.server.repository.MediaFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MinioEventConsumerTest {

    private MediaFileRepository mediaFileRepository;
    private KafkaProducerService producerService;
    private MinioEventConsumer consumer;

    @BeforeEach
    void setUp() {
        mediaFileRepository = mock(MediaFileRepository.class);
        producerService = mock(KafkaProducerService.class);
        consumer = new MinioEventConsumer(mediaFileRepository, producerService);
    }

    @Test
    void handle_shouldUpdateStatusAndSendToProcessingQueue_whenVideoUploaded() throws Exception {
        UUID batchId = UUID.randomUUID();
        String filename = "video.mp4";
        String key = batchId + "/" + filename;
        String message = "{ \"Records\": [ { \"eventName\": \"s3:ObjectCreated:Put\", \"s3\": { \"object\": { \"key\": \"" + key + "\" } } } ] }";

        MediaFile file = new MediaFile();
        file.setId(42L);
        file.setFilename(filename);
        file.setFileType(FileType.VIDEO);

        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.of(file));

        consumer.handle(message);

        verify(mediaFileRepository).save(file);
        verify(producerService).send("processing-queue", "42");
        assertEquals(MediaFileStatus.UPLOADED, file.getStatus());
    }

    @Test
    void handle_shouldNotThrow_whenNoMatchingFile() throws Exception {
        UUID batchId = UUID.randomUUID();
        String filename = "notfound.mp4";
        String key = batchId + "/" + filename;
        String message = "{ \"Records\": [ { \"eventName\": \"s3:ObjectCreated:Put\", \"s3\": { \"object\": { \"key\": \"" + key + "\" } } } ] }";

        when(mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, batchId)).thenReturn(Optional.empty());

        consumer.handle(message);

        verify(mediaFileRepository, never()).save(any());
        verify(producerService, never()).send(anyString(), anyString());
    }
}