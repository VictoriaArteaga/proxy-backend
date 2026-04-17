package com.backend.loggingproxy.proxy;

import com.backend.loggingproxy.logging.LogEntry;
import com.backend.loggingproxy.logging.LogStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private static final Logger log = LoggerFactory.getLogger(LoggingProxy.class);

    private final String serviceId;
    private final MicroserviceProxy<T> target;
    private final LogStore logStore;

    public LoggingProxy(String serviceId, MicroserviceProxy<T> target, LogStore logStore) {
        this.serviceId = serviceId;
        this.target = target;
        this.logStore = logStore;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        Instant start = Instant.now();
        log.info("[{}] {}::{} START params={}", requestId, serviceId, operation, Arrays.toString(params));

        try {
            T result = target.execute(operation, params);
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            logStore.save(new LogEntry(
                    requestId, serviceId, operation, durationMs,
                    "SUCCESS", start, params, result, null
            ));
            log.info("[{}] {}::{} SUCCESS ({}ms)", requestId, serviceId, operation, durationMs);
            return result;
        } catch (Exception e) {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            String trace = resumeStackTrace(e);
            logStore.save(new LogEntry(
                    requestId, serviceId, operation, durationMs,
                    "ERROR", start, params, null, trace
            ));
            log.error("[{}] {}::{} ERROR ({}ms): {}", requestId, serviceId, operation, durationMs, trace);
            throw e;
        }
    }

    private String resumeStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
        StackTraceElement[] elements = e.getStackTrace();
        int limit = Math.min(3, elements.length);
        for (int i = 0; i < limit; i++) {
            sb.append(" | ").append(elements[i].toString());
        }
        return sb.toString();
    }
}
