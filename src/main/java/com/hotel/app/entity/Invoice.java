package com.hotel.app.entity;

import java.time.LocalDate;

public class Invoice {
    private long invoiceId;
    private long bookingId;
    private double totalAmount;
    private double tax;
    private LocalDate generatedDate;

    // Denormalised fields for display
    private String customerName;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;

    public Invoice() {}

    public long getInvoiceId()               { return invoiceId; }
    public void setInvoiceId(long v)         { this.invoiceId = v; }

    public long getBookingId()               { return bookingId; }
    public void setBookingId(long v)         { this.bookingId = v; }

    public double getTotalAmount()           { return totalAmount; }
    public void setTotalAmount(double v)     { this.totalAmount = v; }

    public double getTax()                   { return tax; }
    public void setTax(double v)             { this.tax = v; }

    public LocalDate getGeneratedDate()      { return generatedDate; }
    public void setGeneratedDate(LocalDate v){ this.generatedDate = v; }

    public String getCustomerName()          { return customerName; }
    public void setCustomerName(String v)    { this.customerName = v; }

    public int getRoomNumber()               { return roomNumber; }
    public void setRoomNumber(int v)         { this.roomNumber = v; }

    public LocalDate getCheckInDate()        { return checkInDate; }
    public void setCheckInDate(LocalDate v)  { this.checkInDate = v; }

    public LocalDate getCheckOutDate()       { return checkOutDate; }
    public void setCheckOutDate(LocalDate v) { this.checkOutDate = v; }

    public String getBookingStatus()         { return bookingStatus; }
    public void setBookingStatus(String v)   { this.bookingStatus = v; }

    public double getGrandTotal()            { return totalAmount + tax; }
}
