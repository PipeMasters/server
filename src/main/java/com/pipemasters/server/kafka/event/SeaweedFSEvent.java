package com.pipemasters.server.kafka.event;

import java.util.UUID;

public record SeaweedFSEvent(
        String eventName,
        UUID batchId,
        String filename,
        String fullPath,
        Long size
) {
    public static final String OBJECT_CREATED_EVENT = "s3:ObjectCreated:Put";
    public static final String OBJECT_REMOVED_EVENT = "s3:ObjectRemoved:Delete";

    public String decodedKey() {
        return batchId + "/" + filename;
    }
}
