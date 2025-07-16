package com.pipemasters.server.repository;

import com.pipemasters.server.entity.MediaFile;
import org.springframework.data.jpa.repository.Query;

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
}
