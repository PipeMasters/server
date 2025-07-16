package com.pipemasters.server.kafka;

import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.AudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//@Service
@Deprecated
public class ProcessingQueueConsumer {
    private final Logger log = LoggerFactory.getLogger(ProcessingQueueConsumer.class);
    private final MediaFileRepository mediaFileRepository;
    private final AudioService audioService;
    private final KafkaProducerService producerService;

    public ProcessingQueueConsumer(MediaFileRepository mediaFileRepository,
                                   AudioService audioService,
                                   KafkaProducerService producerService) {
        this.mediaFileRepository = mediaFileRepository;
        this.audioService = audioService;
        this.producerService = producerService;
    }

//    @KafkaListener(topics = "processing-queue")
//    @Transactional
    public void process(String message) {
        Long id = Long.valueOf(message);
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found: " + id));
        file.setStatus(MediaFileStatus.PROCESSING);
        mediaFileRepository.save(file);
        log.debug("Media file with ID {} status updated to {}", id, file.getStatus());
        log.info("Processing media file with ID: {}", id);
        audioService.extractAudio(id).join();
        file.setStatus(MediaFileStatus.PROCESSED);
        mediaFileRepository.save(file);
        log.debug("Media file with ID {} status updated to {}", id, file.getStatus());
        log.info("Finished processing media file with ID: {}", id);
        producerService.send("processed", id.toString());
    }
}