package com.backend.loggingproxy.service;

import com.backend.loggingproxy.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class OrderService implements MicroserviceProxy<Object> {

    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();

    @Override
    public Object execute(String operation, Object... params) {
        sleepRandom(30, 120);
        return switch (operation) {
            case "create" -> {
                String orderId = UUID.randomUUID().toString();
                Map<String, Object> order = Map.of(
                        "orderId", orderId,
                        "sku", params[0],
                        "quantity", params[1],
                        "status", "CREATED"
                );
                orders.put(orderId, order);
                yield order;
            }
            case "get" -> {
                String orderId = (String) params[0];
                Map<String, Object> order = orders.get(orderId);
                if (order == null) throw new RuntimeException("Orden no encontrada: " + orderId);
                yield order;
            }
            case "list" -> orders.values();
            default -> throw new UnsupportedOperationException("Operación no soportada: " + operation);
        };
    }

    private void sleepRandom(int min, int max) {
        try { Thread.sleep(min + (int) (Math.random() * (max - min))); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
