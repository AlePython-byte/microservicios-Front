package com.ale.observability.service;

import com.ale.observability.proxy.LoggingProxy;
import com.ale.observability.service.impl.InventoryService;
import com.ale.observability.service.impl.OrderService;
import com.ale.observability.service.impl.PaymentService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulateLoadService {

    private final LoggingProxy loggingProxy;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public SimulateLoadService(
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

    public void simulate(int totalRequests) {
        for (int i = 0; i < totalRequests; i++) {
            int randomService = ThreadLocalRandom.current().nextInt(3);

            switch (randomService) {
                case 0 -> callInventory();
                case 1 -> callOrders();
                default -> callPayments();
            }
        }
    }

    private void callInventory() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            loggingProxy.execute(
                    "inventory",
                    inventoryService,
                    "getStock",
                    "SKU-" + randomNumber()
            );
        } else {
            loggingProxy.execute(
                    "inventory",
                    inventoryService,
                    "reserveStock",
                    "SKU-" + randomNumber(),
                    ThreadLocalRandom.current().nextInt(1, 6)
            );
        }
    }

    private void callOrders() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            loggingProxy.execute(
                    "orders",
                    orderService,
                    "createOrder",
                    "CUST-" + randomNumber(),
                    "SKU-" + randomNumber(),
                    ThreadLocalRandom.current().nextInt(1, 4)
            );
        } else {
            loggingProxy.execute(
                    "orders",
                    orderService,
                    "getOrderStatus",
                    "ORD-" + randomNumber()
            );
        }
    }

    private void callPayments() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            loggingProxy.execute(
                    "payments",
                    paymentService,
                    "processPayment",
                    "ORD-" + randomNumber(),
                    ThreadLocalRandom.current().nextDouble(50.0, 500.0)
            );
        } else {
            loggingProxy.execute(
                    "payments",
                    paymentService,
                    "refundPayment",
                    "PAY-" + randomNumber(),
                    ThreadLocalRandom.current().nextDouble(10.0, 200.0)
            );
        }
    }

    private int randomNumber() {
        return ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}