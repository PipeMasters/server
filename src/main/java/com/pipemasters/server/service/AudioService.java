package com.pipemasters.server.service;

import java.util.concurrent.CompletableFuture;

public interface AudioService {
    CompletableFuture<String> extractAudio (Long mediaFileId);
    void processUploadedVideo (String uuid, String filename);
}
