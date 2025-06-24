package com.pipemasters.server.kafka.event;

import java.util.UUID;

public record MinioEvent(
        String eventName,
        UUID batchId,
        String filename,
        String rawKey
) {
    public String decodedKey() {
        return batchId + "/" + filename;
    }
}