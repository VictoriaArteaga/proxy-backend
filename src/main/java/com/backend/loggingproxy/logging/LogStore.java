package com.backend.loggingproxy.logging;

import com.backend.loggingproxy.dto.LogFilterRequest;
import com.backend.loggingproxy.dto.PagedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class LogStore {

    private final ConcurrentLinkedDeque<LogEntry> logs = new ConcurrentLinkedDeque<>();

    @Value("${app.logs.max-size:10000}")
    private int maxSize;

    public void save(LogEntry entry) {
        logs.addFirst(entry);
        while (logs.size() > maxSize) logs.pollLast();
    }

    public List<LogEntry> findAll() {
        return List.copyOf(logs);
    }

    public PagedResponse<LogEntry> findFiltered(LogFilterRequest f) {
        List<LogEntry> filtered = logs.stream()
                .filter(l -> f.service() == null || f.service().isBlank() || l.serviceId().equals(f.service()))
                .filter(l -> f.status() == null || f.status().isBlank() || l.status().equalsIgnoreCase(f.status()))
                .filter(l -> f.from() == null || !l.timestamp().isBefore(f.from()))
                .filter(l -> f.to() == null || !l.timestamp().isAfter(f.to()))
                .sorted(Comparator.comparing(LogEntry::timestamp).reversed())
                .toList();

        int page = Math.max(0, f.page());
        int size = Math.max(1, f.size());
        int total = filtered.size();
        int fromIdx = Math.min(page * size, total);
        int toIdx = Math.min(fromIdx + size, total);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PagedResponse<>(filtered.subList(fromIdx, toIdx), page, size, total, totalPages);
    }
}
