package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.response.MetricsDto;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.MetricsService;
import org.springframework.stereotype.Service;
import com.pipemasters.server.repository.UploadBatchRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final UploadBatchRepository uploadBatchRepository;
    private final MediaFileRepository mediaFileRepository;

    public MetricsServiceImpl(UploadBatchRepository uploadBatchRepository,
                          MediaFileRepository mediaFileRepository) {
        this.uploadBatchRepository = uploadBatchRepository;
        this.mediaFileRepository = mediaFileRepository;
    }

    @Transactional
    public MetricsDto getMetrics(String period) {
        Instant now = Instant.now();

        Instant currentStart;
        Instant previousStart;
        Instant previousEnd;

        if ("week".equalsIgnoreCase(period)) {
            currentStart = now.minus(7, ChronoUnit.DAYS);
            previousStart = now.minus(14, ChronoUnit.DAYS);
            previousEnd = now.minus(7, ChronoUnit.DAYS);
        } else if ("month".equalsIgnoreCase(period)) {
            currentStart = now.minus(30, ChronoUnit.DAYS);
            previousStart = now.minus(60, ChronoUnit.DAYS);
            previousEnd = now.minus(30, ChronoUnit.DAYS);
        } else if ("year".equalsIgnoreCase(period)) {
            currentStart = now.minus(365, ChronoUnit.DAYS);
            previousStart = now.minus(730, ChronoUnit.DAYS);
            previousEnd = now.minus(365, ChronoUnit.DAYS);
        } else {
            throw new IllegalArgumentException("Unsupported period: " + period);
        }

        Map<String, Long> currentCounts = uploadBatchRepository.countByBranchBetween(currentStart, now);
        Map<String, Long> previousCounts = uploadBatchRepository.countByBranchBetween(previousStart, previousEnd);
        Map<String, Double> percentChange = calculatePercentageChange(currentCounts, previousCounts);

        long totalStorage = mediaFileRepository.getTotalUsedStorage();
        long currentStorage = mediaFileRepository.getUsedStorageBetween(currentStart, now);
        long previousStorage = mediaFileRepository.getUsedStorageBetween(previousStart, previousEnd);
        double storageChangePercent = previousStorage == 0 ? 100.0 : ((double)(currentStorage - previousStorage) / previousStorage) * 100.0;

        return new MetricsDto(
                currentCounts,
                percentChange,
                totalStorage,
                currentStorage,
                previousStorage,
                storageChangePercent
        );
    }

    private Map<String, Double> calculatePercentageChange(Map<String, Long> current, Map<String, Long> previous) {
        Set<String> allBranches = new HashSet<>();
        allBranches.addAll(current.keySet());
        allBranches.addAll(previous.keySet());

        Map<String, Double> result = new HashMap<>();
        for (String branch : allBranches) {
            long curr = current.getOrDefault(branch, 0L);
            long prev = previous.getOrDefault(branch, 0L);
            double change = prev == 0 ? 100.0 : ((double)(curr - prev) / prev) * 100.0;
            result.put(branch, change);
        }
        return result;
    }
}