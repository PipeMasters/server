package com.pipemasters.server.controller;

import com.pipemasters.server.service.AudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioController {
    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @GetMapping("/process/{mediaFileId}")
    public ResponseEntity<String> processAudio(@PathVariable Long mediaFileId) {
        audioService.extractAudio(mediaFileId);
        return ResponseEntity.ok("Audio processing started for media file ID: " + mediaFileId);

    }
}
