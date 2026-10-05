package com.mustafasen.orderservice.controllers;

import com.mustafasen.orderservice.clients.InventoryClient;
import com.mustafasen.orderservice.dtos.CreateOrderRequest;
import com.mustafasen.orderservice.dtos.OrderResponse;
import com.mustafasen.orderservice.dtos.ReservationResult;
import com.mustafasen.orderservice.events.OrderCreatedEvent;
import com.mustafasen.orderservice.events.OrderEventPublisher;
import com.mustafasen.orderservice.entities.OrderEntity;
import com.mustafasen.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OrderRepository orderRepository;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(inventoryClient, orderEventPublisher, orderRepository);
    }

    private static ReservationResult reservation(boolean success, String message, int remaining) {
        ReservationResult result = new ReservationResult();
        result.setSuccess(success);
        result.setMessage(message);
        result.setRemainingQuantity(remaining);
        return result;
    }

    @Test
    void createOrder_returnsCreatedAndPublishesEvent_whenReservationSucceeds() {
        when(inventoryClient.reserve("product-1", 1)).thenReturn(reservation(true, "Reserved", 49));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("product-1");
        request.setQuantity(1);

        OrderResponse response = orderController.createOrder(request);

        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getProductId()).isEqualTo("product-1");
        assertThat(response.getQuantity()).isEqualTo(1);
        assertThat(response.getOrderId()).isNotBlank();
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderEventPublisher).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_returnsRejectedAndDoesNotPublishEvent_whenReservationFails() {
        when(inventoryClient.reserve("product-3", 1)).thenReturn(reservation(false, "Insufficient stock", 0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("product-3");
        request.setQuantity(1);

        OrderResponse response = orderController.createOrder(request);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getMessage()).isEqualTo("Insufficient stock");
        verify(orderRepository).save(any(OrderEntity.class));
        verifyNoInteractions(orderEventPublisher);
    }
}
