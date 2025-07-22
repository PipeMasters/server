package com.pipemasters.server.dto.response;

import java.util.Map;

public record MetricsDto(
        Map<String, Long> uploadsByBranch,
        Map<String, Double> percentChangeByBranch,
        long totalStorage,
        long currentPeriodStorage,
        long previousPeriodStorage,
        double storageChangePercent
) {}