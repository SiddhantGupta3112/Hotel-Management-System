package com.hotel.app.repository;

import com.hotel.app.entity.Customer;
import com.hotel.app.util.DBConnection;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {


    public Optional<Customer> findByUserId(long userId) {
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, c.nationality, " +
                "c.loyalty_points, u.name, u.email, u.phone_country_code, u.phone_number " +
                "FROM CUSTOMERS c JOIN USERS u ON c.user_id = u.user_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, c.nationality, " +
                "c.loyalty_points, u.name, u.email, u.phone_country_code, u.phone_number " +
                "FROM CUSTOMERS c JOIN USERS u ON c.user_id = u.user_id " +
                "ORDER BY u.name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public boolean createProfile(long userId) {
        String sql = "INSERT INTO CUSTOMERS (user_id, address, id_proof, nationality, loyalty_points) " +
                "VALUES (?, '', '', '', 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CustomerRepository.createProfile: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfile(long customerId, String address,
                                 String idProof, String nationality) {
        String sql = "UPDATE CUSTOMERS SET address = ?, id_proof = ?, nationality = ? " +
                "WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, address);
            stmt.setString(2, idProof);
            stmt.setString(3, nationality);
            stmt.setLong(4, customerId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CustomerRepository.updateProfile: " + e.getMessage());
            return false;
        }
    }

    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, " +
                "c.nationality, c.loyalty_points, " +
                "u.name, u.email, u.phone_country_code, u.phone_number " +
                "FROM CUSTOMERS c " +
                "JOIN USERS u ON c.user_id = u.user_id " +
                "WHERE LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? " +
                "ORDER BY u.name";

        String kw = "%" + keyword.toLowerCase() + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kw);
            stmt.setString(2, kw);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.search: " + e.getMessage());
        }
        return list;
    }

    public boolean updateProfileByUserId(long userId, String address,
                                         String idProof, String nationality) {
        String sql = "UPDATE CUSTOMERS SET address = ?, id_proof = ?, nationality = ? " +
                "WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, address);
            stmt.setString(2, idProof);
            stmt.setString(3, nationality);
            stmt.setLong(4, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CustomerRepository.updateProfileByUserId: " + e.getMessage());
            return false;
        }
    }




    public long save(String name, String email, String phone, String address, String idProof, String nationality) {
        String userSql = "INSERT INTO USERS (email, password_hash, name, phone_number, is_active) " +
                "VALUES (?, 'GUEST_NO_LOGIN', ?, ?, 1)";
        String customerSql = "INSERT INTO CUSTOMERS (user_id, address, id_proof, nationality, loyalty_points) VALUES (?, ?, ?, ?, 0)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Step 1: Insert into USERS
            long generatedUserId = -1;
            try (PreparedStatement uStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                uStmt.setString(1, name);
                uStmt.setString(2, email);
                uStmt.setString(3, phone);
                uStmt.executeUpdate();

                ResultSet rs = uStmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedUserId = rs.getLong(1);
                }
            }

            // Step 2: Insert into CUSTOMERS
            long generatedCustomerId = -1;
            try (PreparedStatement cStmt = conn.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS)) {
                cStmt.setLong(1, generatedUserId);
                cStmt.setString(2, address);
                cStmt.setString(3, idProof);
                cStmt.setString(4, nationality);
                cStmt.executeUpdate();

                ResultSet rs = cStmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedCustomerId = rs.getLong(1);
                }
            }

            conn.commit(); // End Transaction
            return generatedCustomerId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean createFromSignup(long userId) {
        String sql = "INSERT INTO CUSTOMERS (user_id, loyalty_points) VALUES (?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addLoyaltyPoints(long customerId, int points) {
        String sql = "UPDATE CUSTOMERS SET loyalty_points = loyalty_points + ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, points);
            pstmt.setLong(2, customerId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setIdProof(rs.getString("id_proof"));
        c.setNationality(rs.getString("nationality"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setPhoneCountryCode(rs.getString("phone_country_code"));
        c.setPhoneNumber(rs.getString("phone_number"));
        return c;
    }
}