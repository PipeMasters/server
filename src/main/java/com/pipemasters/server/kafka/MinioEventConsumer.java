package com.pipemasters.server.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.MinioEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MinioEventConsumer {
    private final Logger log = LoggerFactory.getLogger(MinioEventConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<MinioEventHandler> handlers;

    public MinioEventConsumer(List<MinioEventHandler> handlers) {
        this.handlers = handlers;
    }

    @KafkaListener(topics = "minio.raw-events")
    @Transactional
    public void handle(String message) throws Exception {
        log.debug("Kafka payload received: {}", message);
        for (MinioEvent event : parse(message)) {
            log.debug("Dispatching event {} for key {}", event.eventName(), event.decodedKey());
            handlers.stream()
                    .filter(h -> h.supports(event.eventName()))
                    .forEach(h -> h.handle(event));
        }
    }

    private List<MinioEvent> parse(String message) throws Exception {
        JsonNode recordsNode = objectMapper.readTree(message).path("Records");
        List<MinioEvent> events = new ArrayList<>();
        if (recordsNode.isArray()) {
            for (JsonNode record : recordsNode) {
                String eventTimeString = record.path("eventTime").asText(null);
                Instant createdAt = Instant.parse(eventTimeString);
                String eventName = record.path("eventName").asText();
                String rawKey = record.path("s3").path("object").path("key").asText();
                String decodedKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
                String[] parts = decodedKey.split("/", 2);
                long size = record.path("s3").path("object").path("size").asLong();
                if (parts.length != 2) {
                    log.warn("Skipped record with unexpected key: {}", rawKey);
                    continue;
                }
                try {
                    events.add(new MinioEvent(eventName, UUID.fromString(parts[0]), parts[1], rawKey, size, createdAt));
                } catch (IllegalArgumentException ex) {
                    log.error("Skipped record with invalid UUID in key {}: {}", rawKey, ex.getMessage());
                }
            }
        } else {
            log.debug("No Records array present in message");
        }
        return events;
    }
}