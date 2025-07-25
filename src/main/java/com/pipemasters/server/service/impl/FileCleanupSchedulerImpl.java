package com.pipemasters.server.service.impl;

import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.FileCleanupScheduler;
import com.pipemasters.server.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class FileCleanupSchedulerImpl implements FileCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupSchedulerImpl.class);

    private final UploadBatchRepository uploadBatchRepository;
    private final FileService fileService;

    public FileCleanupSchedulerImpl(UploadBatchRepository uploadBatchRepository, FileService fileService) {
        this.uploadBatchRepository = uploadBatchRepository;
        this.fileService = fileService;
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldUploadBatches() {
        log.info("Starting daily cleanup of old upload batches.");
        LocalDate sixMonthsAgoDate = LocalDate.now(ZoneOffset.UTC).minusMonths(6);

        Instant sixMonthsAgoInstant = sixMonthsAgoDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        List<UploadBatch> oldBatches = uploadBatchRepository.findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(sixMonthsAgoInstant);

        if (oldBatches.isEmpty()) {
            log.info("No old upload batches found for cleanup.");
            return;
        }

        log.info("Found {} old upload batches to clean up.", oldBatches.size());

        for (UploadBatch batch : oldBatches) {
            try {
                fileService.deleteUploadBatchDirectory(batch.getDirectory());

                batch.setDeletedAt(Instant.now());
                batch.setDeleted(true);
                uploadBatchRepository.save(batch);
                log.info("Successfully processed deletion for UploadBatch with ID: {} and Directory: {}", batch.getId(), batch.getDirectory());
            } catch (Exception e) {
                log.error("Failed to delete UploadBatch with ID: {} and Directory: {}. Error: {}", batch.getId(), batch.getDirectory(), e.getMessage(), e);
            }
        }
        log.info("Finished daily cleanup of old upload batches.");
    }
}
