package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.MetricsDto;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.repository.UploadBatchRepository;
import com.pipemasters.server.service.impl.MetricsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricsServiceImplTest {

    private UploadBatchRepository uploadBatchRepository;
    private MediaFileRepository mediaFileRepository;
    private MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
        uploadBatchRepository = mock(UploadBatchRepository.class);
        mediaFileRepository = mock(MediaFileRepository.class);
        metricsService = new MetricsServiceImpl(uploadBatchRepository, mediaFileRepository);
    }

    @Test
    void testGetMetricsWeekPeriod() {
        Map<String, Long> currentCounts = new HashMap<>();
        currentCounts.put("Northern", 10L);
        Map<String, Long> previousCounts = new HashMap<>();
        previousCounts.put("Northern", 20L);

        when(uploadBatchRepository.countByBranchBetween(any(), any())).thenReturn(currentCounts).thenReturn(previousCounts);
        when(mediaFileRepository.getTotalUsedStorage()).thenReturn(1000L);
        when(mediaFileRepository.getUsedStorageBetween(any(), any())).thenReturn(300L).thenReturn(700L);

        MetricsDto metrics = metricsService.getMetrics("week");

        assertNotNull(metrics);
        assertEquals(10L, metrics.uploadsByBranch().get("Northern"));
        assertEquals(-50.0, metrics.percentChangeByBranch().get("Northern"));
        assertEquals(1000L, metrics.totalStorage());
        assertEquals(300L, metrics.currentPeriodStorage());
        assertEquals(700L, metrics.previousPeriodStorage());
        assertEquals(-57.14285714285714, metrics.storageChangePercent());
    }

    @Test
    void testGetMetricsInvalidPeriodThrows() {
        assertThrows(IllegalArgumentException.class, () -> metricsService.getMetrics("day"));
    }
}
