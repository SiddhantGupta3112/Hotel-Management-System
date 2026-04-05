package com.hotel.app.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceRequest {

    private int requestId;
    private int bookingId;
    private int serviceId;
    private String serviceName;
    private int quantity;
    private String notes;
    private String status;
    private String guestName;
    private int roomNumber;
    private double servicePrice;
    private LocalDateTime requestedAt;

    public ServiceRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public double getServicePrice() { return servicePrice; }
    public void setServicePrice(double servicePrice) { this.servicePrice = servicePrice; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }


    public String getChargeDisplay() {
        return String.format("$%.2f", (this.servicePrice * this.quantity));
    }


    public String getRequestedAtFormatted() {
        if (requestedAt == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        return requestedAt.format(formatter);
    }

    @Override
    public String toString() {
        return "ServiceUsage{" +
                "requestId=" + requestId +
                ", guest='" + guestName + '\'' +
                ", room='" + roomNumber + '\'' +
                ", service='" + serviceName + '\'' +
                ", total=" + getChargeDisplay() +
                '}';
    }
}