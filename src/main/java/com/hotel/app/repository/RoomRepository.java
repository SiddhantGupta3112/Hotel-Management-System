package com.hotel.app.repository;

import com.hotel.app.entity.Room;
import com.hotel.app.entity.RoomType;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.floor, r.status, " +
                     "       rt.room_type_id, rt.type_name, rt.price_per_night " +
                     "FROM ROOMS r " +
                     "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id " +
                     "ORDER BY r.room_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Room room = mapRow(rs);
                list.add(room);
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    public List<RoomType> findAllTypes() {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT room_type_id, type_name, capacity, price_per_night, description " +
                     "FROM ROOM_TYPES ORDER BY type_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RoomType rt = new RoomType();
                rt.setRoomTypeId(rs.getLong("room_type_id"));
                rt.setTypeName(rs.getString("type_name"));
                rt.setCapacity(rs.getInt("capacity"));
                rt.setPricePerNight(rs.getDouble("price_per_night"));
                rt.setDescription(rs.getString("description"));
                list.add(rt);
            }
        } catch (SQLException e) {
            System.err.println("RoomRepository.findAllTypes: " + e.getMessage());
        }
        return list;
    }

    public boolean save(int roomNumber, long roomTypeId, int floor) {
        String sql = "INSERT INTO ROOMS (room_number, room_type_id, floor, status) " +
                     "VALUES (?, ?, ?, 'AVAILABLE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomNumber);
            stmt.setLong(2, roomTypeId);
            stmt.setInt(3, floor);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RoomRepository.save: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(long roomId, String status) {
        String sql = "UPDATE ROOMS SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RoomRepository.updateStatus: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(long roomId) {
        String sql = "DELETE FROM ROOMS WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RoomRepository.delete: " + e.getMessage());
            return false;
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getLong("room_id"));
        r.setRoomNumber(rs.getInt("room_number"));
        r.setFloor(rs.getInt("floor"));
        r.setStatus(rs.getString("status"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setRoomTypeName(rs.getString("type_name"));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        return r;
    }
}
