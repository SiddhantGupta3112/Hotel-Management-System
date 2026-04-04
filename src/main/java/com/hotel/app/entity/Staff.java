package com.hotel.app.entity;

public class Staff {
    // Staff specific fields
    private long staffId;
    private long userId;
    private long departmentId;
    private long managerId;        // Reference to Manager entity
    private String jobDescription; // e.g., "Front Desk Clerk", "Chef de Partie"
    private double salary;

    // Mirrored fields from USERS table
    private String name;
    private String email;
    private String phoneCountryCode;
    private String phoneNumber;

    public Staff() {}

    // Getters and Setters
    public long getStaffId() { return staffId; }
    public void setStaffId(long v) { this.staffId = v; }

    public long getUserId() { return userId; }
    public void setUserId(long v) { this.userId = v; }

    public long getDepartmentId() { return departmentId; }
    public void setDepartmentId(long v) { this.departmentId = v; }

    public long getManagerId() { return managerId; }
    public void setManagerId(long v) { this.managerId = v; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String v) { this.jobDescription = v; }

    public double getSalary() { return salary; }
    public void setSalary(double v) { this.salary = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getPhoneCountryCode() { return phoneCountryCode; }
    public void setPhoneCountryCode(String v) { this.phoneCountryCode = v; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String v) { this.phoneNumber = v; }

    @Override
    public String toString() {
        return name + " (" + jobDescription + ")";
    }
}