package com.hotel.app.repository;

import com.hotel.app.entity.Booking;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Single repository for all dashboard summary queries.
 * Each method opens its own connection (same pattern as UserRepository).
 */
public class DashboardRepository {

    // ------------------------------------------------------------------
    // Stat card queries
    // ------------------------------------------------------------------

    public int getTotalRooms() {
        String sql = "SELECT COUNT(*) FROM ROOMS";
        return queryInt(sql);
    }

    public int getAvailableRooms() {
        String sql = "SELECT COUNT(*) FROM ROOMS WHERE status = 'AVAILABLE'";
        return queryInt(sql);
    }

    public int getOccupiedRooms() {
        String sql = "SELECT COUNT(*) FROM ROOMS WHERE status = 'OCCUPIED'";
        return queryInt(sql);
    }

    public int getTodayCheckIns() {
        String sql = "SELECT COUNT(*) FROM BOOKINGS " +
                     "WHERE TRUNC(check_in_date) = TRUNC(SYSDATE) " +
                     "AND booking_status IN ('CONFIRMED','CHECKED_IN')";
        return queryInt(sql);
    }

    public int getTodayCheckOuts() {
        String sql = "SELECT COUNT(*) FROM BOOKINGS " +
                     "WHERE TRUNC(check_out_date) = TRUNC(SYSDATE) " +
                     "AND booking_status IN ('CHECKED_IN','CHECKED_OUT')";
        return queryInt(sql);
    }

    // ------------------------------------------------------------------
    // Revenue queries
    // ------------------------------------------------------------------

    public double getTodayRevenue() {
        String sql = "SELECT NVL(SUM(amount), 0) FROM PAYMENTS " +
                     "WHERE TRUNC(payment_date) = TRUNC(SYSDATE) " +
                     "AND status = 'COMPLETED'";
        return queryDouble(sql);
    }

    public double getMonthRevenue() {
        String sql = "SELECT NVL(SUM(amount), 0) FROM PAYMENTS " +
                     "WHERE TRUNC(payment_date,'MM') = TRUNC(SYSDATE,'MM') " +
                     "AND status = 'COMPLETED'";
        return queryDouble(sql);
    }

    // ------------------------------------------------------------------
    // Recent bookings (last 8)
    // ------------------------------------------------------------------

    public List<Booking> getRecentBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booking_status, " +
                     "       b.check_in_date, b.check_out_date, " +
                     "       u.name AS customer_name, " +
                     "       r.room_number, rt.type_name " +
                     "FROM BOOKINGS b " +
                     "JOIN CUSTOMERS c  ON b.customer_id = c.customer_id " +
                     "JOIN USERS u      ON c.user_id     = u.user_id " +
                     "JOIN ROOMS r      ON b.room_id     = r.room_id " +
                     "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id " +
                     "ORDER BY b.booking_id DESC " +
                     "FETCH FIRST 8 ROWS ONLY";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking();
                b.setBookingId(rs.getLong("booking_id"));
                b.setBookingStatus(rs.getString("booking_status"));
                b.setCustomerName(rs.getString("customer_name"));
                b.setRoomNumber(rs.getInt("room_number"));
                b.setRoomTypeName(rs.getString("type_name"));

                Date ci = rs.getDate("check_in_date");
                Date co = rs.getDate("check_out_date");
                if (ci != null) b.setCheckInDate(ci.toLocalDate());
                if (co != null) b.setCheckOutDate(co.toLocalDate());

                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("DashboardRepository.getRecentBookings: " + e.getMessage());
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private int queryInt(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("DashboardRepository int query error: " + e.getMessage());
        }
        return 0;
    }

    private double queryDouble(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("DashboardRepository double query error: " + e.getMessage());
        }
        return 0.0;
    }
}
