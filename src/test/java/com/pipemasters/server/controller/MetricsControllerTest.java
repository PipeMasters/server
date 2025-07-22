package com.pipemasters.server.controller;

import com.pipemasters.server.dto.response.MetricsDto;
import com.pipemasters.server.service.MetricsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsControllerTest {

    @Test
    void testGetMetrics() {
        MetricsDto dummy = new MetricsDto(
                Map.of("Central", 5L),
                Map.of("Central", 20.0),
                1000L, 300L, 250L, 20.0
        );

        MetricsService metricsService = Mockito.mock(MetricsService.class);
        Mockito.when(metricsService.getMetrics("month")).thenReturn(dummy);

        MetricsController controller = new MetricsController(metricsService);

        MetricsDto result = controller.getMetrics("month");

        assertEquals(dummy, result);
        assertEquals(5L, result.uploadsByBranch().get("Central"));
        assertEquals(1000L, result.totalStorage());
    }
}
