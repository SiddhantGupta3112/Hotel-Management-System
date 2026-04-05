package com.hotel.app.repository;

import com.hotel.app.entity.Booking;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookingRepository {

    private final String BASE_SELECT =
            "SELECT b.booking_id, b.customer_id, b.room_id, b.booking_date, b.check_in_date, " +
                    "b.check_out_date, b.booking_status, u.name as customer_name, r.room_number, " +
                    "rt.type_name, rt.price_per_night " +
                    "FROM BOOKINGS b " +
                    "JOIN CUSTOMERS c ON b.customer_id = c.customer_id " +
                    "JOIN USERS u ON c.user_id = u.user_id " +
                    "JOIN ROOMS r ON b.room_id = r.room_id " +
                    "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id ";


    public List<Booking> findAll() {
        return executeQuery(BASE_SELECT + "ORDER BY b.booking_id DESC");
    }

    public List<Booking> findByCustomerId(long customerId) {
        return executeQuery(BASE_SELECT + "WHERE b.customer_id = ? ORDER BY b.booking_id DESC", customerId);
    }

    public Optional<Booking> findById(long bookingId) {
        List<Booking> results = executeQuery(BASE_SELECT + "WHERE b.booking_id = ?", bookingId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Booking> findByStatus(String status) {
        return executeQuery(BASE_SELECT + "WHERE b.booking_status = ? ORDER BY b.booking_id DESC", status);
    }

    public List<Booking> findByStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return new ArrayList<>();
        String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
        String sql = BASE_SELECT + "WHERE b.booking_status IN (" + placeholders + ") ORDER BY b.booking_id DESC";
        return executeQuery(sql, statuses.toArray());
    }

    public Optional<Booking> findActiveByCustomerId(long customerId) {
        String sql = BASE_SELECT + "WHERE b.customer_id = ? AND b.booking_status IN " +
                "('APPROVED','CHECKIN_PENDING','CHECKED_IN','CHECKOUT_PENDING')";
        List<Booking> results = executeQuery(sql, customerId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }


    public long save(long customerId, long roomId, LocalDate checkIn, LocalDate checkOut) {
        String sql = "INSERT INTO BOOKINGS (customer_id, room_id, booking_date, check_in_date, check_out_date, booking_status) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"BOOKING_ID"})) {

            pstmt.setLong(1, customerId);
            pstmt.setLong(2, roomId);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.setDate(4, Date.valueOf(checkIn));
            pstmt.setDate(5, Date.valueOf(checkOut));

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getLong(1) : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateStatus(long bookingId, String newStatus) {
        String sql = "UPDATE BOOKINGS SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setLong(2, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatusAndRoomStatus(long bookingId, String bStatus, long roomId, String rStatus) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Update Booking
            try (PreparedStatement bPstmt = conn.prepareStatement("UPDATE BOOKINGS SET booking_status = ? WHERE booking_id = ?")) {
                bPstmt.setString(1, bStatus);
                bPstmt.setLong(2, bookingId);
                bPstmt.executeUpdate();
            }

            // Update Room
            try (PreparedStatement rPstmt = conn.prepareStatement("UPDATE ROOMS SET status = ? WHERE room_id = ?")) {
                rPstmt.setString(1, rStatus);
                rPstmt.setLong(2, roomId);
                rPstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean hasOverlappingBooking(long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        String sql = "SELECT 1 FROM BOOKINGS WHERE room_id = ? " +
                "AND booking_status IN ('APPROVED','CHECKIN_PENDING','CHECKED_IN','CHECKOUT_PENDING') " +
                "AND check_in_date < ? AND check_out_date > ? " +
                "AND booking_id != COALESCE(?, -1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkOut)); // checkIn < existingCheckOut
            pstmt.setDate(3, Date.valueOf(checkIn));  // checkOut > existingCheckIn
            if (excludeBookingId == null) pstmt.setNull(4, Types.BIGINT);
            else pstmt.setLong(4, excludeBookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume overlap on error to be safe
        }
    }


    private List<Booking> executeQuery(String sql, Object... params) {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Date) pstmt.setDate(i + 1, (Date) params[i]);
                else pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getLong("booking_id"));
                    b.setCustomerId(rs.getLong("customer_id"));
                    b.setRoomId(rs.getLong("room_id"));
                    b.setCustomerName(rs.getString("customer_name"));
                    b.setRoomNumber(rs.getInt("room_number"));
                    b.setRoomTypeName(rs.getString("type_name"));
                    b.setPricePerNight(rs.getDouble("price_per_night"));
                    b.setBookingDate(rs.getDate("booking_date").toLocalDate());
                    b.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                    b.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                    b.setBookingStatus(rs.getString("booking_status"));
                    list.add(b);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}