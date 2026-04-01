package com.hotel.app.repository;

import com.hotel.app.entity.Customer;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, " +
                     "       c.nationality, c.loyalty_points, " +
                     "       u.name, u.email " +
                     "FROM CUSTOMERS c " +
                     "JOIN USERS u ON c.user_id = u.user_id " +
                     "ORDER BY u.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, " +
                     "       c.nationality, c.loyalty_points, u.name, u.email " +
                     "FROM CUSTOMERS c JOIN USERS u ON c.user_id = u.user_id " +
                     "WHERE LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? " +
                     "ORDER BY u.name";
        String kw = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.search: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a USERS row + a CUSTOMERS row in one transaction.
     */
    public boolean save(String name, String email, String phone,
                        String address, String idProof, String nationality) {
        String insertUser = "INSERT INTO USERS (email, password_hash, name, phone_number, is_active) " +
                            "VALUES (?, 'GUEST_NO_LOGIN', ?, ?, 1)";
        String insertCust = "INSERT INTO CUSTOMERS (user_id, address, id_proof, nationality) " +
                            "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            long newUserId;
            try (PreparedStatement s1 = conn.prepareStatement(insertUser,
                         new String[]{"USER_ID"})) {
                s1.setString(1, email);
                s1.setString(2, name);
                s1.setString(3, phone);
                s1.executeUpdate();
                try (ResultSet gk = s1.getGeneratedKeys()) {
                    if (!gk.next()) { conn.rollback(); return false; }
                    newUserId = gk.getLong(1);
                }
            }
            try (PreparedStatement s2 = conn.prepareStatement(insertCust)) {
                s2.setLong(1, newUserId);
                s2.setString(2, address);
                s2.setString(3, idProof);
                s2.setString(4, nationality);
                s2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("CustomerRepository.save: " + e.getMessage());
            return false;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setIdProof(rs.getString("id_proof"));
        c.setNationality(rs.getString("nationality"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        return c;
    }
}
