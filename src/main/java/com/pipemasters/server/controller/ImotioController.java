package com.pipemasters.server.controller;

import com.pipemasters.server.service.ImotioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/imotio")
public class ImotioController {

    private final ImotioService imotioService;

    public ImotioController(ImotioService imotioService) {
        this.imotioService = imotioService;
    }

    @PostMapping("/webhook")
    public void handle(@RequestBody String callId) {
        imotioService.handleImotioWebhook(callId);
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> getImotioIntegrationStatus() {
        return ResponseEntity.ok(imotioService.isImotioIntegrationEnabled());
    }

    @PostMapping("/status/{newStatus}")
    public ResponseEntity<Boolean> setImotioIntegrationStatus(@PathVariable Boolean newStatus) {
        imotioService.setImotioIntegrationEnabled(newStatus);
        return ResponseEntity.ok(imotioService.isImotioIntegrationEnabled());
    }
}
