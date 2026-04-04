package com.hotel.app.entity;

public class Customer{

    private long customerId;
    private long userId;
    private String name;
    private String email;
    private String address;
    private String idProof;
    private String nationality;
    private int loyaltyPoints;
    private String phoneCountryCode;
    private String phoneNumber;

    public Customer(){}

    public Customer(long customerId, long userId, String name, String email, String address, String idProof, String nationality, int loyaltyPoints){
        this.customerId = customerId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.idProof = idProof;
        this.nationality = nationality;
        this.loyaltyPoints = loyaltyPoints;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getUserId(){
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdProof(){
        return idProof;
    }

    public void setIdProof(String idProof){
        this.idProof = idProof;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getPhoneCountryCode()              { return phoneCountryCode; }
    public void   setPhoneCountryCode(String v)      { this.phoneCountryCode = v; }

    public String getPhoneNumber()                   { return phoneNumber; }
    public void   setPhoneNumber(String v)           { this.phoneNumber = v; }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, email);
    }
    }



