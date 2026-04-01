package com.hotel.app.entity;

public class RoomType {
    private long roomTypeId;
    private String typeName;
    private int capacity;
    private double pricePerNight;
    private String description;

    public RoomType() {}

    public long getRoomTypeId()              { return roomTypeId; }
    public void setRoomTypeId(long v)        { this.roomTypeId = v; }

    public String getTypeName()              { return typeName; }
    public void setTypeName(String v)        { this.typeName = v; }

    public int getCapacity()                 { return capacity; }
    public void setCapacity(int v)           { this.capacity = v; }

    public double getPricePerNight()         { return pricePerNight; }
    public void setPricePerNight(double v)   { this.pricePerNight = v; }

    public String getDescription()           { return description; }
    public void setDescription(String v)     { this.description = v; }

    @Override
    public String toString() { return typeName; }
}
