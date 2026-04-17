package com.ale.observability.dto;

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
public class LogEntryResponse {

    private String requestId;
    private String serviceId;
    private String operation;
    private long durationMs;
    private String status;
    private LocalDateTime timestamp;
    private List<Object> params;
    private Object response;
    private String errorMessage;
    private String stackTraceSummary;
}