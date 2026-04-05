package com.hotel.app.entity;

public class Service {
    private int service_id;
    private String name;
    private String category;
    private String desc;
    private double base_Price;
    private int is_Available; // Added missing semicolon

    public Service() {}

    public int getServiceId() {return service_id;}
    public void setServiceId(int service_id) {this.service_id = service_id;}

    public String getServiceName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}

    public String getDescription() {return desc;}
    public void setDesc(String desc) {this.desc = desc;}

    public double getPrice() {return base_Price;}
    public void setBase_Price(double base_Price) {this.base_Price = base_Price;}

    public int isIs_Available() {return is_Available;}
    public void setIs_Available(int is_Available) {this.is_Available = is_Available;}

    @Override
    public String toString() {
        return "Service{" +
                "service_id=" + service_id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", is_Available=" + is_Available +
                '}';
    }
}