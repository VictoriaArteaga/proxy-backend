package com.backend.loggingproxy.controller;

import com.backend.loggingproxy.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> ordersProxy;
    private final MicroserviceProxy<Object> paymentsProxy;

    public ServiceController(
            @Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
            @Qualifier("ordersProxy") MicroserviceProxy<Object> ordersProxy,
            @Qualifier("paymentsProxy") MicroserviceProxy<Object> paymentsProxy
    ) {
        this.inventoryProxy = inventoryProxy;
        this.ordersProxy = ordersProxy;
        this.paymentsProxy = paymentsProxy;
    }

    @PostMapping("/inventory/{operation}")
    public ResponseEntity<Object> inventory(@PathVariable String operation,
                                            @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(inventoryProxy.execute(operation, extractParams(body)));
    }

    @PostMapping("/orders/{operation}")
    public ResponseEntity<Object> orders(@PathVariable String operation,
                                         @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(ordersProxy.execute(operation, extractParams(body)));
    }

    @PostMapping("/payments/{operation}")
    public ResponseEntity<Object> payments(@PathVariable String operation,
                                           @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(paymentsProxy.execute(operation, extractParams(body)));
    }

    private Object[] extractParams(Map<String, Object> body) {
        if (body == null || !body.containsKey("params")) return new Object[0];
        Object raw = body.get("params");
        if (raw instanceof List<?> list) return list.toArray();
        return new Object[]{raw};
    }
}
