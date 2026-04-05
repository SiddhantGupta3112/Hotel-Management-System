package com.hotel.app.entity;

public class Department {
    private long departmentId;
    private String departmentName;
    private long headManagerId; // The ID of the Manager who leads this dept

    public Department() {}

    public long   getDepartmentId()            { return departmentId; }
    public void   setDepartmentId(long v)      { this.departmentId = v; }

    public String getDepartmentName()          { return departmentName; }
    public void   setDepartmentName(String v)  { this.departmentName = v; }

    public long   getHeadManagerId()           { return headManagerId; }
    public void   setHeadManagerId(long v)     { this.headManagerId = v; }
}