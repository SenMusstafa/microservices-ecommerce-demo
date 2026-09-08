package com.mustafasen.orderservice.controllers;

import com.mustafasen.orderservice.clients.InventoryClient;
import com.mustafasen.orderservice.dtos.CreateOrderRequest;
import com.mustafasen.orderservice.dtos.OrderResponse;
import com.mustafasen.orderservice.dtos.ReservationResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final InventoryClient inventoryClient;

    public OrderController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        ReservationResult reservation = inventoryClient.reserve(request.getProductId(), request.getQuantity());

        String orderId = UUID.randomUUID().toString();

        if (!reservation.isSuccess()) {
            return new OrderResponse(orderId, request.getProductId(), request.getQuantity(),
                    "REJECTED", reservation.getMessage());
        }

        return new OrderResponse(orderId, request.getProductId(), request.getQuantity(),
                "CREATED", "Order created, remaining stock: " + reservation.getRemainingQuantity());
    }
}
