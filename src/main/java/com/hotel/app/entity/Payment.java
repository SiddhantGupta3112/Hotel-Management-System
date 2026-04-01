package com.hotel.app.entity;

import java.time.LocalDate;

public class Payment {
    private long paymentId;
    private long bookingId;
    private long methodId;
    private String methodName;   // joined from PAYMENT_METHODS
    private double amount;
    private LocalDate paymentDate;
    private String status;       // COMPLETED | PENDING | FAILED | REFUNDED

    public Payment() {}

    public long getPaymentId()               { return paymentId; }
    public void setPaymentId(long v)         { this.paymentId = v; }

    public long getBookingId()               { return bookingId; }
    public void setBookingId(long v)         { this.bookingId = v; }

    public long getMethodId()                { return methodId; }
    public void setMethodId(long v)          { this.methodId = v; }

    public String getMethodName()            { return methodName; }
    public void setMethodName(String v)      { this.methodName = v; }

    public double getAmount()                { return amount; }
    public void setAmount(double v)          { this.amount = v; }

    public LocalDate getPaymentDate()        { return paymentDate; }
    public void setPaymentDate(LocalDate v)  { this.paymentDate = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }
}
