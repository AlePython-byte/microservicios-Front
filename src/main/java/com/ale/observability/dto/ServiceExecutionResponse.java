package com.ale.observability.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExecutionResponse {

    private String requestId;
    private String serviceId;
    private String operation;
    private String status;
    private long durationMs;
    private LocalDateTime timestamp;
    private Object data;
    private String errorMessage;
}