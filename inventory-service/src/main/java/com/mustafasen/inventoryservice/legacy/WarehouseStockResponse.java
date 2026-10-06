package com.mustafasen.inventoryservice.legacy;

public class WarehouseStockResponse {

    private String sku;
    private int quantity;
    private String location;

    public WarehouseStockResponse() {
    }

    public WarehouseStockResponse(String sku, int quantity, String location) {
        this.sku = sku;
        this.quantity = quantity;
        this.location = location;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getLocation() {
        return location;
    }
}
