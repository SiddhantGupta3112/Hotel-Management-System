package com.hotel.app.entity;

import java.time.LocalDate;

public class Payment {

    private long paymentId;
    private long bookingId;
    private long methodId;
    private String methodName;
    private double amount;
    private LocalDate paymentDate;
    private String status;

    public Payment() {
    }

    public Payment(long paymentId, long bookingId, long methodId, String methodName, double amount, LocalDate paymentDate, String status) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.methodId = methodId;
        this.methodName = methodName;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public long getMethodId() {
        return methodId;
    }

    public void setMethodId(long methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}