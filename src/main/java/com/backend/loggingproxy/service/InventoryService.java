package com.backend.loggingproxy.service;

import com.backend.loggingproxy.proxy.MicroserviceProxy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService implements MicroserviceProxy<Object> {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public InventoryService() {
        stock.put("sku-001", 100);
        stock.put("sku-002", 50);
        stock.put("sku-003", 200);
    }

    @Override
    public Object execute(String operation, Object... params) {
        sleepRandom(20, 80);
        return switch (operation) {
            case "check" -> {
                String sku = (String) params[0];
                yield Map.of("sku", sku, "available", stock.getOrDefault(sku, 0));
            }
            case "reserve" -> {
                String sku = (String) params[0];
                int qty = ((Number) params[1]).intValue();
                int current = stock.getOrDefault(sku, 0);
                if (current < qty) throw new RuntimeException("Stock insuficiente para " + sku);
                stock.put(sku, current - qty);
                yield Map.of("sku", sku, "reserved", qty, "remaining", stock.get(sku));
            }
            case "list" -> new HashMap<>(stock);
            default -> throw new UnsupportedOperationException("Operación no soportada: " + operation);
        };
    }

    private void sleepRandom(int min, int max) {
        try { Thread.sleep(min + (int) (Math.random() * (max - min))); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
