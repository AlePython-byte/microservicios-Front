package com.ale.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    private String requestId;
    private String serviceId;
    private String operation;
    private long durationMs;
    private LogStatus status;
    private LocalDateTime timestamp;

    private List<Object> params;
    private Object response;
    private String errorMessage;
}