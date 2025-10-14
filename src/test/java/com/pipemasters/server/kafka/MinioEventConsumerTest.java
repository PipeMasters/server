package com.pipemasters.server.kafka;

import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.MinioEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MinioEventConsumerTest {

    private MinioEventHandler handler1;
    private MinioEventHandler handler2;
    private MinioEventConsumer consumer;

    @BeforeEach
    void setUp() {
        handler1 = mock(MinioEventHandler.class);
        handler2 = mock(MinioEventHandler.class);
        consumer = new MinioEventConsumer(List.of(handler1, handler2));
    }

    @Test
    void handle_dispatchesToSupportedHandlers() throws Exception {
        String batchId = UUID.randomUUID().toString();
        String filename = "file.mp4";
        String eventName = "s3:ObjectCreated:Put";
        String key = batchId + "/" + filename;
        long size = 12345L;
        String eventTime = "2023-10-27T10:30:00Z";

        String message = String.format("""
                {
                  "Records": [
                    {
                      "eventName": "%s",
                      "eventTime": "%s",
                      "s3": {
                        "object": {
                          "key": "%s",
                          "size": %d
                        }
                      }
                    }
                  ]
                }
                """, eventName, eventTime, key, size);


        when(handler1.supports(eventName)).thenReturn(true);
        when(handler2.supports(eventName)).thenReturn(false);

        consumer.handle(message);

        ArgumentCaptor<MinioEvent> captor = ArgumentCaptor.forClass(MinioEvent.class);
        verify(handler1).handle(captor.capture());

        assertEquals(eventName, captor.getValue().eventName());
        assertEquals(UUID.fromString(batchId), captor.getValue().batchId());
        assertEquals(filename, captor.getValue().filename());
        assertEquals(size, captor.getValue().size());
        assertEquals(Instant.parse(eventTime), captor.getValue().createdAt());

        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsRecordsWithInvalidKeyFormat() throws Exception {
        String message = """
                { "Records": [ { 
                    "eventName": "s3:ObjectCreated:Put", 
                    "eventTime": "2023-10-27T10:30:00Z",
                    "s3": { "object": { "key": "not-a-uuid-and-no-slash", "size": 100 } } 
                } ] }
                """;
        consumer.handle(message);
        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsRecordsWithInvalidUUID() throws Exception {
        String message = """
                { "Records": [ { 
                    "eventName": "s3:ObjectCreated:Put", 
                    "eventTime": "2023-10-27T10:30:00Z",
                    "s3": { "object": { "key": "notauuid/file.mp4", "size": 100 } } 
                } ] }
                """;
        consumer.handle(message);
        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_noRecordsArray_doesNothing() throws Exception {
        String message = "{ \"foo\": \"bar\" }";
        consumer.handle(message);
        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_multipleRecords_dispatchesEach() throws Exception {
        String batchId1 = UUID.randomUUID().toString();
        String batchId2 = UUID.randomUUID().toString();
        String eventName = "s3:ObjectCreated:Put";
        String eventTime = "2023-10-27T10:30:00Z";
        String key1 = batchId1 + "/file1.mp4";
        String key2 = batchId2 + "/file2.mp4";

        String message = String.format("""
                {
                  "Records": [
                    {
                      "eventName": "%s",
                      "eventTime": "%s",
                      "s3": { "object": { "key": "%s", "size": 100 } }
                    },
                    {
                      "eventName": "%s",
                      "eventTime": "%s",
                      "s3": { "object": { "key": "%s", "size": 200 } }
                    }
                  ]
                }
                """, eventName, eventTime, key1, eventName, eventTime, key2);

        when(handler1.supports(eventName)).thenReturn(true);

        consumer.handle(message);

        verify(handler1, times(2)).handle(any(MinioEvent.class));
    }
}