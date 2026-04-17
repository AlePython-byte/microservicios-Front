package com.ale.observability.proxy;

import com.ale.observability.dto.ServiceExecutionResponse;
import com.ale.observability.model.LogEntry;
import com.ale.observability.model.LogStatus;
import com.ale.observability.service.MetricsService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoggingProxy {

    private final MetricsService metricsService;

    public LoggingProxy(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public ServiceExecutionResponse execute(
            String serviceId,
            MicroserviceProxy<Object> targetService,
            String operation,
            Object... params
    ) {
        String requestId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        List<Object> safeParams = params == null ? List.of() : Arrays.asList(params);

        try {
            Object result = targetService.execute(operation, params);
            long durationMs = System.currentTimeMillis() - startTime;

            LogEntry logEntry = LogEntry.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .durationMs(durationMs)
                    .status(LogStatus.SUCCESS)
                    .timestamp(timestamp)
                    .params(safeParams)
                    .response(result)
                    .errorMessage(null)
                    .stackTraceSummary(null)
                    .build();

            metricsService.addLog(logEntry);

            return ServiceExecutionResponse.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .status(LogStatus.SUCCESS.name())
                    .durationMs(durationMs)
                    .timestamp(timestamp)
                    .data(result)
                    .errorMessage(null)
                    .build();

        } catch (RuntimeException ex) {
            long durationMs = System.currentTimeMillis() - startTime;
            String stackTraceSummary = buildStackTraceSummary(ex);

            LogEntry logEntry = LogEntry.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .durationMs(durationMs)
                    .status(LogStatus.ERROR)
                    .timestamp(timestamp)
                    .params(safeParams)
                    .response(null)
                    .errorMessage(ex.getMessage())
                    .stackTraceSummary(stackTraceSummary)
                    .build();

            metricsService.addLog(logEntry);

            return ServiceExecutionResponse.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .status(LogStatus.ERROR.name())
                    .durationMs(durationMs)
                    .timestamp(timestamp)
                    .data(null)
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    private String buildStackTraceSummary(Throwable throwable) {
        if (throwable == null || throwable.getStackTrace() == null) {
            return null;
        }

        return Arrays.stream(throwable.getStackTrace())
                .limit(5)
                .map(element -> element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber())
                .collect(Collectors.joining(" | "));
    }
}