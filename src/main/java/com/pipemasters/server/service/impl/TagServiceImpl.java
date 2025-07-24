package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.dto.ImotioTagDto;
import com.pipemasters.server.dto.response.TagDefinitionResponseDto;
import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.TagType;
import com.pipemasters.server.exceptions.ServiceUnavailableException;
import com.pipemasters.server.repository.TagDefinitionRepository;
import com.pipemasters.server.repository.TagInstanceRepository;
import com.pipemasters.server.service.TagService;
import com.pipemasters.server.service.TranscriptFragmentService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final static Logger log = LoggerFactory.getLogger(TagServiceImpl.class);
    private final TagDefinitionRepository tagDefinitionRepository;
    private final TagInstanceRepository tagInstanceRepository;
    private final TranscriptFragmentService transcriptFragmentService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final HttpClient httpClient;
    @Value("${imotio.api.url}")
    private String imotioApiUrl;
    @Value("${imotio.api.token}")
    private String imotioAuthToken;

    public TagServiceImpl(TagDefinitionRepository tagDefinitionRepository,
                          TagInstanceRepository tagInstanceRepository,
                          TranscriptFragmentService transcriptFragmentService,
                          ObjectMapper objectMapper, ModelMapper modelMapper,
                          String imotioApiUrl,
                          String imotioAuthToken) {
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.tagInstanceRepository = tagInstanceRepository;
        this.transcriptFragmentService = transcriptFragmentService;
        this.objectMapper = objectMapper;
        this.modelMapper = modelMapper;
        this.imotioApiUrl = imotioApiUrl;
        this.imotioAuthToken = imotioAuthToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    @Cacheable(value = "tagDefinitions")
    @Transactional(readOnly = true)
    public List<TagDefinitionResponseDto> getAllTags() {
        return tagDefinitionRepository.findAll().stream().map(t -> modelMapper.map(t, TagDefinitionResponseDto.class)).toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "filteredBatches", allEntries = true),
            @CacheEvict(value = "batches", allEntries = true),
            @CacheEvict(value = "tagDefinitions", allEntries = true)
    })
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
        TagType tagType = TagType.valueOf(imotioTagDto.getTagType().toUpperCase());

        TagDefinition definition = tagDefinitionRepository.findByNameAndType(tagName, tagType)
                .orElseGet(() -> {
                    log.info("Creating new TagDefinition: name='{}', type='{}'", tagName, tagType);
                    return tagDefinitionRepository.save(new TagDefinition(tagName, tagType));
                });

        Optional<TranscriptFragment> fragmentOptional = transcriptFragmentService.findByImotioFragmentId(imotioTagDto.getFragmentId());

        if (fragmentOptional.isEmpty()) {
            log.warn("TranscriptFragment with Imotio fragment_id '{}' not found for MediaFile ID {}. Cannot link tag instance for definition '{}' (value: '{}').",
                    imotioTagDto.getFragmentId(), mediaFile.getId(), tagName, tagValue);
            return;
        }
        TranscriptFragment transcriptFragment = fragmentOptional.get();

        Optional<TagInstance> existingInstanceOptional = tagInstanceRepository.findByDefinitionAndFragmentAndBeginTimeAndEndTimeAndValue(
                definition,
                transcriptFragment,
                imotioTagDto.getBegin(),
                imotioTagDto.getEnd(),
                tagValue
        );

        if (existingInstanceOptional.isPresent()) {
            log.debug("Reusing existing TagInstance for definition '{}' (value: '{}') on fragment {} (ID: {}).",
                    tagName, tagValue, transcriptFragment.getId(), existingInstanceOptional.get().getId());
        } else {
            log.info("Creating new TagInstance for definition '{}' (value: '{}') on fragment {} (ID: {}).",
                    tagName, tagValue, transcriptFragment.getId(), definition.getId());

            TagInstance newInstance = new TagInstance(
                    imotioTagDto.getBegin(),
                    imotioTagDto.getEnd(),
                    imotioTagDto.getMatchText(),
                    tagValue,
                    definition,
                    transcriptFragment,
                    mediaFile
            );
            tagInstanceRepository.save(newInstance);
        }
    }
}