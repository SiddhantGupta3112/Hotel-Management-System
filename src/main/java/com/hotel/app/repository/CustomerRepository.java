package com.hotel.app.repository;

import com.hotel.app.entity.Customer;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    /**
     * Find a customer by their user_id.
     * Used after login to load the customer profile.
     */
    public Optional<Customer> findByUserId(long userId) {
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, " +
                "c.nationality, c.loyalty_points, " +
                "u.name, u.email, u.phone_country_code, u.phone_number " +
                "FROM CUSTOMERS c " +
                "JOIN USERS u ON c.user_id = u.user_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.findByUserId: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Find all customers — used in manager dashboard customer list.
     */
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.user_id, c.address, c.id_proof, " +
                "c.nationality, c.loyalty_points, " +
                "u.name, u.email, u.phone_country_code, u.phone_number " +
                "FROM CUSTOMERS c " +
                "JOIN USERS u ON c.user_id = u.user_id " +
                "ORDER BY u.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("CustomerRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Search customers by name or email — manager dashboard search.
     */
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
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("CustomerRepository.search: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a CUSTOMERS row for an existing user.
     * Called by AuthService.register() after the USERS row is created.
     * address, idProof, nationality can be blank at signup.
     */
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

    /**
     * Updates customer profile details.
     * Called from My Profile screen.
     */
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

    /**
     * Add loyalty points after a completed booking.
     */
    public boolean addLoyaltyPoints(long customerId, int points) {
        String sql = "UPDATE CUSTOMERS SET loyalty_points = loyalty_points + ? " +
                "WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, points);
            stmt.setLong(2, customerId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CustomerRepository.addLoyaltyPoints: " + e.getMessage());
            return false;
        }
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

    // ── Private helpers ───────────────────────────────────────────────

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getLong("customer_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setAddress(rs.getString("address"));
        c.setIdProof(rs.getString("id_proof"));
        c.setNationality(rs.getString("nationality"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhoneCountryCode(rs.getString("phone_country_code"));
        c.setPhoneNumber(rs.getString("phone_number"));
        return c;
    }
}