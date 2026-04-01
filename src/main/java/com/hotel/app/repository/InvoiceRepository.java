package com.hotel.app.repository;

import com.hotel.app.entity.Invoice;
import com.hotel.app.entity.Payment;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.invoice_id, i.booking_id, i.total_amount, i.tax, i.generated_date, " +
                     "       u.name AS customer_name, r.room_number, " +
                     "       b.check_in_date, b.check_out_date, b.booking_status " +
                     "FROM INVOICES i " +
                     "JOIN BOOKINGS b    ON i.booking_id   = b.booking_id " +
                     "JOIN CUSTOMERS c   ON b.customer_id  = c.customer_id " +
                     "JOIN USERS u       ON c.user_id      = u.user_id " +
                     "JOIN ROOMS r       ON b.room_id      = r.room_id " +
                     "ORDER BY i.invoice_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("InvoiceRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Generates an invoice for a booking (room charges + service usage).
     * Tax is fixed at 18% GST.
     */
    public boolean generate(long bookingId) {
        String calcSql = "SELECT " +
                         "  (TRUNC(b.check_out_date) - TRUNC(b.check_in_date)) * rt.price_per_night " +
                         "  + NVL((SELECT SUM(su.total_price) FROM SERVICE_USAGE su " +
                         "         WHERE su.booking_id = b.booking_id), 0) AS subtotal " +
                         "FROM BOOKINGS b " +
                         "JOIN ROOMS r       ON b.room_id      = r.room_id " +
                         "JOIN ROOM_TYPES rt ON r.room_type_id = rt.room_type_id " +
                         "WHERE b.booking_id = ?";
        String insertSql = "INSERT INTO INVOICES (booking_id, total_amount, tax) " +
                           "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            double subtotal;
            try (PreparedStatement s1 = conn.prepareStatement(calcSql)) {
                s1.setLong(1, bookingId);
                try (ResultSet rs = s1.executeQuery()) {
                    if (!rs.next()) return false;
                    subtotal = rs.getDouble("subtotal");
                }
            }
            double tax = subtotal * 0.18;
            try (PreparedStatement s2 = conn.prepareStatement(insertSql)) {
                s2.setLong(1, bookingId);
                s2.setDouble(2, subtotal);
                s2.setDouble(3, tax);
                return s2.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("InvoiceRepository.generate: " + e.getMessage());
            return false;
        }
    }

    public List<Payment> findPaymentsByBooking(long bookingId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.booking_id, p.method_id, p.amount, " +
                     "       p.payment_date, p.status, pm.method_name " +
                     "FROM PAYMENTS p " +
                     "JOIN PAYMENT_METHODS pm ON p.method_id = pm.method_id " +
                     "WHERE p.booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getLong("payment_id"));
                    p.setBookingId(rs.getLong("booking_id"));
                    p.setMethodId(rs.getLong("method_id"));
                    p.setMethodName(rs.getString("method_name"));
                    p.setAmount(rs.getDouble("amount"));
                    p.setStatus(rs.getString("status"));
                    Date pd = rs.getDate("payment_date");
                    if (pd != null) p.setPaymentDate(pd.toLocalDate());
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("InvoiceRepository.findPayments: " + e.getMessage());
        }
        return list;
    }

    public boolean recordPayment(long bookingId, long methodId, double amount) {
        String sql = "INSERT INTO PAYMENTS (booking_id, method_id, amount, status) " +
                     "VALUES (?, ?, ?, 'COMPLETED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            stmt.setLong(2, methodId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InvoiceRepository.recordPayment: " + e.getMessage());
            return false;
        }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setInvoiceId(rs.getLong("invoice_id"));
        i.setBookingId(rs.getLong("booking_id"));
        i.setTotalAmount(rs.getDouble("total_amount"));
        i.setTax(rs.getDouble("tax"));
        i.setCustomerName(rs.getString("customer_name"));
        i.setRoomNumber(rs.getInt("room_number"));
        i.setBookingStatus(rs.getString("booking_status"));
        Date gd = rs.getDate("generated_date");
        Date ci = rs.getDate("check_in_date");
        Date co = rs.getDate("check_out_date");
        if (gd != null) i.setGeneratedDate(gd.toLocalDate());
        if (ci != null) i.setCheckInDate(ci.toLocalDate());
        if (co != null) i.setCheckOutDate(co.toLocalDate());
        return i;
    }
}
