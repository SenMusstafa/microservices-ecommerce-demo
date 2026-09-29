package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.services.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
    void getStock_propagatesException_whenProductUnknown() {
        // The controller has no @ExceptionHandler for this, so the exception
        // reaches the servlet layer uncaught (a real container turns this
        // into a 500 response; MockMvc surfaces the raw exception instead).
        when(inventoryService.getStock("unknown")).thenThrow(new NoSuchElementException("Unknown productId: unknown"));

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/inventory/unknown")))
                .hasRootCauseInstanceOf(NoSuchElementException.class);
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
