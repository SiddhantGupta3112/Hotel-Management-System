package com.hotel.app.repository;

import com.hotel.app.entity.Payment;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {


    public List<Payment> findByBookingId(long bookingId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.booking_id, p.method_id, p.amount, " +
                "p.payment_date, p.status, pm.method_name " +
                "FROM PAYMENTS p " +
                "JOIN PAYMENT_METHODS pm ON p.method_id = pm.method_id " +
                "WHERE p.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getLong("payment_id"));
                    p.setBookingId(rs.getLong("booking_id"));
                    p.setMethodId(rs.getLong("method_id"));
                    p.setMethodName(rs.getString("method_name"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                    p.setStatus(rs.getString("status"));
                    payments.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }


    public boolean save(long bookingId, long methodId, double amount) {
        String sql = "INSERT INTO PAYMENTS (booking_id, method_id, amount, payment_date, status) " +
                "VALUES (?, ?, ?, ?, 'COMPLETED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, bookingId);
            pstmt.setLong(2, methodId);
            pstmt.setDouble(3, amount);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<String[]> findAllPaymentMethods() {
        List<String[]> methods = new ArrayList<>();
        String sql = "SELECT method_id, method_name FROM PAYMENT_METHODS ORDER BY method_name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                methods.add(new String[]{
                        String.valueOf(rs.getLong("method_id")),
                        rs.getString("method_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methods;
    }
}