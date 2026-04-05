package com.hotel.app.entity;

public class ServiceCharge {

    private long chargeId;
    private long requestId;
    private long bookingId;
    private double price;

    public ServiceCharge(){}

    public long getChargeId() {return chargeId;}
    public void setChargeId(long chargeId) {this.chargeId=chargeId;}

    public long getRequestId() {return requestId;}
    public void setRequestId(long requestId) {this.requestId=requestId;}

    public long getBookingId() {return bookingId;}
    public void setBookingId(long bookingId) {this.bookingId=bookingId;}

    public double getPrice() {return price;}
    public void setPrice(double price) {this.price=price;}


}
