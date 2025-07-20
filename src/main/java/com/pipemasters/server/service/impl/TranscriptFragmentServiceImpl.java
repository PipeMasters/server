package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.dto.response.MediaFileFragmentsDto;
import com.pipemasters.server.dto.response.SttFragmentDto;
import com.pipemasters.server.dto.response.UploadBatchSearchDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.TranscriptFragment;
import com.pipemasters.server.exceptions.ServiceUnavailableException;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.TranscriptFragmentRepository;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TranscriptFragmentServiceImpl implements TranscriptFragmentService {

    private final TranscriptFragmentRepository repository;
    private final MediaFileRepository mediaFileRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final CacheManager cacheManager;
    @Value("${imotio.api.token}")
    private final String token;
    private final Logger log = LoggerFactory.getLogger(TranscriptFragmentServiceImpl.class);
    private final ModelMapper modelMapper;

    public TranscriptFragmentServiceImpl(TranscriptFragmentRepository repository,
                                         MediaFileRepository mediaFileRepository, CacheManager cacheManager, String token, ModelMapper modelMapper) {
        this.repository = repository;
        this.mediaFileRepository = mediaFileRepository;
        this.cacheManager = cacheManager;
        this.token = token;
        this.modelMapper = modelMapper;
    }


    @Override
    @Cacheable("transcript_search")
    @Transactional(readOnly = true)
    public List<SttFragmentDto> search(String query) {
        log.debug("Searching transcript fragments with query: {}", query);

        List<TranscriptFragment> fragments = repository.search(query);

        return fragments.stream()
                .map(f -> modelMapper.map(f, SttFragmentDto.class))
                .toList();
    }

    @Override
    @Cacheable("transcript_media_file")
    @Transactional(readOnly = true)
    public List<SttFragmentDto> getByMediaFile(Long mediaFileId) {
        log.debug("Fetching transcript fragments for media file ID: {}", mediaFileId);

        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new MediaFileNotFoundException("Media file not found: " + mediaFileId));

        List<TranscriptFragment> fragments = mediaFile.getTranscriptFragments();

        return fragments.stream()
                .map(f -> modelMapper.map(f, SttFragmentDto.class))
                .toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "transcript_media_file", allEntries = true),
            @CacheEvict(value = "transcript_search", allEntries = true),
            @CacheEvict(value = "upload_batch_search", allEntries = true)
    })
    @Transactional
    public void fetchFromExternal(Long mediaFileId, String callId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> new MediaFileNotFoundException("Media file not found: " + mediaFileId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://test.imot.io/api/call/" + callId + "/stt"))
                .header("X-Auth-Token", token)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ObjectMapper mapper = new ObjectMapper();
            List<SttFragmentDto> dtos = mapper.readValue(body, new TypeReference<List<SttFragmentDto>>() {
            });

            List<TranscriptFragment> existingFragments = mediaFile.getTranscriptFragments();
            Set<String> existingFragmentIds = existingFragments.stream()
                    .map(TranscriptFragment::getFragmentId)
                    .collect(Collectors.toSet());

            List<TranscriptFragment> newFragments = dtos.stream()
                    .filter(d -> !existingFragmentIds.contains(d.getFragment_id()))
                    .map(d -> new TranscriptFragment(d.getBegin(), d.getEnd(), d.getDirection(),
                            d.getText(), d.getFragment_id(), mediaFile))
                    .toList();

            repository.saveAll(newFragments);

            Cache cache = cacheManager.getCache("searchByUploadBatch");
            if (cache != null && mediaFile.getUploadBatch() != null) {
                cache.evict(mediaFile.getUploadBatch().getId());
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceUnavailableException("Failed to fetch transcript", e);
        }
    }

    @Override
    @Cacheable("upload_batch_search")
    @Transactional(readOnly = true)
    public List<UploadBatchSearchDto> searchUploadBatches(String query) {
        var batches = repository.searchUploadBatches(query);
        var fragments = repository.findBatchFragments(query);

        var grouped = fragments.stream().collect(
                java.util.stream.Collectors.groupingBy(
                        TranscriptFragmentRepository.BatchFragmentProjection::getBatchId,
                        java.util.stream.Collectors.groupingBy(
                                TranscriptFragmentRepository.BatchFragmentProjection::getMediaFileId,
                                java.util.stream.Collectors.mapping(
                                        TranscriptFragmentRepository.BatchFragmentProjection::getFragmentId,
                                        java.util.stream.Collectors.toList()))));

        return batches.stream().map(batch -> {
            UploadBatchSearchDto dto = modelMapper.map(batch, UploadBatchSearchDto.class);
            var byFile = grouped.getOrDefault(batch.getId(), java.util.Collections.emptyMap());
            List<MediaFileFragmentsDto> fileDtos = byFile.entrySet().stream()
                    .map(e -> new MediaFileFragmentsDto(e.getKey(), e.getValue()))
                    .toList();
            dto.setFiles(fileDtos);
            return dto;
        }).toList();
    }

    @Override
    @Cacheable(cacheNames = "searchByUploadBatch", key = "#uploadBatchId")
    @Transactional(readOnly = true)
    public List<MediaFileFragmentsDto> searchByUploadBatch(Long uploadBatchId, String query) {
        var fragments = repository.findFragmentsByUploadBatch(uploadBatchId, query);
        var grouped = fragments.stream().collect(
                java.util.stream.Collectors.groupingBy(
                        TranscriptFragmentRepository.MediaFileFragmentProjection::getMediaFileId,
                        java.util.stream.Collectors.mapping(
                                TranscriptFragmentRepository.MediaFileFragmentProjection::getFragmentId,
                                java.util.stream.Collectors.toList())));
        return grouped.entrySet().stream()
                .map(e -> new MediaFileFragmentsDto(e.getKey(), e.getValue()))
                .toList();
    }
}