package com.pipemasters.server.kafka.handler.impl;

import com.pipemasters.server.kafka.event.SeaweedFSEvent;
import com.pipemasters.server.kafka.handler.SeaweedFSEventHandler;
import com.pipemasters.server.service.MediaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectRemovedHandler implements SeaweedFSEventHandler {
    private final static Logger log = LoggerFactory.getLogger(ObjectRemovedHandler.class);

    private final MediaFileService mediaFileService;

    public ObjectRemovedHandler(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    @Override
    public boolean supports(String eventName) {
        return eventName.startsWith("s3:ObjectRemoved:");
    }

    @Override
    public void handle(SeaweedFSEvent event) {
        log.debug("Handling removal for key {}", event.decodedKey());
        try {
            mediaFileService.handleMinioFileDeletion(event.batchId(), event.filename());
        } catch (Exception e) {
            log.error("Error processing removal for key {}: {}", event.decodedKey(), e.getMessage(), e);
        }
    }
}