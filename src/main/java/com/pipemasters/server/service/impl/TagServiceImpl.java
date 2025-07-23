package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.dto.ImotioTagDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.Tag;
import com.pipemasters.server.entity.TranscriptFragment;
import com.pipemasters.server.exceptions.ServiceUnavailableException;
import com.pipemasters.server.repository.TagRepository;
import com.pipemasters.server.service.TagService;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final static Logger log = LoggerFactory.getLogger(TagServiceImpl.class);
    private final TagRepository tagRepository;
    private final TranscriptFragmentService transcriptFragmentService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    @Value("${imotio.api.url}")
    private String imotioApiUrl;
    @Value("${imotio.api.token}")
    private String imotioAuthToken;

    public TagServiceImpl(TagRepository tagRepository,
                      TranscriptFragmentService transcriptFragmentService,
                      ObjectMapper objectMapper,
                      String imotioApiUrl,
                      String imotioAuthToken) {
        this.tagRepository = tagRepository;
        this.transcriptFragmentService = transcriptFragmentService;
        this.objectMapper = objectMapper;
        this.imotioApiUrl = imotioApiUrl;
        this.imotioAuthToken = imotioAuthToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    @Transactional
    public void fetchAndProcessImotioTags(MediaFile mediaFile, String callId) {
        String tagsUrl = imotioApiUrl + "/call/" + callId + "/tags";
        log.info("Fetching Imotio tags from: {}", tagsUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tagsUrl))
                .header("X-Auth-Token", imotioAuthToken)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                log.error("Failed to fetch Imotio tags for callId {}. Status: {}. Response: {}", callId, response.statusCode(), responseBody);
                throw new ServiceUnavailableException("Failed to fetch Imotio tags. Status: " + response.statusCode());
            }

            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.warn("Imotio tags API returned an empty or null response for callId: {}", callId);
                return;
            }

            List<ImotioTagDto> imotioTags = objectMapper.readValue(responseBody, new TypeReference<List<ImotioTagDto>>() {});
            log.info("Received {} tags from Imotio for MediaFile ID {} (Imotio ID {}).", imotioTags.size(), mediaFile.getId(), callId);

            for (ImotioTagDto imotioTagDto : imotioTags) {
                if ("rule".equalsIgnoreCase(imotioTagDto.getTagType())) {
                    processSingleImotioTag(imotioTagDto, mediaFile);
                } else {
                    log.debug("Skipping tag with name '{}' and type '{}' as it's not 'rule'.", imotioTagDto.getName(), imotioTagDto.getTagType());
                }
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching or parsing Imotio tags for callId {}: {}", callId, e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to fetch Imotio tags: " + e.getMessage(), e);
        }
    }

    private void processSingleImotioTag(ImotioTagDto imotioTagDto, MediaFile mediaFile) {
        String tagName = imotioTagDto.getName();
        String tagValue = imotioTagDto.getValue() != null ? imotioTagDto.getValue() : "";

        Optional<TranscriptFragment> fragmentOptional = transcriptFragmentService.findByImotioFragmentId(imotioTagDto.getFragmentId());

        if (fragmentOptional.isEmpty()) {
            log.warn("TranscriptFragment with Imotio fragment_id '{}' not found for MediaFile ID {}. Cannot link tag '{}' (value: '{}'). This tag might be orphaned.",
                    imotioTagDto.getFragmentId(), mediaFile.getId(), tagName, tagValue);
            return;
        }
        TranscriptFragment transcriptFragment = fragmentOptional.get();

        Optional<Tag> existingTagOptional = tagRepository.findByNameAndValue(tagName, tagValue);

        Tag tag;
        if (existingTagOptional.isPresent()) {
            tag = existingTagOptional.get();
            log.debug("Reusing existing tag '{}' (value: '{}', ID: {}) for fragment {}.", tagName, tagValue, tag.getId(), transcriptFragment.getId());
        } else {
            tag = new Tag(tagName, tagValue, imotioTagDto.getTagType(), mediaFile, transcriptFragment);
            log.info("Creating new tag '{}' (value: '{}', type: {}) for fragment {}.", tagName, tagValue, imotioTagDto.getTagType(), transcriptFragment.getId());
        }

        tag.setMediaFile(mediaFile);
        tag.setTranscriptFragment(transcriptFragment);

        if (!transcriptFragment.getTags().contains(tag)) {
            transcriptFragment.getTags().add(tag);
        }

        tagRepository.save(tag);
    }
}