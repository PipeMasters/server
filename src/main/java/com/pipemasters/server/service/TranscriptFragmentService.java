package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.entity.TranscriptFragment;

import java.util.List;

public interface TranscriptFragmentService {
    List<SttFragmentDto> search(String query);
    List<SttFragmentDto> getByMediaFile(Long mediaFileId);
    List<UploadBatchSearchDto> searchUploadBatches(String query);
    List<MediaFileFragmentsDto> searchByUploadBatch(Long uploadBatchId, String query);
    void fetchFromExternal(Long mediaFileId, String callId);
}