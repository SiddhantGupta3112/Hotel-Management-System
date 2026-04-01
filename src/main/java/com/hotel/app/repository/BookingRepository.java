package com.hotel.app.repository;

import com.hotel.app.entity.Booking;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.customer_id, b.room_id, " +
                     "       b.booking_date, b.check_in_date, b.check_out_date, b.booking_status, " +
                     "       u.name AS customer_name, r.room_number, rt.type_name " +
                     "FROM BOOKINGS b " +
                     "JOIN CUSTOMERS c   ON b.customer_id  = c.customer_id " +
                     "JOIN USERS u       ON c.user_id      = u.user_id " +
                     "JOIN ROOMS r       ON b.room_id      = r.room_id " +
                     "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id " +
                     "ORDER BY b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BookingRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    public boolean save(long customerId, long roomId,
                        LocalDate checkIn, LocalDate checkOut) {
        String sql = "INSERT INTO BOOKINGS " +
                     "(customer_id, room_id, check_in_date, check_out_date, booking_status) " +
                     "VALUES (?, ?, ?, ?, 'CONFIRMED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, customerId);
            stmt.setLong(2, roomId);
            stmt.setDate(3, Date.valueOf(checkIn));
            stmt.setDate(4, Date.valueOf(checkOut));
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) updateRoomStatus(conn, roomId, "RESERVED");
            return ok;
        } catch (SQLException e) {
            System.err.println("BookingRepository.save: " + e.getMessage());
            return false;
        }
    }

    public boolean checkIn(long bookingId, long roomId) {
        return updateStatus(bookingId, "CHECKED_IN") &&
               updateRoomStatusNewConn(roomId, "OCCUPIED");
    }

    public boolean checkOut(long bookingId, long roomId) {
        return updateStatus(bookingId, "CHECKED_OUT") &&
               updateRoomStatusNewConn(roomId, "AVAILABLE");
    }

    public boolean cancel(long bookingId, long roomId) {
        return updateStatus(bookingId, "CANCELLED") &&
               updateRoomStatusNewConn(roomId, "AVAILABLE");
    }

    // ── Helpers ──────────────────────────────────────────────

    private boolean updateStatus(long bookingId, String status) {
        String sql = "UPDATE BOOKINGS SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingRepository.updateStatus: " + e.getMessage());
            return false;
        }
    }

    private void updateRoomStatus(Connection conn, long roomId, String status)
            throws SQLException {
        String sql = "UPDATE ROOMS SET status = ? WHERE room_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, roomId);
            stmt.executeUpdate();
        }
    }

    private boolean updateRoomStatusNewConn(long roomId, String status) {
        String sql = "UPDATE ROOMS SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BookingRepository.updateRoomStatus: " + e.getMessage());
            return false;
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getLong("booking_id"));
        b.setCustomerId(rs.getLong("customer_id"));
        b.setRoomId(rs.getLong("room_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setRoomNumber(rs.getInt("room_number"));
        b.setRoomTypeName(rs.getString("type_name"));
        b.setBookingStatus(rs.getString("booking_status"));
        Date ci = rs.getDate("check_in_date");
        Date co = rs.getDate("check_out_date");
        Date bd = rs.getDate("booking_date");
        if (ci != null) b.setCheckInDate(ci.toLocalDate());
        if (co != null) b.setCheckOutDate(co.toLocalDate());
        if (bd != null) b.setBookingDate(bd.toLocalDate());
        return b;
    }
}
