package com.ale.observability.service;

import com.ale.observability.dto.LogEntryResponse;
import com.ale.observability.dto.MetricsSummaryResponse;
import com.ale.observability.dto.ServiceMetricsResponse;
import com.ale.observability.model.LogEntry;
import com.ale.observability.model.LogStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();

    public void addLog(LogEntry logEntry) {
        logs.add(logEntry);
    }

    public MetricsSummaryResponse getSummary() {
        return MetricsSummaryResponse.builder()
                .generatedAt(LocalDateTime.now())
                .totalCalls(logs.size())
                .services(List.of(
                        buildServiceSummary("inventory"),
                        buildServiceSummary("orders"),
                        buildServiceSummary("payments")
                ))
                .build();
    }

    public List<LogEntryResponse> getLogs(
            String service,
            String status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        List<LogEntry> filtered = filterLogs(service, status, from, to);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        int fromIndex = safePage * safeSize;
        if (fromIndex >= filtered.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + safeSize, filtered.size());

        return filtered.subList(fromIndex, toIndex)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countLogs(
            String service,
            String status,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return filterLogs(service, status, from, to).size();
    }

    public List<LogEntryResponse> getLatestLogsForChart(String serviceId, int limit) {
        return logs.stream()
                .filter(log -> serviceId == null || serviceId.isBlank() || log.getServiceId().equalsIgnoreCase(serviceId))
                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<LogEntry> filterLogs(
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
                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    private ServiceMetricsResponse buildServiceSummary(String serviceId) {
        List<LogEntry> serviceLogs = logs.stream()
                .filter(log -> log.getServiceId().equalsIgnoreCase(serviceId))
                .toList();

        long totalCalls = serviceLogs.size();
        long successCount = serviceLogs.stream()
                .filter(log -> log.getStatus() == LogStatus.SUCCESS)
                .count();
        long errorCount = serviceLogs.stream()
                .filter(log -> log.getStatus() == LogStatus.ERROR)
                .count();

        double successRate = totalCalls == 0 ? 0.0 : (successCount * 100.0) / totalCalls;
        double errorRate = totalCalls == 0 ? 0.0 : (errorCount * 100.0) / totalCalls;
        double averageResponseTimeMs = totalCalls == 0
                ? 0.0
                : serviceLogs.stream().mapToLong(LogEntry::getDurationMs).average().orElse(0.0);

        return ServiceMetricsResponse.builder()
                .serviceId(serviceId)
                .totalCalls(totalCalls)
                .successCount(successCount)
                .errorCount(errorCount)
                .successRate(round(successRate))
                .errorRate(round(errorRate))
                .averageResponseTimeMs(round(averageResponseTimeMs))
                .highlightAsCritical(errorRate > 15.0)
                .last20Calls(
                        serviceLogs.stream()
                                .sorted(Comparator.comparing(LogEntry::getTimestamp).reversed())
                                .limit(20)
                                .map(this::toResponse)
                                .toList()
                )
                .build();
    }

    private LogEntryResponse toResponse(LogEntry logEntry) {
        return LogEntryResponse.builder()
                .requestId(logEntry.getRequestId())
                .serviceId(logEntry.getServiceId())
                .operation(logEntry.getOperation())
                .durationMs(logEntry.getDurationMs())
                .status(logEntry.getStatus().name())
                .timestamp(logEntry.getTimestamp())
                .params(logEntry.getParams())
                .response(logEntry.getResponse())
                .errorMessage(logEntry.getErrorMessage())
                .stackTraceSummary(logEntry.getStackTraceSummary())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}