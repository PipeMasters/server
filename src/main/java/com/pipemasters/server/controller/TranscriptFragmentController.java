package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transcripts")
public class TranscriptFragmentController {

    private final TranscriptFragmentService transcriptService;

    public TranscriptFragmentController(TranscriptFragmentService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q,
                                    @RequestParam(required = false, defaultValue = "true") boolean uploadBatchSearch) {
        if (uploadBatchSearch) {
            List<UploadBatchSearchDto> batches = transcriptService.searchUploadBatches(q);
            return ResponseEntity.ok(batches);
        }
        return ResponseEntity.ok(transcriptService.search(q));
    }

    @GetMapping("/batch/{uploadBatchId}/search")
    public ResponseEntity<List<MediaFileFragmentsDto>> searchByUploadBatch(@PathVariable Long uploadBatchId,
                                                                           @RequestParam String q) {
        return ResponseEntity.ok(transcriptService.searchByUploadBatch(uploadBatchId, q));
    }

    @PostMapping("/media/{mediaFileId}/fetch/{callId}")
    public ResponseEntity<Void> fetchFromExternal(@PathVariable Long mediaFileId,
                                                  @PathVariable String callId) {
        transcriptService.fetchFromExternal(mediaFileId, callId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/media/{mediaFileId}")
    public ResponseEntity<List<SttFragmentDto>> get(@PathVariable Long mediaFileId) {
        return ResponseEntity.ok(transcriptService.getByMediaFile(mediaFileId));
    }
}