package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TranscriptFragment;
import com.pipemasters.server.entity.UploadBatch;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TranscriptFragmentRepository extends GeneralRepository<TranscriptFragment, Long> {

    @Query(value = """
            SELECT * FROM transcript_fragments tf
            WHERE tf.tsv @@ plainto_tsquery('russian', :query)
            """, nativeQuery = true)
    List<TranscriptFragment> search(String query);

    interface BatchFragmentProjection {
        Long getBatchId();

        Long getMediaFileId();

        Long getFragmentId();
    }

    interface MediaFileFragmentProjection {
        Long getMediaFileId();

        Long getFragmentId();
    }

    @Query(value = """
            SELECT tf.media_file_id AS mediaFileId, tf.id AS fragmentId
            FROM transcript_fragments tf
            JOIN media_files mf ON tf.media_file_id = mf.id
            WHERE mf.upload_batch_id = :uploadBatchId
            AND tf.tsv @@ plainto_tsquery('russian', :query)
            """, nativeQuery = true)
    List<MediaFileFragmentProjection> findFragmentsByUploadBatch(@Param("uploadBatchId") Long uploadBatchId, @Param("query") String query);

    Optional<TranscriptFragment> findByFragmentId(String fragmentId);

    @Query(value = """
            SELECT mf.upload_batch_id AS batchId, tf.media_file_id AS mediaFileId, tf.id AS fragmentId
            FROM transcript_fragments tf
                JOIN media_files mf ON tf.media_file_id = mf.id
            WHERE tf.tsv @@ plainto_tsquery('russian', :query)
            """, nativeQuery = true)
    List<BatchFragmentProjection> findBatchFragments(String query);

    @Query(value = """
            SELECT DISTINCT ub.* FROM upload_batches ub
                JOIN media_files mf ON mf.upload_batch_id = ub.id
                JOIN transcript_fragments tf ON tf.media_file_id = mf.id
            WHERE tf.tsv @@ plainto_tsquery('russian', :query)
            """, nativeQuery = true)
    List<UploadBatch> searchUploadBatches(String query);
}