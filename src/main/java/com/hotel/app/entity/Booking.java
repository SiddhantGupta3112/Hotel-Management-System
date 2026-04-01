package com.hotel.app.entity;

import java.time.LocalDate;

public class Booking {
    private long bookingId;
    private long customerId;
    private long roomId;
    private String customerName;   // joined from USERS via CUSTOMERS
    private int roomNumber;        // joined from ROOMS
    private String roomTypeName;   // joined from ROOM_TYPES
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;  // CONFIRMED | CHECKED_IN | CHECKED_OUT | CANCELLED

    public Booking() {}

    public long getBookingId()               { return bookingId; }
    public void setBookingId(long v)         { this.bookingId = v; }

    public long getCustomerId()              { return customerId; }
    public void setCustomerId(long v)        { this.customerId = v; }

    public long getRoomId()                  { return roomId; }
    public void setRoomId(long v)            { this.roomId = v; }

    public String getCustomerName()          { return customerName; }
    public void setCustomerName(String v)    { this.customerName = v; }

    public int getRoomNumber()               { return roomNumber; }
    public void setRoomNumber(int v)         { this.roomNumber = v; }

    public String getRoomTypeName()          { return roomTypeName; }
    public void setRoomTypeName(String v)    { this.roomTypeName = v; }

    public LocalDate getBookingDate()        { return bookingDate; }
    public void setBookingDate(LocalDate v)  { this.bookingDate = v; }

    public LocalDate getCheckInDate()        { return checkInDate; }
    public void setCheckInDate(LocalDate v)  { this.checkInDate = v; }

    public LocalDate getCheckOutDate()       { return checkOutDate; }
    public void setCheckOutDate(LocalDate v) { this.checkOutDate = v; }

    public String getBookingStatus()         { return bookingStatus; }
    public void setBookingStatus(String v)   { this.bookingStatus = v; }

    /** Convenience: number of nights */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
