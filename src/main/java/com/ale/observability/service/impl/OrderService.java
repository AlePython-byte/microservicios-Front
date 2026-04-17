package com.ale.observability.service.impl;

import com.ale.observability.exception.ServiceOperationException;
import com.ale.observability.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService implements MicroserviceProxy<Object> {

    private static final List<String> ORDER_STATUSES = List.of(
            "CREATED",
            "PROCESSING",
            "SHIPPED",
            "DELIVERED"
    );

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation.toLowerCase()) {
            case "createorder" -> createOrder(params);
            case "getorderstatus" -> getOrderStatus(params);
            default -> throw new ServiceOperationException("Unsupported orders operation: " + operation);
        };
    }

    private Object createOrder(Object... params) {
        String customerId = getStringParam(params, 0, "CUST-1001");
        String productId = getStringParam(params, 1, "SKU-1001");
        int quantity = getIntParam(params, 2, 1);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "orders");
        response.put("operation", "createOrder");
        response.put("orderId", "ORD-" + System.currentTimeMillis());
        response.put("customerId", customerId);
        response.put("productId", productId);
        response.put("quantity", quantity);
        response.put("status", "CREATED");
        return response;
    }

    private Object getOrderStatus(Object... params) {
        String orderId = getStringParam(params, 0, "ORD-1001");
        String status = ORDER_STATUSES.get(ThreadLocalRandom.current().nextInt(ORDER_STATUSES.size()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "orders");
        response.put("operation", "getOrderStatus");
        response.put("orderId", orderId);
        response.put("status", status);
        return response;
    }

    private String getStringParam(Object[] params, int index, String defaultValue) {
        if (params == null || index >= params.length || params[index] == null) {
            return defaultValue;
        }
        return String.valueOf(params[index]);
    }

    private int getIntParam(Object[] params, int index, int defaultValue) {
        if (params == null || index >= params.length || params[index] == null) {
            return defaultValue;
        }

        Object value = params[index];
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}