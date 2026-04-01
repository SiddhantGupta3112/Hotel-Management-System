package com.hotel.app.entity;

public class Room {
    private long roomId;
    private int roomNumber;
    private long roomTypeId;
    private String roomTypeName;   // joined from ROOM_TYPES
    private double pricePerNight;  // joined from ROOM_TYPES
    private int floor;
    private String status;         // AVAILABLE | OCCUPIED | MAINTENANCE | RESERVED

    public Room() {}

    public long getRoomId()                  { return roomId; }
    public void setRoomId(long v)            { this.roomId = v; }

    public int getRoomNumber()               { return roomNumber; }
    public void setRoomNumber(int v)         { this.roomNumber = v; }

    public long getRoomTypeId()              { return roomTypeId; }
    public void setRoomTypeId(long v)        { this.roomTypeId = v; }

    public String getRoomTypeName()          { return roomTypeName; }
    public void setRoomTypeName(String v)    { this.roomTypeName = v; }

    public double getPricePerNight()         { return pricePerNight; }
    public void setPricePerNight(double v)   { this.pricePerNight = v; }

    public int getFloor()                    { return floor; }
    public void setFloor(int v)              { this.floor = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + roomTypeName + ")";
    }
}
