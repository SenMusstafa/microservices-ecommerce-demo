package com.mustafasen.inventoryservice.services;
import java.util.NoSuchElementException;

import com.mustafasen.inventoryservice.dtos.ReservationResponse;
import com.mustafasen.inventoryservice.dtos.StockResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    // In-memory stock for the demo. A later phase will back this with PostgreSQL.
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    @PostConstruct
    public void seedData() {
        stock.put("product-1", 50);
        stock.put("product-2", 10);
        stock.put("product-3", 0);
    }

    public StockResponse getStock(String productId) {
        Integer quantity = stock.get(productId);
        if (quantity == null) {
            throw new NoSuchElementException("Unknown productId: " + productId);
        }
        return new StockResponse(productId, quantity);
    }

    public synchronized ReservationResponse reserve(String productId, int requestedQuantity) {
        Integer available = stock.get(productId);
        if (available == null) {
            return new ReservationResponse(false, "Unknown productId: " + productId, 0);
        }
        if (available < requestedQuantity) {
            return new ReservationResponse(false, "Insufficient stock", available);
        }
        int remaining = available - requestedQuantity;
        stock.put(productId, remaining);
        return new ReservationResponse(true, "Reserved", remaining);
    }
}
