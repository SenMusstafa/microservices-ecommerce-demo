package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.services.InventoryService;
import com.mustafasen.inventoryservice.entities.Product;
import com.mustafasen.inventoryservice.exceptions.ProductAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void getStock_returnsStockAsJson() throws Exception {
        when(inventoryService.getStock("product-1")).thenReturn(new StockResponse("product-1", 50));

        mockMvc.perform(get("/api/v1/inventory/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void getStock_returns404_whenProductUnknown() throws Exception {
        when(inventoryService.getStock("unknown")).thenThrow(new NoSuchElementException("Unknown productId: unknown"));

        mockMvc.perform(get("/api/v1/inventory/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Unknown productId: unknown"));
    }

    @Test
    void listProducts_returnsProducts() throws Exception {
        when(inventoryService.listProducts()).thenReturn(List.of(new Product("product-1", "Product 1", 50)));

        mockMvc.perform(get("/api/v1/inventory/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("product-1"))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    void createProduct_returns201() throws Exception {
        when(inventoryService.createProduct(any())).thenReturn(new Product("p9", "Widget", 4));

        mockMvc.perform(post("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"p9\",\"name\":\"Widget\",\"quantity\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p9"));
    }

    @Test
    void createProduct_returns409_whenDuplicate() throws Exception {
        when(inventoryService.createProduct(any())).thenThrow(new ProductAlreadyExistsException("p9"));

        mockMvc.perform(post("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"p9\",\"name\":\"Widget\",\"quantity\":4}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createProduct_returns400_whenInvalid() throws Exception {
        when(inventoryService.createProduct(any())).thenThrow(new IllegalArgumentException("name must not be blank"));

        mockMvc.perform(post("/api/v1/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"p9\",\"name\":\"\",\"quantity\":4}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/inventory/products/p9"))
                .andExpect(status().isNoContent());
        verify(inventoryService).deleteProduct("p9");
    }

    @Test
    void reserve_returnsReservationResult() throws Exception {
        when(inventoryService.reserve(eq("product-1"), eq(5)))
                .thenReturn(new ReservationResponse(true, "Reserved", 45));

        mockMvc.perform(post("/api/v1/inventory/product-1/reserve").param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.remainingQuantity").value(45));
    }
}
