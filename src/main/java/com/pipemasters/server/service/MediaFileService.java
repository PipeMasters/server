package com.pipemasters.server.service;

import java.util.UUID;

public interface MediaFileService {
    void handleMinioFileDeletion(UUID batchUuid, String filename);
}
