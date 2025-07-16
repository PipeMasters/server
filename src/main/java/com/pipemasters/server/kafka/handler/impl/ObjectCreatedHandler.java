package com.pipemasters.server.kafka.handler.impl;

import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.MinioEventHandler;
import com.pipemasters.server.repository.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectCreatedHandler implements MinioEventHandler {
    private final static Logger log = LoggerFactory.getLogger(ObjectCreatedHandler.class);

    private final MediaFileRepository repository;
    private final KafkaProducerService producer;

    public ObjectCreatedHandler(MediaFileRepository repository, KafkaProducerService producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Override
    public boolean supports(String eventName) {
        return eventName.startsWith("s3:ObjectCreated:");
    }

    @Override
    public void handle(MinioEvent event) {
        repository.findByFilenameAndUploadBatchDirectory(event.filename(), event.batchId())
                .ifPresentOrElse(file -> {
                    file.setStatus(MediaFileStatus.UPLOADED);
                    repository.save(file);
                    log.debug("Status of file {} set to {}", file.getId(), file.getStatus());
                    if (file.getFileType() == FileType.VIDEO) {
                        log.debug("Video file queued for processing: {}", file.getFilename());
                        producer.send("audio-extraction", file.getUploadBatch().getDirectory() + "/" + file.getFilename());
                    }
                }, () -> log.warn("MediaFile not found for key {}", event.decodedKey()));
    }
}
