package com.pipemasters.server.controller;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Page<UploadBatchDtoSmallResponse>> search(@RequestParam String q,
                                                                    @PageableDefault(size = 15, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageDto<UploadBatchDtoSmallResponse> dtoPage = transcriptService.searchUploadBatches(q, pageable);
        return new ResponseEntity<>(dtoPage.toPage(pageable), HttpStatus.OK);
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