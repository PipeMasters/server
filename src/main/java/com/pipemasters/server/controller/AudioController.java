package com.pipemasters.server.controller;

import com.pipemasters.server.service.AudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioController {
    private final AudioService audioService;
    private final Logger log = LoggerFactory.getLogger(AudioController.class);
    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @GetMapping("/process/{mediaFileId}")
    public ResponseEntity<String> processAudio(@PathVariable Long mediaFileId) {
        audioService.extractAudio(mediaFileId);
        return ResponseEntity.ok("Audio processing started for media file ID: " + mediaFileId);

    }

//    @PostMapping("/minio-event")
    public ResponseEntity<Void> handleMinioEvent(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String auth) {

        log.debug("Received MinIO event: {}", payload);

        if (auth != null && !"Bearer my-super-secret".equals(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Map<String, Object>> records = (List<Map<String, Object>>) payload.get("Records");
        if (records == null) {
            log.warn("No Records field in webhook payload");
            return ResponseEntity.badRequest().build();
        }

        for (Map<String, Object> record : records) {
            String eventName = (String) record.get("eventName");
            if (eventName == null || !eventName.startsWith("s3:ObjectCreated")) {
                log.debug("Skip event {}", eventName);
                continue;
            }

            Map<String, Object> s3      = (Map<String, Object>) record.get("s3");
            Map<String, Object> object  = (Map<String, Object>) s3.get("object");
            String encodedKey           = (String) object.get("key");

            String key = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);

            String[] parts = key.split("/", 2);
            if (parts.length != 2) {
                log.warn("Unexpected key format: {}", key);
                continue;
            }

            String uuid     = parts[0];
            String filename = parts[1];

            if (!filename.endsWith(".mp4") && !filename.endsWith(".mkv") && !filename.endsWith(".avi")) {
                log.debug("Skip non-video file: {}", filename);
                continue;
            }

            log.info("Uploaded file {} in dir {}", filename, uuid);
            try {
                log.debug("Processing uploaded file: {}", filename);
                audioService.processUploadedVideo(uuid, filename);
            } catch (Exception ex) {
                log.error("Error while processing {}: {}", filename, ex.getMessage(), ex);
            }
        }
        return ResponseEntity.ok().build();
    }
}
