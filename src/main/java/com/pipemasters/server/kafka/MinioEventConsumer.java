package com.pipemasters.server.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.repository.MediaFileRepository;
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

    public MinioEventConsumer(MediaFileRepository mediaFileRepository, KafkaProducerService producerService) {
        this.mediaFileRepository = mediaFileRepository;
        this.producerService = producerService;
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
                if (eventName.startsWith("s3:ObjectCreated:")) {
                    String key = record.path("s3").path("object").path("key").asText();
                    String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);
                    String[] parts = decodedKey.split("/", 2);
                    if (parts.length != 2) continue;
                    String batch = parts[0];
                    String filename = parts[1];
                    log.debug("Looking for file with filename: {} and batch: {}", filename, batch);
                    Optional<MediaFile> opt = mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, UUID.fromString(batch));
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
                        log.warn("MediaFile not found for key {}", key);
                    }
                }
            }
        } else log.debug("No Records field in kafka payload");
    }
}