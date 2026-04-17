package com.backend.loggingproxy.controller;

import com.backend.loggingproxy.dto.LogFilterRequest;
import com.backend.loggingproxy.dto.MetricsSummaryDTO;
import com.backend.loggingproxy.dto.PagedResponse;
import com.backend.loggingproxy.logging.LogEntry;
import com.backend.loggingproxy.logging.LogStore;
import com.backend.loggingproxy.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    @Value("${app.metrics.error-threshold:0.15}")
    private double errorThreshold;

    private final LogStore logStore;
    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> ordersProxy;
    private final MicroserviceProxy<Object> paymentsProxy;

    public MetricsController(
            LogStore logStore,
            @Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
            @Qualifier("ordersProxy") MicroserviceProxy<Object> ordersProxy,
            @Qualifier("paymentsProxy") MicroserviceProxy<Object> paymentsProxy
    ) {
        this.logStore = logStore;
        this.inventoryProxy = inventoryProxy;
        this.ordersProxy = ordersProxy;
        this.paymentsProxy = paymentsProxy;
    }

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummaryDTO> summary() {
        List<LogEntry> all = logStore.findAll();
        List<MetricsSummaryDTO.ServiceMetrics> metrics = List.of("inventory", "orders", "payments").stream()
                .map(id -> buildServiceMetrics(id, all))
                .toList();
        return ResponseEntity.ok(new MetricsSummaryDTO(metrics));
    }

    @GetMapping("/logs")
    public ResponseEntity<PagedResponse<LogEntry>> logs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(logStore.findFiltered(
                new LogFilterRequest(service, status, from, to, page, size)
        ));
    }

    @PostMapping("/simulate-load")
    public ResponseEntity<Map<String, Object>> simulateLoad() {
        int total = 50;
        String[] skus = {"sku-001", "sku-002", "sku-003"};
        for (int i = 0; i < total; i++) {
            int pick = (int) (Math.random() * 3);
            try {
                switch (pick) {
                    case 0 -> inventoryProxy.execute("check", skus[i % 3]);
                    case 1 -> ordersProxy.execute("create", skus[i % 3], 1 + (int) (Math.random() * 5));
                    case 2 -> paymentsProxy.execute("charge", UUID.randomUUID().toString(), Math.random() * 1000);
                }
            } catch (Exception ignored) {
                // Las fallas ya quedan registradas por el LoggingProxy
            }
        }
        return ResponseEntity.ok(Map.of("generated", total));
    }

    private MetricsSummaryDTO.ServiceMetrics buildServiceMetrics(String serviceId, List<LogEntry> all) {
        List<LogEntry> filtered = all.stream().filter(l -> l.serviceId().equals(serviceId)).toList();
        long total = filtered.size();
        long success = filtered.stream().filter(l -> "SUCCESS".equals(l.status())).count();
        long errors = total - success;
        double errorRate = total == 0 ? 0 : (double) errors / total;
        double avgMs = filtered.stream().mapToLong(LogEntry::durationMs).average().orElse(0);
        boolean hasIssues = errorRate > errorThreshold;
        return new MetricsSummaryDTO.ServiceMetrics(serviceId, total, success, errors, errorRate, avgMs, hasIssues);
    }
}
