package com.ale.observability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetricsResponse {

    private String serviceId;
    private long totalCalls;
    private long successCount;
    private long errorCount;
    private double successRate;
    private double errorRate;
    private double averageResponseTimeMs;
    private boolean highlightAsCritical;
    private List<LogEntryResponse> last20Calls;
}