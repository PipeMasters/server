package com.pipemasters.server.repository;

import com.pipemasters.server.entity.UploadBatch;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadBatchRepository extends GeneralRepository<UploadBatch, Long> {
    Optional<UploadBatch> findByDirectory(UUID directory);
    List<UploadBatch> findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(Instant cutoffTime);
}
