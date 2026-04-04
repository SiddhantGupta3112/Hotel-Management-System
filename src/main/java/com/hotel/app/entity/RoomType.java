package com.hotel.app.entity;

public class RoomType {

    private long roomTypeId;
    private String typeName;
    private int capacity;
    private double pricePerNight;
    private String description;

    public RoomType() {
    }

    public RoomType(long roomTypeId, String typeName, int capacity,
                    double pricePerNight, String description) {
        this.roomTypeId = roomTypeId;
        this.typeName = typeName;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.description = description;
    }


    public long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        String formattedPrice = (this.pricePerNight % 1 == 0) ?
                String.format("%.0f", this.pricePerNight) :
                String.format("%.2f", this.pricePerNight);

        return this.typeName + " | Capacity: " + this.capacity +
                " | ₹" + formattedPrice + "/night";
    }
}