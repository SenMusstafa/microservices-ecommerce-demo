package com.mustafasen.orderservice.dtos;

// Mirrors inventory-service's ReservationResponse — kept as a separate
// contract per service rather than a shared module, since these two
// services are allowed to evolve independently.
public class ReservationResult {

    private boolean success;
    private String message;
    private int remainingQuantity;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
