package com.pipemasters.server.service;

import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.exceptions.file.FileGenerationException;
import com.pipemasters.server.service.impl.FileCleanupSchedulerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileCleanupSchedulerImplTest {

    @Mock
    private UploadBatchRepository uploadBatchRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileCleanupSchedulerImpl fileCleanupScheduler;

    private LocalDate sixMonthsAgoDate;
    private Instant sixMonthsAgoInstant;

    @BeforeEach
    void setUp() {
        sixMonthsAgoDate = LocalDate.now(ZoneOffset.UTC).minusMonths(6);
        sixMonthsAgoInstant = sixMonthsAgoDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Test
    void cleanupOldUploadBatches_shouldDeleteAndMarkBatchesWhenFound() throws Exception {
        UploadBatch batch1 = new UploadBatch();
        batch1.setId(1L);
        batch1.setDirectory(UUID.randomUUID());
        batch1.setCreatedAt(sixMonthsAgoInstant.minus(Duration.ofDays(1)));
        batch1.setDeleted(false);
        batch1.setArchived(false);

        UploadBatch batch2 = new UploadBatch();
        batch2.setId(2L);
        batch2.setDirectory(UUID.randomUUID());
        batch2.setCreatedAt(sixMonthsAgoInstant.minus(Duration.ofDays(5)));
        batch2.setDeleted(false);
        batch2.setArchived(false);

        List<UploadBatch> oldBatches = Arrays.asList(batch1, batch2);

        when(uploadBatchRepository.findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(any(Instant.class)))
                .thenReturn(oldBatches);

        fileCleanupScheduler.cleanupOldUploadBatches();

        verify(fileService, times(1)).deleteUploadBatchDirectory(batch1.getDirectory());
        verify(fileService, times(1)).deleteUploadBatchDirectory(batch2.getDirectory());

        ArgumentCaptor<UploadBatch> batchCaptor = ArgumentCaptor.forClass(UploadBatch.class);
        verify(uploadBatchRepository, times(2)).save(batchCaptor.capture());

        List<UploadBatch> savedBatches = batchCaptor.getAllValues();
        assertEquals(2, savedBatches.size());

        assertTrue(savedBatches.stream().anyMatch(b -> b.getId().equals(batch1.getId()) && b.isDeleted() && b.getDeletedAt() != null));
        assertTrue(savedBatches.stream().anyMatch(b -> b.getId().equals(batch2.getId()) && b.isDeleted() && b.getDeletedAt() != null));
    }

    @Test
    void cleanupOldUploadBatches_shouldHandleNoOldBatchesFound() throws Exception {
        when(uploadBatchRepository.findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(any(Instant.class)))
                .thenReturn(Collections.emptyList());

        fileCleanupScheduler.cleanupOldUploadBatches();

        verify(fileService, never()).deleteUploadBatchDirectory(any(UUID.class));
        verify(uploadBatchRepository, never()).save(any(UploadBatch.class));
    }

    @Test
    void cleanupOldUploadBatches_shouldContinueProcessingIfDeletionFailsForOneBatch() throws Exception {
        UploadBatch batch1 = new UploadBatch();
        batch1.setId(1L);
        batch1.setDirectory(UUID.randomUUID());

        batch1.setCreatedAt(sixMonthsAgoInstant.minus(Duration.ofDays(1)));
        batch1.setDeleted(false);
        batch1.setArchived(false);

        UploadBatch batch2 = new UploadBatch();
        batch2.setId(2L);
        batch2.setDirectory(UUID.randomUUID());

        batch2.setCreatedAt(sixMonthsAgoInstant.minus(Duration.ofDays(5)));
        batch2.setDeleted(false);
        batch2.setArchived(false);

        List<UploadBatch> oldBatches = Arrays.asList(batch1, batch2);

        when(uploadBatchRepository.findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(any(Instant.class)))
                .thenReturn(oldBatches);

        doThrow(new FileGenerationException("Simulated S3 deletion error for batch1"))
                .when(fileService).deleteUploadBatchDirectory(batch1.getDirectory());
        doNothing()
                .when(fileService).deleteUploadBatchDirectory(batch2.getDirectory());

        fileCleanupScheduler.cleanupOldUploadBatches();

        verify(fileService, times(1)).deleteUploadBatchDirectory(batch1.getDirectory());
        verify(fileService, times(1)).deleteUploadBatchDirectory(batch2.getDirectory());

        ArgumentCaptor<UploadBatch> batchCaptor = ArgumentCaptor.forClass(UploadBatch.class);
        verify(uploadBatchRepository, times(1)).save(batchCaptor.capture());

        UploadBatch savedBatch = batchCaptor.getValue();
        assertEquals(batch2.getId(), savedBatch.getId());
        assertTrue(savedBatch.isDeleted());
        assertNotNull(savedBatch.getDeletedAt());
    }

    @Test
    void cleanupOldUploadBatches_shouldNotMarkBatchAsDeletedIfFileServiceThrowsException() throws Exception {
        UploadBatch batch = new UploadBatch();
        batch.setId(1L);
        batch.setDirectory(UUID.randomUUID());
        batch.setCreatedAt(sixMonthsAgoInstant.minus(Duration.ofDays(1)));
        batch.setDeleted(false);
        batch.setArchived(false);

        when(uploadBatchRepository.findByCreatedAtBeforeAndDeletedFalseAndArchivedFalse(any(Instant.class)))
                .thenReturn(Collections.singletonList(batch));

        doThrow(new FileGenerationException("Simulated S3 deletion error"))
                .when(fileService).deleteUploadBatchDirectory(batch.getDirectory());

        fileCleanupScheduler.cleanupOldUploadBatches();

        verify(fileService, times(1)).deleteUploadBatchDirectory(batch.getDirectory());
        verify(uploadBatchRepository, never()).save(any(UploadBatch.class));
    }
}