package com.ale.observability.controller;

import com.ale.observability.dto.ServiceExecutionRequest;
import com.ale.observability.dto.ServiceExecutionResponse;
import com.ale.observability.proxy.LoggingProxy;
import com.ale.observability.proxy.MicroserviceProxy;
import com.ale.observability.service.impl.InventoryService;
import com.ale.observability.service.impl.OrderService;
import com.ale.observability.service.impl.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceExecutionController {

    private final LoggingProxy loggingProxy;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public ServiceExecutionController(
            LoggingProxy loggingProxy,
            InventoryService inventoryService,
            OrderService orderService,
            PaymentService paymentService
    ) {
        this.loggingProxy = loggingProxy;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/inventory/{operation}")
    public ResponseEntity<ServiceExecutionResponse> executeInventoryOperation(
            @PathVariable String operation,
            @RequestBody(required = false) ServiceExecutionRequest request
    ) {
        return executeService("inventory", operation, request, inventoryService);
    }

    @PostMapping("/orders/{operation}")
    public ResponseEntity<ServiceExecutionResponse> executeOrderOperation(
            @PathVariable String operation,
            @RequestBody(required = false) ServiceExecutionRequest request
    ) {
        return executeService("orders", operation, request, orderService);
    }

    @PostMapping("/payments/{operation}")
    public ResponseEntity<ServiceExecutionResponse> executePaymentOperation(
            @PathVariable String operation,
            @RequestBody(required = false) ServiceExecutionRequest request
    ) {
        return executeService("payments", operation, request, paymentService);
    }

    private ResponseEntity<ServiceExecutionResponse> executeService(
            String serviceId,
            String operation,
            ServiceExecutionRequest request,
            MicroserviceProxy<Object> targetService
    ) {
        List<Object> params = request != null && request.getParams() != null
                ? request.getParams()
                : List.of();

        ServiceExecutionResponse response = loggingProxy.execute(
                serviceId,
                targetService,
                operation,
                params.toArray()
        );

        HttpStatus httpStatus = "SUCCESS".equalsIgnoreCase(response.getStatus())
                ? HttpStatus.OK
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(httpStatus).body(response);
    }
}