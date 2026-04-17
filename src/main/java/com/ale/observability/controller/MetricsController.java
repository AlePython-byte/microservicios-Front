package com.ale.observability.controller;

import com.ale.observability.dto.LogEntryResponse;
import com.ale.observability.dto.MetricsSummaryResponse;
import com.ale.observability.dto.SimulateLoadRequest;
import com.ale.observability.service.MetricsService;
import com.ale.observability.service.SimulateLoadService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final SimulateLoadService simulateLoadService;

    public MetricsController(
            MetricsService metricsService,
            SimulateLoadService simulateLoadService
    ) {
        this.metricsService = metricsService;
        this.simulateLoadService = simulateLoadService;
    }

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummaryResponse> getSummary() {
        return ResponseEntity.ok(metricsService.getSummary());
    }

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<LogEntryResponse> content = metricsService.getLogs(service, status, from, to, safePage, safeSize);
        long totalElements = metricsService.countLogs(service, status, from, to);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("page", safePage);
        response.put("size", safeSize);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate-load")
    public ResponseEntity<Map<String, Object>> simulateLoad(
            @Valid @RequestBody(required = false) SimulateLoadRequest request
    ) {
        int totalRequests = request != null ? request.getTotalRequests() : 30;
        simulateLoadService.simulate(totalRequests);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Load simulation completed");
        response.put("totalRequests", totalRequests);

        return ResponseEntity.ok(response);
    }
}