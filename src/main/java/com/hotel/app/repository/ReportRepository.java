package com.hotel.app.repository;

import com.hotel.app.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Repository for complex analytical queries.
 * Handles data aggregation for Line, Bar, and Pie charts.
 */
public class ReportRepository {

    /**
     * 1. Monthly Revenue (Line Chart)
     * Groups total invoice amounts by month.
     */
    public Map<String, Double> getMonthlyRevenue(LocalDate start, LocalDate end) {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = """
            SELECT TO_CHAR(Generated_Date, 'YYYY-MM') AS Month, SUM(Total_amount) AS Total
            FROM Invoices
            WHERE Generated_Date >= ? AND Generated_Date <= ?
            GROUP BY TO_CHAR(Generated_Date, 'YYYY-MM')
            ORDER BY Month ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) data.put(rs.getString("Month"), rs.getDouble("Total"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * 2. Monthly Booking Volume (Bar Chart)
     * Counts total reservations made per month.
     */
    public Map<String, Integer> getMonthlyBookingCount(LocalDate start, LocalDate end) {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT TO_CHAR(Booking_Date, 'YYYY-MM') AS Month, COUNT(booking_id) AS Count
            FROM Bookings
            WHERE Booking_Date >= ? AND Booking_Date <= ?
            GROUP BY TO_CHAR(Booking_Date, 'YYYY-MM')
            ORDER BY Month ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) data.put(rs.getString("Month"), rs.getInt("Count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * 3. Booking Status Distribution (Pie Chart)
     * Breakdown of Confirmed, Cancelled, and Checked-out bookings.
     */
    public Map<String, Integer> getBookingStatusDistribution(LocalDate start, LocalDate end) {
        Map<String, Integer> data = new TreeMap<>();
        // Fixed: column name is BOOKING_STATUS
        String sql = """
        SELECT BOOKING_STATUS, COUNT(BOOKING_ID) AS Count
        FROM Bookings
        WHERE BOOKING_DATE >= ? AND BOOKING_DATE <= ?
        GROUP BY BOOKING_STATUS
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("BOOKING_STATUS"), rs.getInt("Count"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * 4. Room Type Distribution (Pie Chart)
     * Shows which room categories (Deluxe, Suite, etc.) make up the inventory.
     */
    public Map<String, Integer> getRoomTypeDistribution() {
        Map<String, Integer> data = new TreeMap<>();
        String sql = """
            SELECT rt.type_name, COUNT(r.room_id) AS Count
            FROM Rooms r
            JOIN Room_Types rt ON r.room_type_id = rt.room_type_id
            GROUP BY rt.type_name
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) data.put(rs.getString("type_name"), rs.getInt("Count"));
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * 5. Popular Services (Bar Chart)
     * Counts how many times each service has been billed.
     */
    public Map<String, Integer> getPopularServices(LocalDate start, LocalDate end) {
        Map<String, Integer> data = new LinkedHashMap<>();

        // Triple Join: sc -> sr -> s
        // Fixed: Using s.NAME as the service identifier
        String sql = """
        SELECT s.NAME AS SERVICE_DISPLAY_NAME, COUNT(sc.CHARGE_ID) AS UsageCount
        FROM SERVICE_CHARGES sc
        JOIN SERVICE_REQUESTS sr ON sc.REQUEST_ID = sr.REQUEST_ID
        JOIN SERVICES s ON sr.SERVICE_ID = s.SERVICE_ID
        WHERE CAST(sc.CHARGED_AT AS DATE) >= ? 
          AND CAST(sc.CHARGED_AT AS DATE) <= ?
        GROUP BY s.NAME
        ORDER BY UsageCount DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString("SERVICE_DISPLAY_NAME"), rs.getInt("UsageCount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getPopularServices: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }
}