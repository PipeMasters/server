package com.pipemasters.server.kafka.handler;

import com.pipemasters.server.kafka.event.SeaweedFSEvent;

public interface SeaweedFSEventHandler {
    boolean supports(String eventName);

    void handle(SeaweedFSEvent event);
}