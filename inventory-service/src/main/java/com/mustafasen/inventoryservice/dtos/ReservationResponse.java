package com.mustafasen.inventoryservice.dtos;

public class ReservationResponse {

    private boolean success;
    private String message;
    private int remainingQuantity;

    public ReservationResponse() {
    }

    public ReservationResponse(boolean success, String message, int remainingQuantity) {
        this.success = success;
        this.message = message;
        this.remainingQuantity = remainingQuantity;
    }

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
