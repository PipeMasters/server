package com.pipemasters.server.controller;

import com.pipemasters.server.service.ImotioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/imotio")
public class ImotioWebhookController {

    private final ImotioService imotioService;

    public ImotioWebhookController(ImotioService imotioService) {
        this.imotioService = imotioService;
    }

    @PostMapping("/webhook")
    public void handle(@RequestBody String callId) {
        imotioService.handleImotioWebhook(callId);
    }
}
