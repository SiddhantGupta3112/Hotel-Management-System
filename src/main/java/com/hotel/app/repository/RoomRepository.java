package com.hotel.app.repository;

import com.hotel.app.entity.Room;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomRepository{

    private final String base_query = "SELECT r.room_id, r.room_number, r.floor, r.status, " +
            "rt.room_type_id, rt.type_name, rt.price_per_night, rt.capacity " +
            "FROM ROOMS r JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id ";

    public List<Room> findall(){
        List<Room> rooms = new ArrayList<>();
        String sql = base_query + " order by r.room_number";

        try(Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql)){

            while(rs.next()){
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public int getRoomCount(){
        String sql = "SELECT COUNT(room_id) AS Total_rooms FROM Rooms";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt("Total_rooms");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching count of rooms");
            e.printStackTrace();
        }

        return 0;
    }

    public int getRoomCountByStatus(String status){
        String sql = """ 
                        SELECT COUNT(room_id) AS Total_rooms 
                        FROM Rooms 
                        WHERE STATUS = ?
                        """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt("Total_rooms");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching count of rooms by status");
            e.printStackTrace();
        }

        return 0;
    }


    public List<Room> findAvailable() {
        List<Room> rooms = new ArrayList<>();
        String sql = base_query + " where r.status = 'AVAILABLE' order by r.room_number";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public Optional<Room> findbyid(long roomId) {
        String sql = base_query + " where r.room_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean roomNumberExists(int roomnumber){
        String sql = "Select 1 from rooms where room_number = ?";
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1,roomnumber);
            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long save(int roomnumber, long roomtypeId, int floor) {
        String sql = "INSERT INTO ROOMS (room_number, room_type_id, floor, status) VALUES (?, ?, ?, 'AVAILABLE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ROOM_ID"})) {

            ps.setInt(1, roomnumber);
            ps.setLong(2, roomtypeId);
            ps.setInt(3, floor);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(long roomid, int roomnumber, long roomtypeid, int floor) {
        String sql = "UPDATE ROOMS SET room_number = ?, room_type_id = ?, floor = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomnumber);
            pstmt.setLong(2, roomtypeid);
            pstmt.setInt(3, floor);
            pstmt.setLong(4, roomid);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(long roomid, String status) {
        String sql = "UPDATE ROOMS SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setLong(2, roomid);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(long roomid) {
        String sql = "DELETE FROM ROOMS WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, roomid);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // As requested, we don't suppress, but return false on FK violation/error
            e.printStackTrace();
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

