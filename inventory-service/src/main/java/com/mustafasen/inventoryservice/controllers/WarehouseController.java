package com.mustafasen.inventoryservice.controllers;

import com.mustafasen.inventoryservice.legacy.WarehouseGateway;
import com.mustafasen.inventoryservice.legacy.WarehouseStockResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST/JSON facade over the legacy SOAP warehouse system.
@RestController
@RequestMapping("/api/v1/inventory/products")
public class WarehouseController {

    private final WarehouseGateway warehouseGateway;

    public WarehouseController(WarehouseGateway warehouseGateway) {
        this.warehouseGateway = warehouseGateway;
    }

    @GetMapping("/{productId}/warehouse")
    public WarehouseStockResponse warehouseStock(@PathVariable String productId) {
        return warehouseGateway.getWarehouseStock(productId);
    }
}
