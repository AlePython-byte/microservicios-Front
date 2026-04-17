package com.ale.observability.service.impl;

import com.ale.observability.exception.ServiceOperationException;
import com.ale.observability.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class InventoryService implements MicroserviceProxy<Object> {

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation.toLowerCase()) {
            case "getstock" -> getStock(params);
            case "reservestock" -> reserveStock(params);
            default -> throw new ServiceOperationException("Unsupported inventory operation: " + operation);
        };
    }

    private Object getStock(Object... params) {
        String productId = getStringParam(params, 0, "SKU-1001");
        int stock = ThreadLocalRandom.current().nextInt(10, 120);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "inventory");
        response.put("operation", "getStock");
        response.put("productId", productId);
        response.put("availableStock", stock);
        response.put("warehouse", "MAIN");
        return response;
    }

    private Object reserveStock(Object... params) {
        String productId = getStringParam(params, 0, "SKU-1001");
        int quantity = getIntParam(params, 1, 1);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "inventory");
        response.put("operation", "reserveStock");
        response.put("productId", productId);
        response.put("requestedQuantity", quantity);
        response.put("reserved", true);
        response.put("reservationCode", "RSV-" + System.currentTimeMillis());
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