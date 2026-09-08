package com.mustafasen.orderservice.dtos;

public class CreateOrderRequest {

    private String productId;
    private int quantity;

    public CreateOrderRequest() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
