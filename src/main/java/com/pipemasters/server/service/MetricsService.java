package com.pipemasters.server.service;

import com.pipemasters.server.dto.response.MetricsDto;

public interface MetricsService {
    MetricsDto getMetrics(String period);
}