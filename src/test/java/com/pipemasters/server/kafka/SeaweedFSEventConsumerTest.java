package com.pipemasters.server.kafka;

import com.pipemasters.server.kafka.event.SeaweedFSEvent;
import com.pipemasters.server.kafka.handler.SeaweedFSEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import seaweedfs.client.FilerProto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SeaweedFSEventConsumerTest {

    @Mock
    private SeaweedFSEventHandler handler1;
    @Mock
    private SeaweedFSEventHandler handler2;

    private SeaweedFSEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SeaweedFSEventConsumer(List.of(handler1, handler2));
    }

    @Test
    void handle_dispatchesCreateEventToSupportedHandlers() {
        String batchId = UUID.randomUUID().toString();
        String filename = "file.mp4";
        String fullPath = "/some/directory/" + batchId + "/" + filename;
        long fileSize = 1024;
        String eventName = SeaweedFSEvent.OBJECT_CREATED_EVENT;

        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setNewEntry(FilerProto.Entry.newBuilder()
                        .setName(filename)
                        .setIsDirectory(false)
                        .setAttributes(FilerProto.FuseAttributes.newBuilder().setFileSize(fileSize).build())
                        .build())
                .build();

        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        when(handler1.supports(eventName)).thenReturn(true);
        when(handler2.supports(eventName)).thenReturn(false);

        consumer.handle(record);

        ArgumentCaptor<SeaweedFSEvent> captor = ArgumentCaptor.forClass(SeaweedFSEvent.class);
        verify(handler1).handle(captor.capture());
        verify(handler2, never()).handle(any());

        SeaweedFSEvent capturedEvent = captor.getValue();
        assertEquals(eventName, capturedEvent.eventName());
        assertEquals(UUID.fromString(batchId), capturedEvent.batchId());
        assertEquals(filename, capturedEvent.filename());
        assertEquals(fullPath, capturedEvent.fullPath());
        assertEquals(fileSize, capturedEvent.size());
    }

    @Test
    void handle_dispatchesRemoveEventToSupportedHandlers() {
        String batchId = UUID.randomUUID().toString();
        String filename = "video.mkv";
        String fullPath = "/another/path/" + batchId + "/" + filename;
        long fileSize = 2048;
        String eventName = SeaweedFSEvent.OBJECT_REMOVED_EVENT;

        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setOldEntry(FilerProto.Entry.newBuilder()
                        .setName(filename)
                        .setIsDirectory(false)
                        .setAttributes(FilerProto.FuseAttributes.newBuilder().setFileSize(fileSize).build())
                        .build())
                .build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        when(handler1.supports(eventName)).thenReturn(true);

        consumer.handle(record);

        ArgumentCaptor<SeaweedFSEvent> captor = ArgumentCaptor.forClass(SeaweedFSEvent.class);
        verify(handler1).handle(captor.capture());

        SeaweedFSEvent capturedEvent = captor.getValue();
        assertEquals(eventName, capturedEvent.eventName());
        assertEquals(UUID.fromString(batchId), capturedEvent.batchId());
        assertEquals(filename, capturedEvent.filename());
        assertEquals(fullPath, capturedEvent.fullPath());
        assertEquals(fileSize, capturedEvent.size());
    }


    @Test
    void handle_skipsDirectoryCreationEvent() {
        String batchId = UUID.randomUUID().toString();
        String fullPath = "/buckets/" + batchId + "/new_folder";

        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setNewEntry(FilerProto.Entry.newBuilder()
                        .setName("new_folder")
                        .setIsDirectory(true)
                        .build())
                .build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsDirectoryDeletionEvent() {
        String batchId = UUID.randomUUID().toString();
        String fullPath = "/buckets/" + batchId + "/old_folder";

        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setOldEntry(FilerProto.Entry.newBuilder()
                        .setName("old_folder")
                        .setIsDirectory(true)
                        .build())
                .build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }


    @Test
    void handle_skipsRecordWithInvalidUUIDInPath() {
        String fullPath = "/some/directory/not-a-valid-uuid/file.mp4";
        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setNewEntry(FilerProto.Entry.newBuilder().setName("file.mp4").build())
                .build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsRecordWithoutParentDirectoryInPath() {
        String fullPath = "file.mp4";
        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder()
                .setNewEntry(FilerProto.Entry.newBuilder().setName("file.mp4").build())
                .build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsEmptyMessage() {
        String fullPath = "/some/path/file.txt";
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, new byte[0]);

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsNullMessage() {
        String fullPath = "/some/path/file.txt";
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, null);

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsInvalidProtobufMessage() {
        String fullPath = "/some/directory/" + UUID.randomUUID() + "/file.mp4";
        byte[] invalidProtoData = "not a protobuf".getBytes();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, invalidProtoData);

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }

    @Test
    void handle_skipsMessagesWithoutNewOrOldEntry() {
        String fullPath = "/some/directory/" + UUID.randomUUID() + "/file.mp4";
        FilerProto.EventNotification notification = FilerProto.EventNotification.newBuilder().build();
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>("seaweedfs.raw-events", 0, 0, fullPath, notification.toByteArray());

        consumer.handle(record);

        verify(handler1, never()).handle(any());
        verify(handler2, never()).handle(any());
    }
}