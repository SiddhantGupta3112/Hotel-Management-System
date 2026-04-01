package com.hotel.app.entity;

public class User{
    private long userId;
    private String email;
    private String passwordHash;
    private String name;
    private String phoneCountryCode;
    private String phoneNumber;
    private boolean isActive; // Changed to boolean
    private java.time.LocalDateTime createdAt; // Changed to LocalDateTime

    public User(){

    }

    // User ID
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    // Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Password Hash
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Phone Country Code
    public String getPhoneCountryCode() {
        return phoneCountryCode;
    }
    public void setPhoneCountryCode(String phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    // Phone Number
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Is Active
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    // Created At
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", email='" + email + "', name='" + name + "'}";
    }
}