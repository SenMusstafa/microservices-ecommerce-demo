package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.legacy.LegacyUnavailableException;
import com.mustafasen.inventoryservice.legacy.WarehouseGateway;
import com.mustafasen.inventoryservice.legacy.WarehouseStockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarehouseGateway warehouseGateway;

    @Test
    void returnsLegacyStockAsJson() throws Exception {
        when(warehouseGateway.getWarehouseStock("product-1"))
                .thenReturn(new WarehouseStockResponse("product-1", 120, "Istanbul-A1"));

        mockMvc.perform(get("/api/v1/inventory/products/product-1/warehouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(120))
                .andExpect(jsonPath("$.location").value("Istanbul-A1"));
    }

    @Test
    void unknownSku_returns404() throws Exception {
        when(warehouseGateway.getWarehouseStock("nope")).thenThrow(new NoSuchElementException("Unknown SKU: nope"));

        mockMvc.perform(get("/api/v1/inventory/products/nope/warehouse")).andExpect(status().isNotFound());
    }

    @Test
    void legacyDown_returns503() throws Exception {
        when(warehouseGateway.getWarehouseStock("product-1"))
                .thenThrow(new LegacyUnavailableException("Legacy warehouse is unreachable", null));

        mockMvc.perform(get("/api/v1/inventory/products/product-1/warehouse")).andExpect(status().isServiceUnavailable());
    }
}
