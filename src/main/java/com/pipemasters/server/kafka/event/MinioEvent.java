package com.pipemasters.server.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MinioEvent(
        String eventName,
        UUID batchId,
        String filename,
        String rawKey,
        Long size,
        Instant createdAt
) {
    public String decodedKey() {
        return batchId + "/" + filename;
    }
}