package com.hotel.app.repository;

import com.hotel.app.entity.User;
import com.hotel.app.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByEmail(String email) {
        // 1. Updated SQL to include phone_country_code and phone_number
        String sql =  "SELECT user_id, email, name, password_hash, is_active, phone_country_code, phone_number, created_at " +
                "FROM USERS WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setActive(rs.getInt("is_active") == 1);
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        user.setCreatedAt(ts.toLocalDateTime());
                    }

                    // 2. Map the new database columns to the User entity fields
                    user.setPhoneCountryCode(rs.getString("phone_country_code"));
                    user.setPhoneNumber(rs.getString("phone_number"));

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    public List<String> findRolesByUserId(long userId) {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT r.role_name FROM ROLES r " +
                "JOIN USER_ROLES ur ON r.role_id = ur.role_id " +
                "WHERE ur.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role_name"));
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error finding roles for user", e);
        }
        return roles;
    }

    

    public long save(String email, String passwordHash, String name,
                     String countryCode, String phoneNumber, boolean isActive) {

        String sql = "{call save_user(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setString(3, name);
            stmt.setString(4, countryCode);
            stmt.setString(5, phoneNumber);
            stmt.setInt(6, isActive ? 1 : 0);
            stmt.registerOutParameter(7, Types.NUMERIC);

            stmt.execute();
            return stmt.getLong(7);

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                System.err.println("Registration failed: Email already exists.");
            } else {
                handleSQLException("Error saving user", e);
            }
            return -1;
        }
    }

    public boolean updateUser(long userId, String name, String email,
                              String phoneCountryCode, String phoneNumber) {
        // Check if new email is already taken by a different user
        String checkSql = "SELECT COUNT(*) FROM USERS WHERE LOWER(email) = LOWER(?) AND user_id != ?";

        try (Connection conn = DBConnection.getConnection()) {

            // Step 1 — verify email not taken by someone else
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email.trim());
                checkStmt.setLong(2, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("UserRepository.updateProfile: Email already in use");
                        return false;
                    }
                }
            }

            // Step 2 — update the user
            String updateSql = "UPDATE USERS " +
                    "SET name = ?, email = LOWER(?), " +
                    "phone_country_code = ?, phone_number = ? " +
                    "WHERE user_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, name.trim());
                updateStmt.setString(2, email.trim());
                updateStmt.setString(3, phoneCountryCode.trim());
                updateStmt.setString(4, phoneNumber.trim());
                updateStmt.setLong(5, userId);
                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("UserRepository.updateProfile: " + e.getMessage());
            return false;
        }
    }


    public boolean updatePassword(long userId, String password_hash){
        String sql = "UPDATE  USERS " +
                "SET password_hash = ? " +
                "WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password_hash);
            stmt.setLong(2, userId);


            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating role", e);
            return false;
        }
    }
    public boolean deleteUserAccount(long userId) {
        // Match the procedure name exactly: delete_user
        String sql = "{call delete_user(?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setLong(1, userId);
            stmt.execute();
            return true; // If no exception was thrown, it worked

        } catch (SQLException e) {
            handleSQLException("Error executing delete_user procedure", e);
            return false;
        }
    }

    

    public boolean updateRole(long userId, String oldRole, String newRole) {
        String sql = "UPDATE USER_ROLES " +
                "SET role_id = (SELECT role_id FROM ROLES WHERE role_name = ?) " +
                "WHERE user_id = ? " +
                "AND role_id = (SELECT role_id FROM ROLES WHERE role_name = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setLong(2, userId);
            stmt.setString(3, oldRole);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating role", e);
            return false;
        }
    }
    
    

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM USERS WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleSQLException("Error checking email existence", e);
            return false;
        }
    }

    public boolean assignRole(long userId, String role) {
        String sql = "INSERT INTO USER_ROLES (user_id, role_id) " +
                "VALUES (?, (SELECT role_id FROM ROLES WHERE role_name = ?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, role);
            int assign_role = stmt.executeUpdate();

            if(assign_role > 0){
                return true;
            }

        } catch (SQLException e) {
            handleSQLException("Error assigning role", e);
        }
        return false;
    }

    

    private void handleSQLException(String message, SQLException e) {
        System.err.println(message + ": " + e.getMessage());
    }
}