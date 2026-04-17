package com.ale.observability.service;

import com.ale.observability.model.LogEntry;
import com.ale.observability.model.LogStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();

    public void addLog(LogEntry logEntry) {
        logs.add(logEntry);
    }

    public List<LogEntry> getLogs(
            String service,
            String status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        List<LogEntry> filtered = logs.stream()
                .filter(log -> service == null || service.isBlank() || log.getServiceId().equalsIgnoreCase(service))
                .filter(log -> status == null || status.isBlank() || log.getStatus().name().equalsIgnoreCase(status))
                .filter(log -> from == null || !log.getTimestamp().isBefore(from))
                .filter(log -> to == null || !log.getTimestamp().isAfter(to))
                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                .collect(Collectors.toList());

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        int fromIndex = safePage * safeSize;
        if (fromIndex >= filtered.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        return filtered.subList(fromIndex, toIndex);
    }

    public long countLogs(
            String service,
            String status,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return logs.stream()
                .filter(log -> service == null || service.isBlank() || log.getServiceId().equalsIgnoreCase(service))
                .filter(log -> status == null || status.isBlank() || log.getStatus().name().equalsIgnoreCase(status))
                .filter(log -> from == null || !log.getTimestamp().isBefore(from))
                .filter(log -> to == null || !log.getTimestamp().isAfter(to))
                .count();
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", LocalDateTime.now());
        response.put("totalCalls", logs.size());
        response.put("services", buildServiceSummaries());
        return response;
    }

    public List<LogEntry> getLatestLogs(int limit) {
        return logs.stream()
                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildServiceSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        summaries.add(buildServiceSummary("inventory"));
        summaries.add(buildServiceSummary("orders"));
        summaries.add(buildServiceSummary("payments"));
        return summaries;
    }

    private Map<String, Object> buildServiceSummary(String serviceId) {
        List<LogEntry> serviceLogs = logs.stream()
                .filter(log -> log.getServiceId().equalsIgnoreCase(serviceId))
                .toList();

        long totalCalls = serviceLogs.size();
        long successCount = serviceLogs.stream().filter(log -> log.getStatus() == LogStatus.SUCCESS).count();
        long errorCount = serviceLogs.stream().filter(log -> log.getStatus() == LogStatus.ERROR).count();

        double successRate = totalCalls == 0 ? 0.0 : (successCount * 100.0) / totalCalls;
        double errorRate = totalCalls == 0 ? 0.0 : (errorCount * 100.0) / totalCalls;
        double averageResponseTimeMs = totalCalls == 0
                ? 0.0
                : serviceLogs.stream().mapToLong(LogEntry::getDurationMs).average().orElse(0.0);

        Map<String, Object> serviceSummary = new LinkedHashMap<>();
        serviceSummary.put("serviceId", serviceId);
        serviceSummary.put("totalCalls", totalCalls);
        serviceSummary.put("successCount", successCount);
        serviceSummary.put("errorCount", errorCount);
        serviceSummary.put("successRate", round(successRate));
        serviceSummary.put("errorRate", round(errorRate));
        serviceSummary.put("averageResponseTimeMs", round(averageResponseTimeMs));
        serviceSummary.put("highlightAsCritical", errorRate > 15.0);

        return serviceSummary;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}