package com.pipemasters.server.repository;

import com.pipemasters.server.entity.UploadBatch;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadBatchRepository extends GeneralRepository<UploadBatch, Long>, JpaSpecificationExecutor<UploadBatch> {
    Optional<UploadBatch> findByDirectory(UUID directory);
    List<UploadBatch> findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(Instant cutoffTime);
}
