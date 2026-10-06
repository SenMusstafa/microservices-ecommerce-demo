package com.mustafasen.inventoryservice.legacy;

// The interface the rest of the service codes against (the Adapter pattern's
// "target"); how the legacy system is actually reached is hidden behind it.
public interface WarehouseGateway {

    WarehouseStockResponse getWarehouseStock(String sku);
}
