package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.FileUploadRequestDto;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.AudioService;
import com.pipemasters.server.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AudioServiceImpl implements AudioService {

    private final MediaFileRepository mediaFileRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final FileService fileService;
    private final Logger log = LoggerFactory.getLogger(AudioServiceImpl.class);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public AudioServiceImpl(MediaFileRepository mediaFileRepository, UploadBatchRepository uploadBatchRepository, FileService fileService) {
        this.mediaFileRepository = mediaFileRepository;
        this.uploadBatchRepository = uploadBatchRepository;
        this.fileService = fileService;
    }

    @Override
    @Async("ffmpegExecutor")
    @Transactional
    public CompletableFuture<String> extractAudio(Long mediaFileId) {

        return CompletableFuture.supplyAsync(() -> {

            MediaFile source = mediaFileRepository.findById(mediaFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Media file not found: " + mediaFileId));

            Path videoFile = null;
            Path audioFile = null;
            try {
                String prefix = UUID.randomUUID().toString().substring(0, 8) + "_";
                videoFile = Files.createTempFile(prefix + "vid", ".tmp");

                HttpRequest downloadRequest = HttpRequest.newBuilder()
                        .uri(URI.create(fileService.generatePresignedDownloadUrl(mediaFileId)))
                        .timeout(Duration.ofMinutes(2))
                        .GET()
                        .build();
                log.debug("Downloading video file from URL: {}", downloadRequest.uri());
                try (InputStream in = httpClient.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream())
                        .body()) {
                    Files.copy(in, videoFile, StandardCopyOption.REPLACE_EXISTING);
                }
                log.debug("Video file downloaded to: {}", videoFile);
                audioFile = Files.createTempFile(prefix + "aud", ".mp3");
                log.debug("Starting audio extraction for mediaFile={}", mediaFileId);
                Process ffmpeg = new ProcessBuilder(
                        "ffmpeg", "-y", "-i", videoFile.toString(),
                        "-vn", "-acodec", "libmp3lame", audioFile.toString())
                        .redirectErrorStream(true)
                        .start();
                try (InputStream ffmpegOut = ffmpeg.getInputStream()) {
                    String output = new String(ffmpegOut.readAllBytes());
                    log.debug("ffmpeg output: {}", output);
                }
                int exit = ffmpeg.waitFor();
                if (exit != 0) {
                    throw new IllegalStateException("FFmpeg exited with status " + exit);
                }

                String targetName = source.getFilename() + "_audio.mp3";

                FileUploadRequestDto uploadDto = new FileUploadRequestDto(
                        source.getUploadBatch().getId(), targetName, FileType.AUDIO, mediaFileId);

                String presignedPut = fileService.generatePresignedUploadUrl(uploadDto);

                HttpRequest uploadRequest = HttpRequest.newBuilder()
                        .uri(URI.create(presignedPut))
                        .timeout(Duration.ofMinutes(2))
                        .PUT(HttpRequest.BodyPublishers.ofFile(audioFile))
                        .build();
                log.debug("Uploading audio file to URL: {}", uploadRequest.uri());
                httpClient.send(uploadRequest, HttpResponse.BodyHandlers.discarding());
                log.debug("Audio file uploaded successfully: {}", targetName);
                return "i will fix this i promise";

            } catch (IOException | InterruptedException e) {
                log.error("Audio extraction failed for mediaFile={}", mediaFileId, e);
                throw new RuntimeException("Failed to extract audio", e);

            } finally {
                safeDelete(videoFile);
                safeDelete(audioFile);
            }
        });
    }

    @Override
    @Transactional
    public void processUploadedVideo(String uuid, String filename) {
        MediaFile mediaFile = mediaFileRepository.findByFilenameAndUploadBatchDirectory(filename, UUID.fromString(uuid))
                .orElseThrow(() -> new IllegalArgumentException("Media file not found: " + filename + " in batch " + uuid));
        extractAudio(mediaFile.getId());

//        UploadBatch uploadBatch = uploadBatchRepository.findByDirectory(UUID.fromString(uuid))
//                .orElseThrow(() -> new IllegalArgumentException("Upload batch not found for UUID: " + uuid));
//        uploadBatch.getFiles().forEach(f -> {
//            if (f.getFilename().equals(filename)) {
//                log.debug("Processing file: {}", f.getFilename());
//                extractAudio(f.getId());
//            } else {
//                log.debug("Skipping file: {}", f.getFilename());
//            }
//        });
    }


    private static void safeDelete(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }
}
