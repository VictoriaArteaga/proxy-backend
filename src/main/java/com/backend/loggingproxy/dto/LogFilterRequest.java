package com.backend.loggingproxy.dto;

import java.time.Instant;

public record LogFilterRequest(
        String service,
        String status,
        Instant from,
        Instant to,
        int page,
        int size
) {}
