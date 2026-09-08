package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.services.InventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public StockResponse getStock(@PathVariable String productId) {
        return inventoryService.getStock(productId);
    }

    @PostMapping("/{productId}/reserve")
    public ReservationResponse reserve(@PathVariable String productId, @RequestParam int quantity) {
        return inventoryService.reserve(productId, quantity);
    }
}
