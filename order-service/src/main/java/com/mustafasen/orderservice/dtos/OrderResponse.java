package com.mustafasen.orderservice.dtos;

public class OrderResponse {

    private String orderId;
    private String productId;
    private int quantity;
    private String status;
    private String message;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, String productId, int quantity, String status, String message) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
