package com.backend.loggingproxy.service;

import com.backend.loggingproxy.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
public class PaymentService implements MicroserviceProxy<Object> {

    @Value("${app.payment.failure-rate:0.10}")
    private double failureRate;

    @Override
    public Object execute(String operation, Object... params) {
        sleepRandom(50, 200);

        // 10% de fallas intencionales
        if (Math.random() < failureRate) {
            throw new RuntimeException("Payment gateway timeout");
        }

        return switch (operation) {
            case "charge" -> {
                String orderId = (String) params[0];
                double amount = ((Number) params[1]).doubleValue();
                yield Map.of(
                        "transactionId", UUID.randomUUID().toString(),
                        "orderId", orderId,
                        "amount", amount,
                        "status", "APPROVED"
                );
            }
            case "refund" -> {
                String transactionId = (String) params[0];
                yield Map.of("transactionId", transactionId, "status", "REFUNDED");
            }
            default -> throw new UnsupportedOperationException("Operación no soportada: " + operation);
        };
    }

    private void sleepRandom(int min, int max) {
        try { Thread.sleep(min + (int) (Math.random() * (max - min))); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
