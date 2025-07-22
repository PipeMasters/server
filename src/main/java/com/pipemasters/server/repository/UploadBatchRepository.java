package com.pipemasters.server.repository;

import com.pipemasters.server.entity.UploadBatch;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

public interface UploadBatchRepository extends GeneralRepository<UploadBatch, Long>, JpaSpecificationExecutor<UploadBatch> {
    Optional<UploadBatch> findByDirectory(UUID directory);
    List<UploadBatch> findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(Instant cutoffTime);
    @Query("""
        SELECT ub.branch.name, COUNT(ub)
        FROM UploadBatch ub
        WHERE ub.archived = false AND ub.deleted = false
              AND ub.createdAt BETWEEN :start AND :end
        GROUP BY ub.branch.name
    """)
    List<Object[]> countUploadsRaw(Instant start, Instant end);

    default Map<String, Long> countByBranchBetween(Instant start, Instant end) {
        List<Object[]> rows = countUploadsRaw(start, end);
        return rows.stream().collect(
                java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                )
        );
    }

    @Query("""
        SELECT u FROM UploadBatch u
        WHERE u.archived = false AND u.deleted = false
    """)
    List<UploadBatch> findAllNotArchived();
}