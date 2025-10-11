package com.pipemasters.server.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pipemasters.server.kafka.event.SeaweedFSEvent;
import com.pipemasters.server.kafka.handler.SeaweedFSEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seaweedfs.client.FilerProto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeaweedFSEventConsumer {
    private final Logger log = LoggerFactory.getLogger(SeaweedFSEventConsumer.class);
    private final List<SeaweedFSEventHandler> handlers;

    public SeaweedFSEventConsumer(List<SeaweedFSEventHandler> handlers) {
        this.handlers = handlers;
    }

    @KafkaListener(topics = "seaweedfs_filer_events")
    @Transactional
    public void handle(ConsumerRecord<String, byte[]> record) {
        byte[] messageBytes = record.value();
        String fullPath = record.key();

        if (messageBytes == null || messageBytes.length == 0) {
            log.warn("Received empty message from Kafka topic. Skipping. Path: {}", fullPath);
            return;
        }

        try {
            FilerProto.EventNotification notification = FilerProto.EventNotification.parseFrom(messageBytes);

            parseEventAndDispatch(notification, fullPath);

        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse Protobuf message (EventNotification format) for path: {}. Skipping.", fullPath, e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing SeaweedFS event for path: {}. Skipping.", fullPath, e);
        }
    }

    private void parseEventAndDispatch(FilerProto.EventNotification notification, String fullPath) {
        Optional<SeaweedFSEvent> event = extractEventDetails(notification, fullPath)
                .flatMap(details -> extractBatchIdAndCreateEvent(details.fullPath, details.filename, details.size, details.eventName));

        event.ifPresent(this::dispatchEvent);
    }

    private Optional<EventDetails> extractEventDetails(FilerProto.EventNotification notification, String fullPath) {

        FilerProto.Entry entry = null;
        String eventName = null;

        if (notification.hasNewEntry() && !notification.getNewEntry().getIsDirectory()) {
            entry = notification.getNewEntry();
            eventName = SeaweedFSEvent.OBJECT_CREATED_EVENT;
        } else if (notification.hasOldEntry() && !notification.hasNewEntry() && !notification.getOldEntry().getIsDirectory()) {
            entry = notification.getOldEntry();
            eventName = SeaweedFSEvent.OBJECT_REMOVED_EVENT;
        }

        if (entry != null) {
            String filename = entry.getName();
            long size = entry.getAttributes().getFileSize();

            log.info("Parsed event {} for file {} (size: {})", eventName, fullPath, size);
            return Optional.of(new EventDetails(fullPath, filename, size, eventName));
        }

        log.debug("Skipping non-file event for path: {}", fullPath);
        return Optional.empty();
    }

    private Optional<SeaweedFSEvent> extractBatchIdAndCreateEvent(String fullPath, String filename, long size, String eventName) {
        try {
            Path path = Paths.get(fullPath);
            Path parentDir = path.getParent();
            if (parentDir == null) {
                log.warn("Skipped record without parent directory: {}", fullPath);
                return Optional.empty();
            }

            String batchIdString = parentDir.getFileName().toString();
            UUID batchId = UUID.fromString(batchIdString);

            return Optional.of(new SeaweedFSEvent(eventName, batchId, filename, fullPath, size));

        } catch (IllegalArgumentException e) {
            log.warn("Skipped record with invalid UUID in path {}: {}", fullPath, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to extract batchId from path {}", fullPath, e);
            return Optional.empty();
        }
    }

    private void dispatchEvent(SeaweedFSEvent event) {
        log.info("Dispatching event {} for key {}", event.eventName(), event.decodedKey());
        handlers.stream()
                .filter(h -> h.supports(event.eventName()))
                .forEach(h -> h.handle(event));
    }

    private record EventDetails(String fullPath, String filename, long size, String eventName) {}
}