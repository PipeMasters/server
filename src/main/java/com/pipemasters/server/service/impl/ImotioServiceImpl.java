package com.pipemasters.server.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${imotio.api.url}")
    private String imotioApiUrl;

    @Value("${imotio.auth.token}")
    private String imotioAuthToken;

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;

    public ImotioServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                             MediaFileRepository mediaFileRepository, FileService fileService) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.mediaFileRepository = mediaFileRepository;
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .baseUrl(imotioApiUrl)
                .build();
    }


    // TODO: подумать что делать со статусами медиафайла, как их правильно менять и надо ли вообще это делать.
    //  мб логирования тут сделать поменьше, а то прям спам. Надо сделать кастомные ошибки сюда.
    //  надо подумать, что возвращать при ошибках, сейчас это return null, но это не информативно как то, не знаю можно ли лучше.
    //  мб еще как то можно оптимизировать код, тоже надо подумать.
    //  надо написать тесты на этот сервис
    @Override
    @Transactional
    public String processImotioFileUpload(Long mediaFileId) {
        log.info("Starting Imotio file upload processing for MediaFile with ID: {}.", mediaFileId);

        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaFileId);
        if (mediaFileOptional.isEmpty()) {
            log.warn("MediaFile with ID {} not found in the database. Skipping Imotio upload.", mediaFileId);
            return null;
        }
        MediaFile mediaFile = mediaFileOptional.get();

        if (!isAudioFileSuitableForImotio(mediaFile)) {
            log.info("MediaFile {} (type: {}) is not an audio file suitable for Imotio. Skipping.",
                    mediaFile.getId(), mediaFile.getFileType());
            return null;
        }

        UploadBatch uploadBatch = mediaFile.getUploadBatch();
        if (uploadBatch == null) {
            log.error("MediaFile {} has no associated UploadBatch. Cannot proceed with Imotio upload.", mediaFile.getId());
            return null;
        }
        User uploadedBy = uploadBatch.getUploadedBy();
        if (uploadedBy == null) {
            log.error("UploadBatch {} has no associated User. Cannot proceed with Imotio upload for MediaFile {}.", uploadBatch.getId(), mediaFile.getId());
            return null;
        }

        String stereoUrl;
        try {
            stereoUrl = fileService.generatePresignedDownloadUrl(mediaFileId);
            if (stereoUrl == null || stereoUrl.isEmpty()) {
                log.error("Generated presigned URL for MediaFile {} is empty or null.", mediaFile.getId());
                return null;
            }
            log.debug("Successfully generated presigned URL for MediaFile {}: {}", mediaFile.getId(), stereoUrl);
        } catch (MediaFileNotFoundException e) {
            log.error("MediaFile content not found for URL generation for MediaFile {}. Error: {}", mediaFile.getId(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error generating presigned URL for MediaFile {}. Error: {}", mediaFile.getId(), e.getMessage(), e);
            return null;
        }

        String imotioUniqueId;
        try {
            imotioUniqueId = performImotioUpload(
                    stereoUrl,
                    uploadedBy.getId(),
                    uploadBatch.getBranch() != null ? uploadBatch.getBranch().getId() : null,
                    mediaFile.getId(),
                    uploadBatch.getId(),
                    uploadBatch.getCreatedAt()
            );

            mediaFile.setImotioId(imotioUniqueId);
            mediaFileRepository.save(mediaFile);
            log.info("File {} successfully uploaded to Imotio, assigned imotioId: {}.",
                    mediaFile.getId(), imotioUniqueId);

        } catch (Exception e) {
            log.error("Failed to upload audio file {} to Imotio: {}", mediaFile.getFilename(), e.getMessage(), e);
            return null;
        }
        return imotioUniqueId;
    }

    private String performImotioUpload(String stereoUrl,
                                       Long employeeId, Long unitId, Long mediaFileId, Long uploadBatchId,
                                       Instant uploadBatchCreatedAt) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            formData.add("stereo_url", stereoUrl);
            formData.add("ID сотрудника", String.valueOf(employeeId));
            formData.add("ID подразделения", String.valueOf(unitId));
            formData.add("ID файла", String.valueOf(mediaFileId));
            formData.add("ID uploadBatch", String.valueOf(uploadBatchId));

            if (uploadBatchCreatedAt != null) {
                formData.add("call_time", String.valueOf(uploadBatchCreatedAt.getEpochSecond()));
            } else {
                log.warn("UploadBatch createdAt is null for MediaFile ID {}. Using current timestamp for call_time.", mediaFileId);
                formData.add("call_time", String.valueOf(Instant.now().getEpochSecond()));
            }

            log.info("Attempting to send request to Imotio API at endpoint: /process_call");

            Mono<String> responseMono = webClient.post()
                    .uri("/process_call")
                    .header("X-Auth-Token", imotioAuthToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("Imotio API returned an error status: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Imotio API error response body: {}", errorBody);
                                    return Mono.error(new RuntimeException("Imotio API error: " + clientResponse.statusCode() + " - " + errorBody));
                                });
                    })
                    .bodyToMono(String.class);

            String responseBody = responseMono.block();

            if (responseBody != null && !responseBody.trim().isEmpty()) {
                log.debug("Raw Imotio API response: {}", responseBody);
                try {
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    JsonNode callIdNode = rootNode.get("call_id");

                    if (callIdNode != null && callIdNode.isTextual()) {
                        String imotioUniqueId = callIdNode.asText();
                        log.debug("Successfully extracted 'call_id' from Imotio API response: {}", imotioUniqueId);
                        return imotioUniqueId;
                    } else {
                        log.error("Imotio API response did not contain a valid 'call_id' field: {}", responseBody);
                        throw new RuntimeException("Imotio API response missing 'call_id'.");
                    }
                } catch (Exception e) {
                    log.error("Error parsing Imotio API response JSON: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to parse Imotio API response: " + e.getMessage(), e);
                }
            } else {
                log.error("Imotio API returned an empty or null response body.");
                throw new RuntimeException("Imotio API returned an empty or null response.");
            }

        } catch (WebClientResponseException e) {
            log.error("WebClient error during Imotio file upload: {}. Response body: {}", e.getMessage(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to upload audio to Imotio due to WebClient error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to upload audio to Imotio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload audio to Imotio: " + e.getMessage(), e);
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
