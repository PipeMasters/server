package com.pipemasters.server.repository;

import com.pipemasters.server.entity.MediaFile;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends GeneralRepository<MediaFile, Long> {
    void deleteById(Long id);
    @Query("select m from MediaFile m where m.filename = :filename and m.uploadBatch.directory = :uploadBatchDirectory")
    Optional<MediaFile> findByFilenameAndUploadBatchDirectory(String filename, UUID uploadBatchDirectory);
    @Query("select (count(m) > 0) from MediaFile m where m.filename = :filename and m.uploadBatch.directory = :uploadBatchDirectory")
    boolean existsByFilenameAndUploadBatchDirectory(String filename, UUID uploadBatchDirectory);
    @Query("select m from MediaFile m where m.uploadBatch.id = :uploadBatchId")
    List<MediaFile> findByUploadBatchId(Long uploadBatchId);
    Optional<MediaFile> findByImotioId(String imotioId);

    @Query("""
        SELECT COALESCE(SUM(mf.size), 0)
        FROM MediaFile mf
        WHERE mf.uploadBatch.archived = false AND mf.uploadBatch.deleted = false
    """)
    Long getTotalUsedStorage();

    @Query("""
        SELECT COALESCE(SUM(mf.size), 0)
        FROM MediaFile mf
        WHERE mf.uploadBatch.archived = false AND mf.uploadBatch.deleted = false
              AND mf.uploadBatch.createdAt BETWEEN :start AND :end
    """)
    Long getUsedStorageBetween(Instant start, Instant end);
}