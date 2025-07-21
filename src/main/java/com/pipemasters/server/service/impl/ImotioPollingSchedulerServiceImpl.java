package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.ImotioPollingSchedulerService;
import com.pipemasters.server.service.TranscriptFragmentService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImotioPollingSchedulerServiceImpl implements ImotioPollingSchedulerService {
    // TODO: сейчас метод pollImotioStatuses просто каждые 10 сек постоянно опрашивается.
    //  Надо сделать так, что бы он начинал опрашиваться когда что-то есть и переставал опрашиваться,
    //  когда уже ничего там (в pollingTasks) нету
    //  + надо сделать кастомные исключения

    private final static Logger log = LoggerFactory.getLogger(ImotioPollingSchedulerServiceImpl.class);

    private final MediaFileRepository mediaFileRepository;
    private final TranscriptFragmentService transcriptFragmentService;
    private final ObjectMapper objectMapper;

    @Value("${imotio.api.url}")
    private String imotioApiUrl;

    @Value("${imotio.api.token}")
    private String imotioAuthToken;

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    private final ConcurrentHashMap<String, Long> pollingTasks = new ConcurrentHashMap<>();

    public ImotioPollingSchedulerServiceImpl(MediaFileRepository mediaFileRepository,
                                             TranscriptFragmentService transcriptFragmentService,
                                             ObjectMapper objectMapper,
                                             WebClient.Builder webClientBuilder) {
        this.mediaFileRepository = mediaFileRepository;
        this.transcriptFragmentService = transcriptFragmentService;
        this.objectMapper = objectMapper;
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .baseUrl(imotioApiUrl)
                .build();
    }

    @Override
    public void addTaskToPoll(String imotioId, Long mediaFileId) {
        if (imotioId == null || mediaFileId == null) {
            log.warn("Attempted to add null imotioId or mediaFileId to polling queue. Skipping.");
            return;
        }
        pollingTasks.put(imotioId, mediaFileId);
        log.info("Added Imotio ID {} (MediaFile ID {}) to polling queue. Current queue size: {}",
                imotioId, mediaFileId, pollingTasks.size());
    }

    @Override
    @Scheduled(fixedRate = 10000)
    public void pollImotioStatuses() {
        if (pollingTasks.isEmpty()) {
            log.debug("Imotio polling queue is empty. Skipping status check.");
            return;
        }

        for (String imotioId : pollingTasks.keySet()) {
            Long mediaFileId = pollingTasks.get(imotioId);
            if (mediaFileId == null) {
                log.warn("MediaFile ID for Imotio ID {} was unexpectedly null during polling. Removing from queue.", imotioId);
                pollingTasks.remove(imotioId);
                continue;
            }
            processSingleImotioStatus(imotioId, mediaFileId);
        }
        log.info("Finished Imotio status polling cycle. Current queue size: {}", pollingTasks.size());
    }

    @Override
    public void processSingleImotioStatus(String imotioId, Long mediaFileId) {
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaFileId);
        if (mediaFileOptional.isEmpty()) {
            log.error("MediaFile with ID {} not found in DB during Imotio polling for imotioId {}. Removing from queue.", mediaFileId, imotioId);
            pollingTasks.remove(imotioId);
            return;
        }

        try {
            String statusUrlPath = "/call/" + imotioId + "/status";
            log.debug("Polling Imotio status for MediaFile ID {} (Imotio ID {}). Path: {}", mediaFileId, imotioId, statusUrlPath);

            String responseBody = webClient.get()
                    .uri(statusUrlPath)
                    .header("X-Auth-Token", imotioAuthToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Imotio status check failed for MediaFile ID {} (Imotio ID {}). HTTP Status: {}. Response: {}",
                                                mediaFileId, imotioId, clientResponse.statusCode(), errorBody);
                                        if (errorBody != null && errorBody.contains("\"error\":\"Call not found\"")) {
                                            log.warn("Imotio reported 'Call not found' for MediaFile ID {} (Imotio ID {}). Removing from polling queue permanently.", mediaFileId, imotioId);
                                            pollingTasks.remove(imotioId);
                                        }
                                        return Mono.error(new RuntimeException(
                                                String.format("Imotio status API returned non-200 status for %s: %s - %s",
                                                        imotioId, clientResponse.statusCode(), errorBody)));
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Imotio status API returned an empty or null response.");
            }

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode doneNode = rootNode.get("done");

            if (doneNode != null && doneNode.isInt() && doneNode.asInt() == 1) {
                transcriptFragmentService.fetchFromExternal(mediaFileId, imotioId);

                pollingTasks.remove(imotioId);
            } else {
                log.debug("Imotio processing in progress for MediaFile ID {} (Imotio ID {}). 'done' status: {}.",
                        mediaFileId, imotioId, doneNode != null ? doneNode.asInt() : "N/A");
            }

        } catch (WebClientResponseException e) {
            log.error("WebClient error during Imotio status polling for MediaFile ID {} (Imotio ID {}): {}. Response body: {}",
                    mediaFileId, imotioId, e.getMessage(), e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            log.error("Error parsing Imotio API response JSON for MediaFile ID {} (Imotio ID {}): {}", mediaFileId, imotioId, e.getMessage(), e);
            pollingTasks.remove(imotioId);
        } catch (Exception e) {
            log.error("An unexpected error occurred during Imotio status polling for MediaFile ID {} (Imotio ID {}): {}", mediaFileId, imotioId, e.getMessage(), e);
            pollingTasks.remove(imotioId);
        }
    }
}