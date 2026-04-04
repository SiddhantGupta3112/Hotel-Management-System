package com.hotel.app.entity;

public class Room {

    private long roomId;
    private int roomNumber;
    private long roomTypeId;
    private String roomTypeName;
    private double pricePerNight;
    private int capacity;
    private int floor;
    private String status;

    public Room() {
    }

    public Room(long roomId, int roomNumber, long roomTypeId, String roomTypeName,
                double pricePerNight, int capacity, int floor, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.floor = floor;
        this.status = status;
    }


    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String formattedPrice = (this.pricePerNight % 1 == 0) ?
                String.format("%.0f", this.pricePerNight) :
                String.format("%.2f", this.pricePerNight);

        return "Room " + this.roomNumber + " — " + this.roomTypeName +
                " | Floor " + this.floor +
                " | ₹" + formattedPrice + "/night";
    }
}