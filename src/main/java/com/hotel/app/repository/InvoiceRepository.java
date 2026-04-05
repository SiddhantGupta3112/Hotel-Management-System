package com.hotel.app.repository;

import com.hotel.app.entity.Invoice;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    private final String BASE_SELECT =
            "SELECT i.invoice_id, i.booking_id, i.total_amount, i.tax, i.generated_date, " +
                    "u.name AS customer_name, r.room_number, b.check_in_date, b.check_out_date, b.booking_status " +
                    "FROM INVOICES i " +
                    "JOIN BOOKINGS b ON i.booking_id = b.booking_id " +
                    "JOIN CUSTOMERS c ON b.customer_id = c.customer_id " +
                    "JOIN USERS u ON c.user_id = u.user_id " +
                    "JOIN ROOMS r ON b.room_id = r.room_id ";

    public List<Invoice> findAll() {
        return executeQuery(BASE_SELECT + "ORDER BY i.invoice_id DESC");
    }

    public List<Invoice> findByCustomerId(long customerId) {
        return executeQuery(BASE_SELECT + "WHERE c.customer_id = ? ORDER BY i.invoice_id DESC", customerId);
    }

    public boolean invoiceExists(long bookingId) {
        String sql = "SELECT 1 FROM INVOICES WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean generate(long bookingId) {
        if (invoiceExists(bookingId)) {
            return false;
        }

        String calcSql =
                "SELECT (b.check_out_date - b.check_in_date) * rt.price_per_night + " +
                        "COALESCE((SELECT SUM(sc.amount_charged) FROM SERVICE_CHARGES sc WHERE sc.booking_id = b.booking_id), 0) AS subtotal " +
                        "FROM BOOKINGS b " +
                        "JOIN ROOMS r ON b.room_id = r.room_id " +
                        "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id " +
                        "WHERE b.booking_id = ?";

        String insertSql = "INSERT INTO INVOICES (booking_id, total_amount, tax, generated_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            double subtotal = 0;

            try (PreparedStatement calcStmt = conn.prepareStatement(calcSql)) {
                calcStmt.setLong(1, bookingId);
                try (ResultSet rs = calcStmt.executeQuery()) {
                    if (rs.next()) {
                        subtotal = rs.getDouble("subtotal");
                    } else {
                        return false; // Booking not found
                    }
                }
            }

            double tax = subtotal * 0.18;

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setLong(1, bookingId);
                insertStmt.setDouble(2, subtotal); // totalAmount is stored as subtotal here
                insertStmt.setDouble(3, tax);
                insertStmt.setDate(4, Date.valueOf(LocalDate.now()));
                return insertStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Invoice> executeQuery(String sql, Object... params) {
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = new Invoice();
                    inv.setInvoiceId(rs.getLong("invoice_id"));
                    inv.setBookingId(rs.getLong("booking_id"));
                    inv.setTotalAmount(rs.getDouble("total_amount"));
                    inv.setTax(rs.getDouble("tax"));
                    inv.setGeneratedDate(rs.getDate("generated_date").toLocalDate());
                    inv.setCustomerName(rs.getString("customer_name"));
                    inv.setRoomNumber(rs.getInt("room_number"));
                    inv.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                    inv.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                    inv.setBookingStatus(rs.getString("booking_status"));
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}