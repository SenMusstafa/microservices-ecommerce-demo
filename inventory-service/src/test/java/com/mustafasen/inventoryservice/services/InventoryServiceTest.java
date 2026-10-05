package com.mustafasen.inventoryservice.services;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.dtos.ProductRequest;
import com.mustafasen.inventoryservice.entities.Product;
import com.mustafasen.inventoryservice.exceptions.ProductAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(InventoryService.class)
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService.seedData();
    }

    private static ProductRequest request(String id, String name, int quantity) {
        ProductRequest request = new ProductRequest();
        request.setId(id);
        request.setName(name);
        request.setQuantity(quantity);
        return request;
    }

    @Test
    void seedData_isIdempotent() {
        inventoryService.updateProduct("product-1", request(null, "Renamed", 7));

        inventoryService.seedData();

        assertThat(inventoryService.listProducts()).hasSize(3);
        assertThat(inventoryService.getProduct("product-1").getName()).isEqualTo("Renamed");
    }

    @Test
    void createProduct_persistsNewProduct() {
        Product created = inventoryService.createProduct(request("product-9", "Widget", 4));

        assertThat(created.getId()).isEqualTo("product-9");
        assertThat(inventoryService.getStock("product-9").getQuantity()).isEqualTo(4);
    }

    @Test
    void createProduct_throws_whenIdAlreadyExists() {
        assertThatThrownBy(() -> inventoryService.createProduct(request("product-1", "Dup", 1)))
                .isInstanceOf(ProductAlreadyExistsException.class);
    }

    @Test
    void createProduct_throws_whenQuantityNegative() {
        assertThatThrownBy(() -> inventoryService.createProduct(request("product-8", "Bad", -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateProduct_changesNameAndQuantity() {
        Product updated = inventoryService.updateProduct("product-2", request(null, "Updated", 99));

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(inventoryService.getStock("product-2").getQuantity()).isEqualTo(99);
    }

    @Test
    void updateProduct_throws_forUnknownProduct() {
        assertThatThrownBy(() -> inventoryService.updateProduct("nope", request(null, "X", 1)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteProduct_removesProduct() {
        inventoryService.deleteProduct("product-3");

        assertThatThrownBy(() -> inventoryService.getStock("product-3"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteProduct_throws_forUnknownProduct() {
        assertThatThrownBy(() -> inventoryService.deleteProduct("nope"))
                .isInstanceOf(NoSuchElementException.class);
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
