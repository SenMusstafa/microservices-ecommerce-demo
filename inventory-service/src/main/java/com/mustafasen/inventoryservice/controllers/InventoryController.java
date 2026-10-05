package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.dtos.ProductRequest;
import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import com.mustafasen.inventoryservice.entities.Product;
import com.mustafasen.inventoryservice.services.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return inventoryService.listProducts();
    }

    @GetMapping("/products/{productId}")
    public Product getProduct(@PathVariable String productId) {
        return inventoryService.getProduct(productId);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody ProductRequest request) {
        return inventoryService.createProduct(request);
    }

    @PutMapping("/products/{productId}")
    public Product updateProduct(@PathVariable String productId, @RequestBody ProductRequest request) {
        return inventoryService.updateProduct(productId, request);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String productId) {
        inventoryService.deleteProduct(productId);
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
