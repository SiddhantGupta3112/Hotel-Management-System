package com.hotel.app.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking {

    private long bookingId;
    private long customerId;
    private long roomId;
    private String customerName;
    private int roomNumber;
    private String roomTypeName;
    private double pricePerNight;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    /**
      Valid Status Values:
     * PENDING          - customer just created the booking, waiting for admin approval
     * APPROVED         - admin approved the booking, customer can now request check-in
     * REJECTED         - admin rejected the booking (terminal state)
     * CHECKIN_PENDING  - customer requested check-in, waiting for admin approval
     * CHECKED_IN       - admin approved check-in, guest is in the room
     * CHECKOUT_PENDING - customer requested check-out, waiting for admin approval
     * CHECKED_OUT      - admin approved check-out (terminal state)
     * CANCELLED        - customer cancelled before check-in (only valid from PENDING or APPROVED)
     */
    private String bookingStatus;
    public Booking() {
    }

    public boolean canRequestCheckIn() {
        return "APPROVED".equals(bookingStatus) &&
                checkInDate != null &&
                !LocalDate.now().isBefore(checkInDate);
    }

    public boolean canRequestCheckOut() {
        return "CHECKED_IN".equals(bookingStatus);
    }

    public boolean canCancel() {
        return "PENDING".equals(bookingStatus) || "APPROVED".equals(bookingStatus);
    }

    public long getNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public double getTotalCost() {
        return getNights() * pricePerNight;
    }


    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}