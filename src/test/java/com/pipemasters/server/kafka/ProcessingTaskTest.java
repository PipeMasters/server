package com.pipemasters.server.kafka;

import com.pipemasters.server.kafka.ProcessingTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingTaskTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ProcessingTask task = new ProcessingTask();
        task.setMediaFileId(123L);
        assertEquals(123L, task.getMediaFileId());
    }

    @Test
    void testAllArgsConstructor() {
        ProcessingTask task = new ProcessingTask(456L);
        assertEquals(456L, task.getMediaFileId());
    }
}