package com.pipemasters.server.kafka.handler;

import com.pipemasters.server.kafka.event.MinioEvent;

public interface MinioEventHandler {
    boolean supports(String eventName);

    void handle(MinioEvent event);
}