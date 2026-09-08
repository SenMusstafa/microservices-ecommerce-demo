package com.mustafasen.orderservice.clients;

import com.mustafasen.orderservice.dtos.ReservationResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// "inventory-service" is resolved via Eureka — no hardcoded host/port.
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/{productId}/reserve")
    ReservationResult reserve(@PathVariable("productId") String productId, @RequestParam("quantity") int quantity);
}
