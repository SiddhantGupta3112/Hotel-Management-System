package com.hotel.app.entity;

public class Customer{
    private long customerId;
    private long userId;
    private String address;
    private String idProof;
    private String nationality;
    private int loyaltyPoints;

    private String name;
    private String email;
    private String phoneCountryCode;
    private String phoneNumber;

    public Customer() {}

    public long   getCustomerId()                    { return customerId; }
    public void   setCustomerId(long v)              { this.customerId = v; }

    public long   getUserId()                        { return userId; }
    public void   setUserId(long v)                  { this.userId = v; }

    public String getAddress()                       { return address; }
    public void   setAddress(String v)               { this.address = v; }

    public String getIdProof()                       { return idProof; }
    public void   setIdProof(String v)               { this.idProof = v; }

    public String getNationality()                   { return nationality; }
    public void   setNationality(String v)           { this.nationality = v; }

    public int    getLoyaltyPoints()                 { return loyaltyPoints; }
    public void   setLoyaltyPoints(int v)            { this.loyaltyPoints = v; }

    public String getName()                          { return name; }
    public void   setName(String v)                  { this.name = v; }

    public String getEmail()                         { return email; }
    public void   setEmail(String v)                 { this.email = v; }

    public String getPhoneCountryCode()              { return phoneCountryCode; }
    public void   setPhoneCountryCode(String v)      { this.phoneCountryCode = v; }

    public String getPhoneNumber()                   { return phoneNumber; }
    public void   setPhoneNumber(String v)           { this.phoneNumber = v; }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}