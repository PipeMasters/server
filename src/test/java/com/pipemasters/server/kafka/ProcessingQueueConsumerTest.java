package com.pipemasters.server.kafka;

import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.ProcessingQueueConsumer;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.AudioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProcessingQueueConsumerTest {

    private MediaFileRepository mediaFileRepository;
    private AudioService audioService;
    private KafkaProducerService producerService;
    private ProcessingQueueConsumer consumer;

    @BeforeEach
    void setUp() {
        mediaFileRepository = mock(MediaFileRepository.class);
        audioService = mock(AudioService.class);
        producerService = mock(KafkaProducerService.class);
        consumer = new ProcessingQueueConsumer(mediaFileRepository, audioService, producerService);
    }

    @Test
    void process_shouldUpdateStatusAndCallAudioService() {
        Long id = 1L;
        MediaFile file = new MediaFile();
        file.setId(id);
        file.setStatus(MediaFileStatus.PENDING);

        when(mediaFileRepository.findById(id)).thenReturn(Optional.of(file));
        when(audioService.extractAudio(id)).thenReturn(CompletableFuture.completedFuture("done"));

        consumer.process(id.toString());

        verify(mediaFileRepository, times(2)).save(file);
        verify(audioService).extractAudio(id);
        verify(producerService).send("processed", id.toString());
        assertEquals(MediaFileStatus.PROCESSED, file.getStatus());
    }
}