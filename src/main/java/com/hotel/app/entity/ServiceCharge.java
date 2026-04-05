package com.hotel.app.entity;

public class ServiceCharge {

    private long chargeId;
    private long requestId;
    private long bookingId;
    private double price;
    
    private String serviceName;   // Joined from SERVICE_REQUESTS → SERVICES
    private int quantity;
    private String chargedAt;     // Formatted timestamp

    public ServiceCharge() {}

    // Existing Getters and Setters
    public long getChargeId() { return chargeId; }
    public void setChargeId(long chargeId) { this.chargeId = chargeId; }

    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // New Getters and Setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getChargedAt() { return chargedAt; }
    public void setChargedAt(String chargedAt) { this.chargedAt = chargedAt; }
}