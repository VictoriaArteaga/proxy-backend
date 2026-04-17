package com.backend.loggingproxy.logging;

import java.time.Instant;

public record LogEntry(
        String requestId,
        String serviceId,
        String operation,
        long durationMs,
        String status,
        Instant timestamp,
        Object[] params,
        Object response,
        String errorTrace
) {}
