package com.hotel.app.service;

import com.hotel.app.entity.Room;
import com.hotel.app.entity.RoomType;
import com.hotel.app.repository.RoomRepository;
import com.hotel.app.repository.RoomTypeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RoomService {

    private final RoomRepository roomRepo = new RoomRepository();
    private final RoomTypeRepository roomTypeRepo = new RoomTypeRepository();

    public static class Result<T> {
        private final boolean success;
        private final T value;
        private final String error;

        private Result(boolean success, T value, String error) {
            this.success = success;
            this.value = value;
            this.error = error;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(true, value, null);
        }

        public static <T> Result<T> failure(String message) {
            return new Result<>(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public T getValue() { return value; }
        public String getError() { return error; }
    }

    //  Admin: Room Type Management

    public Result<Long> addRoomType(String typeName, int capacity, double pricePerNight, String description) {
        if (typeName == null || typeName.trim().isEmpty()) return Result.failure("Type name cannot be blank.");
        if (capacity < 1) return Result.failure("Capacity must be at least 1.");
        if (pricePerNight <= 0) return Result.failure("Price must be greater than zero.");
        if (roomTypeRepo.typeNameExists(typeName)) return Result.failure("Room type '" + typeName + "' already exists.");

        long id = roomTypeRepo.save(typeName, capacity, pricePerNight, description);
        return id != -1 ? Result.success(id) : Result.failure("Database error while saving room type.");
    }

    public Result<Void> updateRoomType(long id, String typeName, int capacity, double pricePerNight, String description) {
        Optional<RoomType> existing = roomTypeRepo.findById(id);
        if (existing.isEmpty()) return Result.failure("Room type not found.");

        if (typeName == null || typeName.trim().isEmpty()) return Result.failure("Name cannot be blank.");
        if (!existing.get().getTypeName().equalsIgnoreCase(typeName) && roomTypeRepo.typeNameExists(typeName)) {
            return Result.failure("Another room type already uses this name.");
        }

        boolean updated = roomTypeRepo.update(id, typeName, capacity, pricePerNight, description);
        return updated ? Result.success(null) : Result.failure("Update failed.");
    }

    public Result<Void> deleteRoomType(long id) {
        if (roomTypeRepo.findById(id).isEmpty()) return Result.failure("Room type does not exist.");

        boolean deleted = roomTypeRepo.delete(id);
        return deleted ? Result.success(null) : Result.failure("Cannot delete: rooms are still assigned to this type.");
    }

    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepo.findAll();
    }

    // Admin: Room Management

    public Result<Long> addRoom(int roomNumber, long roomTypeId, int floor) {
        if (roomNumber <= 0) return Result.failure("Invalid room number.");
        if (floor < 0) return Result.failure("Floor cannot be negative.");
        if (roomTypeRepo.findById(roomTypeId).isEmpty()) return Result.failure("Selected room type does not exist.");
        if (roomRepo.roomNumberExists(roomNumber)) return Result.failure("Room number " + roomNumber + " is already taken.");

        long id = roomRepo.save(roomNumber, roomTypeId, floor);
        return id != -1 ? Result.success(id) : Result.failure("Database error while saving room.");
    }

    public Result<Void> updateRoom(long roomId, int roomNumber, long roomTypeId, int floor) {
        Optional<Room> existing = roomRepo.findbyid(roomId);
        if (existing.isEmpty()) return Result.failure("Room not found.");

        if (roomNumber <= 0) return Result.failure("Invalid room number.");

        if (existing.get().getRoomNumber() != roomNumber && roomRepo.roomNumberExists(roomNumber)) {
            return Result.failure("Room number " + roomNumber + " is already assigned to another room.");
        }

        boolean updated = roomRepo.update(roomId, roomNumber, roomTypeId, floor);
        return updated ? Result.success(null) : Result.failure("Update failed.");
    }

    public Result<Void> changeRoomStatus(long roomId, String status) {
        List<String> validStatuses = Arrays.asList("AVAILABLE", "OCCUPIED", "MAINTENANCE", "RESERVED");
        if (!validStatuses.contains(status.toUpperCase())) {
            return Result.failure("Invalid status: " + status);
        }

        boolean updated = roomRepo.updateStatus(roomId, status.toUpperCase());
        return updated ? Result.success(null) : Result.failure("Could not update status.");
    }

    public Result<Void> deleteRoom(long roomId) {
        if (roomRepo.findbyid(roomId).isEmpty()) return Result.failure("Room not found.");

        boolean deleted = roomRepo.delete(roomId);
        return deleted ? Result.success(null) : Result.failure("Cannot delete: room has existing bookings.");
    }

    public List<Room> getAllRooms() {
        return roomRepo.findall();
    }

    //  Customer Access


    public List<Room> getAvailableRooms() {
        return roomRepo.findAvailable();
    }
}