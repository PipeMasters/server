package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.exceptions.file.MediaFileProcessingException;
import com.pipemasters.server.exceptions.imotio.ImotioApiCallException;
import com.pipemasters.server.exceptions.imotio.ImotioProcessingException;
import com.pipemasters.server.exceptions.imotio.ImotioResponseParseException;
import com.pipemasters.server.service.ImotioPollingSchedulerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.exceptions.file.MediaFileNotFoundException;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.FileService;
import com.pipemasters.server.service.ImotioService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ImotioServiceImpl implements ImotioService {

    private final static Logger log = LoggerFactory.getLogger(ImotioServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final MediaFileRepository mediaFileRepository;
    private final FileService fileService;
    private final ImotioPollingSchedulerService imotioPollingSchedulerService;

    @Value("${imotio.api.url}")
    private String imotioApiUrl;

    @Value("${imotio.api.token}")
    private String imotioAuthToken;

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    public ImotioServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                             MediaFileRepository mediaFileRepository, FileService fileService, ImotioPollingSchedulerServiceImpl imotioPollingSchedulerService) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.mediaFileRepository = mediaFileRepository;
        this.fileService = fileService;
        this.imotioPollingSchedulerService = imotioPollingSchedulerService;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .baseUrl(imotioApiUrl)
                .build();
    }

    @Override
    @Transactional
    public void processImotioFileUpload(Long mediaFileId) {
        log.info("Attempting to process Imotio file upload for MediaFile ID: {}.", mediaFileId);
        MediaFile mediaFile = null;

        try {
            Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaFileId);
            if (mediaFileOptional.isEmpty()) {
                throw new MediaFileNotFoundException("MediaFile with ID " + mediaFileId + " not found.");
            }
            mediaFile = mediaFileOptional.get();

            mediaFile.setStatus(MediaFileStatus.PROCESSING);
            mediaFileRepository.save(mediaFile);

            if (!isAudioFileSuitableForImotio(mediaFile)) {
                throw new MediaFileProcessingException("MediaFile " + mediaFile.getId() + " is not a suitable audio file for Imotio.");
            }

            UploadBatch uploadBatch = mediaFile.getUploadBatch();
            if (uploadBatch == null) {
                throw new MediaFileProcessingException("MediaFile " + mediaFile.getId() + " has no associated UploadBatch.");
            }
            User uploadedBy = uploadBatch.getUploadedBy();
            if (uploadedBy == null) {
                throw new MediaFileProcessingException("UploadBatch " + uploadBatch.getId() + " has no associated User.");
            }

            String stereoUrl;
            try {
                stereoUrl = fileService.generatePresignedDownloadUrl(mediaFileId);
                if (stereoUrl == null || stereoUrl.isEmpty()) {
                    throw new MediaFileProcessingException("Generated presigned URL for MediaFile " + mediaFile.getId() + " is empty or null.");
                }
            } catch (MediaFileNotFoundException e) {
                throw new MediaFileProcessingException("MediaFile content not found for URL generation for MediaFile " + mediaFile.getId(), e);
            } catch (Exception e) {
                throw new MediaFileProcessingException("Error generating presigned URL for MediaFile " + mediaFile.getId(), e);
            }

            String imotioUniqueId = performImotioUpload(
                    stereoUrl,
                    uploadedBy.getId(),
                    uploadBatch.getBranch() != null ? uploadBatch.getBranch().getId() : null,
                    mediaFile.getId(),
                    uploadBatch.getId(),
                    uploadBatch.getCreatedAt()
            );

            mediaFile.setImotioId(imotioUniqueId);
            mediaFile.setStatus(MediaFileStatus.PROCESSED);
            mediaFileRepository.save(mediaFile);
            imotioPollingSchedulerService.addTaskToPoll(imotioUniqueId, mediaFileId);

        } catch (ImotioProcessingException | MediaFileProcessingException e) {
            log.error("Imotio processing failed for MediaFile ID {}: {}", mediaFileId, e.getMessage(), e);
            if (mediaFile != null) {
                mediaFile.setStatus(MediaFileStatus.FAILED);
                mediaFileRepository.save(mediaFile);
            }
        } catch (Exception e) {
            log.error("An unexpected error occurred during Imotio processing for MediaFile ID {}: {}", mediaFileId, e.getMessage(), e);
            if (mediaFile != null) {
                mediaFile.setStatus(MediaFileStatus.FAILED);
                mediaFileRepository.save(mediaFile);
            }
        }
    }

    private String performImotioUpload(String stereoUrl,
                                       Long employeeId, Long unitId, Long mediaFileId, Long uploadBatchId,
                                       Instant uploadBatchCreatedAt) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("stereo_url", stereoUrl);
        formData.add("ID сотрудника", String.valueOf(employeeId));
        formData.add("ID подразделения", String.valueOf(unitId));
        formData.add("ID файла", String.valueOf(mediaFileId));
        formData.add("ID uploadBatch", String.valueOf(uploadBatchId));

        if (uploadBatchCreatedAt != null) {
            formData.add("call_time", String.valueOf(uploadBatchCreatedAt.getEpochSecond()));
        } else {
            formData.add("call_time", String.valueOf(Instant.now().getEpochSecond()));
        }

        try {
            String responseBody = webClient.post()
                    .uri("/process_call")
                    .header("X-Auth-Token", imotioAuthToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new ImotioApiCallException("Imotio API error", clientResponse.statusCode(), errorBody)))
                    )
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new ImotioResponseParseException("Imotio API returned an empty or null response.");
            }

            try {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode callIdNode = rootNode.get("call_id");

                if (callIdNode != null && callIdNode.isTextual()) {
                    return callIdNode.asText();
                } else {
                    throw new ImotioResponseParseException("Imotio API response missing valid 'call_id'. Response: " + responseBody);
                }
            } catch (Exception e) {
                throw new ImotioResponseParseException("Failed to parse Imotio API response", e);
            }
        } catch (WebClientResponseException e) {
            throw new ImotioApiCallException("Failed to upload audio to Imotio due to WebClient error", e, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ImotioProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ImotioProcessingException("An unexpected error occurred during Imotio upload.", e);
        }
    }

    private boolean isAudioFileSuitableForImotio(MediaFile mediaFile) {
        String filename = mediaFile.getFilename();
        String fileExtension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            fileExtension = filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return mediaFile.getFileType() == FileType.AUDIO &&
                (fileExtension.equals("mp3") || fileExtension.equals("wav"));
    }
}
