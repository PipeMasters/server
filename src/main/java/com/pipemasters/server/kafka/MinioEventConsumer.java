package com.pipemasters.server.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.MediaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
public class MinioEventConsumer {
    private final Logger log = LoggerFactory.getLogger(MinioEventConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MediaFileRepository mediaFileRepository;
    private final KafkaProducerService producerService;
    private final MediaFileService mediaFileService;

    public MinioEventConsumer(MediaFileRepository mediaFileRepository, KafkaProducerService producerService, MediaFileService mediaFileService) {
        this.mediaFileRepository = mediaFileRepository;
        this.producerService = producerService;
        this.mediaFileService = mediaFileService;
    }

    @KafkaListener(topics = "minio.raw-events")
    @Transactional
    public void handle(String message) throws Exception {
        JsonNode root = objectMapper.readTree(message);
        JsonNode records = root.path("Records");
        log.debug("Received Minio event: {}", root.toPrettyString());
        if (records.isArray()) {
            for (JsonNode record : records) {
                String eventName = record.path("eventName").asText();
                String key = record.path("s3").path("object").path("key").asText();
                String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
                String[] parts = decodedKey.split("/", 2);

                if (parts.length != 2) {
                    log.warn("Invalid key format for Minio event: {}", key);
                    continue;
                }
                String batch = parts[0];
                String filename = parts[1];
                UUID uploadBatchDirectory;
                try {
                    uploadBatchDirectory = UUID.fromString(batch);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID format '{}' from Minio event key: {}. Skipping event.", batch, key, e);
                    continue;
                }

                if (eventName.startsWith("s3:ObjectCreated:")) {
                    log.debug("Handling Minio object creation event for key: {}", key);
                    Optional<MediaFile> opt = mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, uploadBatchDirectory);
                    if (opt.isPresent()) {
                        MediaFile file = opt.get();
                        file.setStatus(MediaFileStatus.UPLOADED);
                        mediaFileRepository.save(file);
                        log.debug("Media file with ID {} status updated to {}", file.getId(), file.getStatus());
                        if (file.getFileType() == FileType.VIDEO) {
                            log.debug("Video file uploaded: {}", file.getFilename());
                            producerService.send("processing-queue", file.getId().toString());
                        }
                    } else {
                        log.warn("MediaFile not found for key {}. This might indicate a race condition or an out-of-sync state.", key);
                    }
                } else if (eventName.startsWith("s3:ObjectRemoved:")) {
                    log.debug("Handling Minio object removal event for key: {}", key);
                    try {
                        mediaFileService.handleMinioFileDeletion(uploadBatchDirectory, filename);
                    } catch (Exception e) {
                        log.error("Error processing Minio object removal event for key {}: {}", key, e.getMessage(), e);
                    }
                } else {
                    log.debug("Unhandled Minio event type: {}", eventName);
                }
            }
        } else {
            log.debug("No 'Records' field in Kafka payload for Minio event. Payload: {}", root.toPrettyString());
        }
    }
}
