package com.ale.observability.service.impl;

import com.ale.observability.exception.ServiceOperationException;
import com.ale.observability.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService implements MicroserviceProxy<Object> {

    @Override
    public Object execute(String operation, Object... params) {
        simulateRandomFailure();

        return switch (operation.toLowerCase()) {
            case "processpayment" -> processPayment(params);
            case "refundpayment" -> refundPayment(params);
            default -> throw new ServiceOperationException("Unsupported payments operation: " + operation);
        };
    }

    private Object processPayment(Object... params) {
        String orderId = getStringParam(params, 0, "ORD-1001");
        double amount = getDoubleParam(params, 1, 99.99);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "payments");
        response.put("operation", "processPayment");
        response.put("paymentId", "PAY-" + UUID.randomUUID());
        response.put("orderId", orderId);
        response.put("amount", amount);
        response.put("status", "APPROVED");
        return response;
    }

    private Object refundPayment(Object... params) {
        String paymentId = getStringParam(params, 0, "PAY-1001");
        double amount = getDoubleParam(params, 1, 49.99);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "payments");
        response.put("operation", "refundPayment");
        response.put("paymentId", paymentId);
        response.put("refundAmount", amount);
        response.put("status", "REFUNDED");
        return response;
    }

    private void simulateRandomFailure() {
        int randomValue = ThreadLocalRandom.current().nextInt(100);
        if (randomValue < 10) {
            throw new ServiceOperationException("Simulated payment processing failure");
        }
    }

    private String getStringParam(Object[] params, int index, String defaultValue) {
        if (params == null || index >= params.length || params[index] == null) {
            return defaultValue;
        }
        return String.valueOf(params[index]);
    }

    private double getDoubleParam(Object[] params, int index, double defaultValue) {
        if (params == null || index >= params.length || params[index] == null) {
            return defaultValue;
        }

        Object value = params[index];
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}