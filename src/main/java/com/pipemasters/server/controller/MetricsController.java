package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.MetricsDto;
import com.pipemasters.server.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public MetricsDto getMetrics(@RequestParam(defaultValue = "week") String period) {
        return metricsService.getMetrics(period);
    }
}