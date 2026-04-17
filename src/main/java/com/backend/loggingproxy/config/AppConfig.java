package com.backend.loggingproxy.config;

import com.backend.loggingproxy.logging.LogStore;
import com.backend.loggingproxy.proxy.LoggingProxy;
import com.backend.loggingproxy.proxy.MicroserviceProxy;
import com.backend.loggingproxy.service.InventoryService;
import com.backend.loggingproxy.service.OrderService;
import com.backend.loggingproxy.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean(name = "inventoryProxy")
    public MicroserviceProxy<Object> inventoryProxy(InventoryService service, LogStore logStore) {
        return new LoggingProxy<>("inventory", service, logStore);
    }

    @Bean(name = "ordersProxy")
    public MicroserviceProxy<Object> ordersProxy(OrderService service, LogStore logStore) {
        return new LoggingProxy<>("orders", service, logStore);
    }

    @Bean(name = "paymentsProxy")
    public MicroserviceProxy<Object> paymentsProxy(PaymentService service, LogStore logStore) {
        return new LoggingProxy<>("payments", service, logStore);
    }
}
