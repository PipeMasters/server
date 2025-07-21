package com.pipemasters.server.kafka.handler;

import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.impl.ObjectRemovedHandler;
import com.pipemasters.server.service.MediaFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectRemovedHandlerTest {

    private MediaFileService mediaFileService;
    private ObjectRemovedHandler handler;

    @BeforeEach
    void setUp() {
        mediaFileService = mock(MediaFileService.class);
        handler = new ObjectRemovedHandler(mediaFileService);
    }

    @Test
    void supports_returnsTrueForObjectRemovedEvents() {
        assertTrue(handler.supports("s3:ObjectRemoved:Delete"));
        assertTrue(handler.supports("s3:ObjectRemoved:SomethingElse"));
        assertFalse(handler.supports("s3:ObjectCreated:Put"));
    }

    @Test
    void handle_callsMediaFileServiceForValidEvent() {
        MinioEvent event = new MinioEvent("s3:ObjectRemoved:Delete", UUID.randomUUID(), "file.mp4", "rawKey", 12345L);

        handler.handle(event);

        verify(mediaFileService).handleMinioFileDeletion(event.batchId(), event.filename());
    }

    @Test
    void handle_logsErrorWhenMediaFileServiceThrowsException() {
        MinioEvent event = new MinioEvent("s3:ObjectRemoved:Delete", UUID.randomUUID(), "file.mp4", "rawKey", 12345L);
        doThrow(new RuntimeException("Service error")).when(mediaFileService).handleMinioFileDeletion(any(), any());

        handler.handle(event);

        verify(mediaFileService).handleMinioFileDeletion(event.batchId(), event.filename());
    }
}