package com.pipemasters.server.service;

import com.pipemasters.server.dto.PageDto;
import com.pipemasters.server.dto.UploadBatchDtoSmallResponse;
import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.entity.TranscriptFragment;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TranscriptFragmentService {
    List<SttFragmentDto> search(String query);
    List<SttFragmentDto> getByMediaFile(Long mediaFileId);
    Optional<TranscriptFragment> findByImotioFragmentId(String imotioFragmentId);
    PageDto<UploadBatchDtoSmallResponse> searchUploadBatches(String query, Pageable pageable);
    List<MediaFileFragmentsDto> searchByUploadBatch(Long uploadBatchId, String query);
    void fetchFromExternal(Long mediaFileId, String callId);
}