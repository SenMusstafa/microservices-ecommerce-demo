package com.mustafasen.inventoryservice.services;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        inventoryService.seedData();
    }

    @Test
    void getStock_returnsSeededQuantity_forKnownProduct() {
        StockResponse response = inventoryService.getStock("product-1");

        assertThat(response.getProductId()).isEqualTo("product-1");
        assertThat(response.getQuantity()).isEqualTo(50);
    }

    @Test
    void getStock_throws_forUnknownProduct() {
        assertThatThrownBy(() -> inventoryService.getStock("product-does-not-exist"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void reserve_succeedsAndDecrementsStock_whenEnoughAvailable() {
        ReservationResponse response = inventoryService.reserve("product-1", 20);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getRemainingQuantity()).isEqualTo(30);
        assertThat(inventoryService.getStock("product-1").getQuantity()).isEqualTo(30);
    }

    @Test
    void reserve_fails_whenRequestedQuantityExceedsStock() {
        ReservationResponse response = inventoryService.reserve("product-2", 999);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Insufficient stock");
        assertThat(response.getRemainingQuantity()).isEqualTo(10);
        assertThat(inventoryService.getStock("product-2").getQuantity()).isEqualTo(10);
    }

    @Test
    void reserve_fails_whenStockIsZero() {
        ReservationResponse response = inventoryService.reserve("product-3", 1);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getRemainingQuantity()).isEqualTo(0);
    }

    @Test
    void reserve_fails_forUnknownProduct() {
        ReservationResponse response = inventoryService.reserve("product-does-not-exist", 1);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Unknown productId");
    }
}
