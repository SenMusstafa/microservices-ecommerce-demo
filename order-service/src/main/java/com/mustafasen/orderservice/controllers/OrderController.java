package com.mustafasen.orderservice.controllers;

import com.mustafasen.orderservice.clients.InventoryClient;
import com.mustafasen.orderservice.dtos.CreateOrderRequest;
import com.mustafasen.orderservice.dtos.OrderResponse;
import com.mustafasen.orderservice.dtos.ReservationResult;
import com.mustafasen.orderservice.events.OrderCreatedEvent;
import com.mustafasen.orderservice.events.OrderEventPublisher;
import com.mustafasen.orderservice.entities.OrderEntity;
import com.mustafasen.orderservice.repositories.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final InventoryClient inventoryClient;
    private final OrderEventPublisher orderEventPublisher;

    private final OrderRepository orderRepository;

    public OrderController(InventoryClient inventoryClient, OrderEventPublisher orderEventPublisher,
                           OrderRepository orderRepository) {
        this.inventoryClient = inventoryClient;
        this.orderEventPublisher = orderEventPublisher;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<OrderEntity> listOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        ReservationResult reservation = inventoryClient.reserve(request.getProductId(), request.getQuantity());

        String orderId = UUID.randomUUID().toString();

        if (!reservation.isSuccess()) {
            return saved(orderId, request, "REJECTED", reservation.getMessage());
        }

        OrderResponse response = saved(orderId, request, "CREATED",
                "Order created, remaining stock: " + reservation.getRemainingQuantity());

        orderEventPublisher.publishOrderCreated(
                new OrderCreatedEvent(orderId, request.getProductId(), request.getQuantity(), "CREATED"));

        return response;
    }

    private OrderResponse saved(String orderId, CreateOrderRequest request, String status, String message) {
        orderRepository.save(new OrderEntity(orderId, request.getProductId(), request.getQuantity(), status, message));
        return new OrderResponse(orderId, request.getProductId(), request.getQuantity(), status, message);
    }
}
