package com.hotel.app.repository;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.Service;
import com.hotel.app.entity.ServiceCharge;
import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ServiceRepository{

    public List<Service> findAllServices() {
        List<Service> list=new ArrayList<>();
        String sql="SELECT * FROM SERVICES WHERE is_available = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setServiceId(rs.getInt("service_id"));
                s.setName(rs.getString("name"));
                s.setCategory(rs.getString("category"));
                s.setDesc(rs.getString("description"));
                s.setBase_Price(rs.getDouble("base_price"));
                s.setIs_Available(rs.getInt("is_available"));

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
        }
        return list;
    }

    public boolean saveRequest(long bookingId, long serviceId, long quantity, String notes) {
        String sql =
                "INSERT INTO SERVICE_REQUESTS (booking_id, service_id, quantity, notes, status) " +
                        "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            stmt.setLong(2, serviceId);
            stmt.setLong(3, quantity);
            stmt.setString(4, notes);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ServiceRepository.saveRequest: " + e.getMessage());
            return false;
        }
    }

    public List<ServiceRequest> findPendingRequests() {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT sr.*, s.name, r.room_number " +
                "FROM SERVICE_REQUESTS sr " +
                "JOIN ROOMS r ON b.room_id=r.room_id" +
                "JOIN SERVICES s ON sr.service_id = s.service_id " +
                "JOIN BOOKINGS b ON sr.booking_id = b.booking_id " +
                "WHERE sr.status IN ('PENDING', 'IN_PROGRESS')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServiceRequest req = new ServiceRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setBookingId(rs.getInt("booking_id"));
                req.setServiceName(rs.getString("name"));
                req.setQuantity(rs.getInt("quantity"));
                req.setNotes(rs.getString("notes"));
                // Assuming your enum is called RequestStatus
                // RIGHT: This just sets the String value directly
                req.setStatus(rs.getString("status"));
                // Set room number here if you add the field to your entity
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markServed(long requestId, long staffUserId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Fetch data for the charge calculation
            int bookingId = 0;
            double totalPrice = 0;
            String fetchSql = "SELECT sr.booking_id, sr.quantity, s.base_price FROM SERVICE_REQUESTS sr " +
                    "JOIN SERVICES s ON sr.service_id = s.service_id WHERE sr.request_id = ?";

            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                fetchStmt.setLong(1, requestId);
                ResultSet rs = fetchStmt.executeQuery();
                if (rs.next()) {
                    bookingId = rs.getInt("booking_id");
                    totalPrice = rs.getInt("quantity") * rs.getDouble("base_price");
                } else {
                    throw new SQLException("Request ID not found.");
                }
            }

            // 2. Update Request Status
            String updateSql = "UPDATE SERVICE_REQUESTS SET status='SERVED', served_at=CURRENT_TIMESTAMP, served_by=? WHERE request_id=?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setLong(1, staffUserId);
                updateStmt.setLong(2, requestId);
                updateStmt.executeUpdate();
            }

            // 3. Insert into SERVICE_CHARGES
            String chargeSql = "INSERT INTO SERVICE_CHARGES (request_id, booking_id, amount_charged) VALUES (?, ?, ?)";
            try (PreparedStatement chargeStmt = conn.prepareStatement(chargeSql)) {
                chargeStmt.setLong(1, requestId);
                chargeStmt.setLong(2, bookingId);
                chargeStmt.setDouble(3, totalPrice);
                chargeStmt.executeUpdate();
            }

            conn.commit(); // Success: Commit both statements
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean markCancelled(Long requestId) {
        String sql = "UPDATE SERVICE_REQUESTS SET status='CANCELLED' WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ServiceCharge> findChargesForBooking(Long bookingId) {
        List<ServiceCharge> charges = new ArrayList<>();
        String sql = "SELECT * FROM SERVICE_CHARGES WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ServiceCharge sc = new ServiceCharge();
                sc.setChargeId(rs.getInt("charge_id"));
                sc.setRequestId(rs.getInt("request_id"));
                sc.setBookingId(rs.getInt("booking_id"));
                sc.setPrice(rs.getDouble("amount_charged"));
                // Set timestamp if you have it
                charges.add(sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return charges;
    }

    public Optional<Booking> findActiveBookingForUser(long userId) {
        String sql =
                "SELECT b.booking_id, b.customer_id, b.room_id, " +
                        "       b.check_in_date, b.check_out_date, b.booking_status, " +
                        "       u.name AS customer_name, r.room_number " +
                        "FROM BOOKINGS b " +
                        "JOIN CUSTOMERS c ON b.customer_id = c.customer_id " +
                        "JOIN USERS     u ON c.user_id     = u.user_id " +
                        "JOIN ROOMS     r ON b.room_id     = r.room_id " +
                        "WHERE c.user_id = ? " +
                        "  AND b.booking_status IN ('APPROVED','CHECKIN_PENDING','CHECKED_IN','CHECKOUT_PENDING') " +
                        "ORDER BY b.booking_id DESC " +
                        "FETCH FIRST 1 ROW ONLY";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getLong("booking_id"));
                    b.setCustomerId(rs.getLong("customer_id"));
                    b.setRoomId(rs.getLong("room_id"));
                    b.setCustomerName(rs.getString("customer_name"));
                    b.setRoomNumber(rs.getInt("room_number"));
                    b.setBookingStatus(rs.getString("booking_status"));
                    Date ci = rs.getDate("check_in_date");
                    Date co = rs.getDate("check_out_date");
                    if (ci != null) b.setCheckInDate(ci.toLocalDate());
                    if (co != null) b.setCheckOutDate(co.toLocalDate());
                    return Optional.of(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("ServiceRepository.findActiveBookingForUser: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<ServiceRequest> findRequestsByBooking(long bookingId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT sr.*, s.name as service_name, s.base_price " +
                "FROM SERVICE_REQUESTS sr " +
                "JOIN SERVICES s ON sr.service_id = s.service_id " +
                "WHERE sr.booking_id = ? ORDER BY sr.requested_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest su = mapResultSetToServiceRequest(rs);
                    list.add(su);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ServiceRequest> findActiveRequests() {
        List<ServiceRequest> list = new ArrayList<>();
        String sql =
                "SELECT sr.request_id, sr.booking_id, sr.service_id, sr.quantity, " +
                        "       sr.notes, sr.status, sr.requested_at, " +
                        "       s.name AS service_name, s.base_price, " +
                        "       u.name AS guest_name, r.room_number " +
                        "FROM SERVICE_REQUESTS sr " +
                        "JOIN SERVICES  s  ON sr.service_id  = s.service_id " +
                        "JOIN BOOKINGS  b  ON sr.booking_id  = b.booking_id " +
                        "JOIN CUSTOMERS c  ON b.customer_id  = c.customer_id " +
                        "JOIN USERS     u  ON c.user_id      = u.user_id " +
                        "JOIN ROOMS     r  ON b.room_id      = r.room_id " +
                        "WHERE sr.status IN ('PENDING', 'IN_PROGRESS') " +
                        "ORDER BY sr.requested_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServiceRequest req = mapResultSetToServiceRequest(rs);
                req.setGuestName(rs.getString("guest_name"));
                req.setRoomNumber(rs.getInt("room_number"));
                list.add(req);
            }
        } catch (SQLException e) {
            System.err.println("ServiceRepository.findActiveRequests: " + e.getMessage());
        }
        return list;
    }

    public boolean markInProgress(long requestId, long staffId) {
        String sql = "UPDATE SERVICE_REQUESTS SET status = 'IN_PROGRESS', " +
                "served_by = ? WHERE request_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, staffId);
            stmt.setLong(2, requestId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ServiceRequest mapResultSetToServiceRequest(ResultSet rs) throws SQLException {
        ServiceRequest req = new ServiceRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setBookingId(rs.getInt("booking_id"));
        req.setServiceId(rs.getInt("service_id"));
        req.setServiceName(rs.getString("service_name"));
        req.setServicePrice(rs.getDouble("base_price"));
        req.setQuantity(rs.getInt("quantity"));
        req.setNotes(rs.getString("notes"));
        req.setStatus(rs.getString("status"));
        Timestamp rat = rs.getTimestamp("requested_at");
        if (rat != null) req.setRequestedAt(rat.toLocalDateTime());
        return req;
    }

}