package com.hotel.app.repository;

import com.hotel.app.entity.Room;
import com.hotel.app.entity.RoomType;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomTypeRepository {
    public List<RoomType> findAll(){
        List<RoomType> types = new ArrayList<>();
        String sql = "Select * from Room_Types order by price_per_night";
        try(Connection conn = DBConnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)){

            while(rs.next()) {
                types.add(mapRows(rs));
            }
        }
        catch (SQLException e){
            e.printStackTrace();;
    }
        return types;
}


    public Optional<RoomType> findById(long roomTypeId){

            String sql = "Select * from Room_Types where room_type_id = ?";
            try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setLong(1,roomTypeId);

                try(ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRows(rs));
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
    }

    public boolean typeNameExists(String typeName){
            String sql = "Select 1 from room_types where lower(type_name) = lower(?)";
            try(Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1,typeName);

                try(ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
    }


    public long save(String typeName, int capacity, double pricePerNight, String description) {
            String sql = "Insert into room_types(type_name, capacity, price_per_night, description) values (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ROOM_TYPE_ID"})) {

                ps.setString(1, typeName);
                ps.setInt(2, capacity);
                ps.setDouble(3, pricePerNight);
                ps.setString(4, description);

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

    public boolean update(long roomTypeId, String typeName, int capacity, double pricePerNight, String description) {
        String sql = "Update room_types set type_name = ?, capacity = ?, price_per_night = ?, description = ? where room_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, typeName);
            ps.setInt(2, capacity);
            ps.setDouble(3, pricePerNight);
            ps.setString(4, description);
            ps.setLong(5, roomTypeId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(long roomTypeId) {
        String sql = "Delete from room_types where room_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, roomTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // This will return false if a foreign key constraint in the ROOMS table prevents deletion
            e.printStackTrace();
            return false;
        }
    }

    private RoomType mapRows(ResultSet rs) throws SQLException {
        return new RoomType(
                rs.getLong("room_type_id"),
                rs.getString("type_name"),
                rs.getInt("capacity"),
                rs.getDouble("price_per_night"),
                rs.getString("description")
        );
    }
}
