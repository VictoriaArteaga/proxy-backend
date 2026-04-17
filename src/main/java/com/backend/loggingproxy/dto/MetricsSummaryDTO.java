package com.backend.loggingproxy.dto;

import java.util.List;

public record MetricsSummaryDTO(List<ServiceMetrics> services) {

    public record ServiceMetrics(
            String serviceId,
            long totalCalls,
            long successCount,
            long errorCount,
            double errorRate,
            double avgDurationMs,
            boolean hasIssues
    ) {}
}
