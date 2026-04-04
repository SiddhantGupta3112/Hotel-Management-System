package com.hotel.app.repository;

import com.hotel.app.entity.Staff;
import com.hotel.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffRepository {

    /**
     * Create: Hire a new staff member.
     */
    public boolean save(long userId, long deptId, long managerId, String jobDesc, double salary) {
        String sql = """
            INSERT INTO STAFF (user_id, department_id, manager_id, job_description, salary) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, deptId);
            stmt.setLong(3, managerId);
            stmt.setString(4, jobDesc);
            stmt.setDouble(5, salary);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            handleSQLException("Error saving new staff", e);
            return false;
        }
    }

    /**
     * Read: Get all staff (Admin scope).
     */
    public List<Staff> findAll() {
        List<Staff> staffList = new ArrayList<>();

        String sql = """
            SELECT s.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM STAFF s 
            JOIN USERS u ON s.user_id = u.user_id
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                staffList.add(mapRow(rs));
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching all staff", e);
        }

        return staffList;
    }

    /**
     * Read: Get staff by department (HOD scope).
     */
    public List<Staff> findByDepartment(long deptId) {
        List<Staff> staffList = new ArrayList<>();

        String sql = """
            SELECT s.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM STAFF s 
            JOIN USERS u ON s.user_id = u.user_id 
            WHERE s.department_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    staffList.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching staff by department", e);
        }

        return staffList;
    }

    /**
     * Read: Get staff under a specific manager (Manager scope).
     */
    public List<Staff> findByManagerId(long managerId) {
        List<Staff> staffList = new ArrayList<>();

        String sql = """
            SELECT s.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM STAFF s 
            JOIN USERS u ON s.user_id = u.user_id 
            WHERE s.manager_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, managerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    staffList.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching staff for manager", e);
        }

        return staffList;
    }

    /**
     * Read: Get single staff by staff ID.
     */
    public Optional<Staff> findById(long staffId) {
        String sql = """
            SELECT s.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM STAFF s 
            JOIN USERS u ON s.user_id = u.user_id 
            WHERE s.staff_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, staffId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching staff by ID", e);
        }

        return Optional.empty();
    }

    public Optional<Staff> findByUserId(long userId) {
        String sql = """
        SELECT s.*, u.name, u.email, u.phone_country_code, u.phone_number 
        FROM STAFF s 
        JOIN USERS u ON s.user_id = u.user_id 
        WHERE s.user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching staff by user ID", e);
        }

        return Optional.empty();
    }

    /**
     * Update: Modify staff details.
     */
    public boolean updateStaffDetails(long staffId, long deptId, long managerId, String jobDesc, double salary) {
        String sql = """
            UPDATE STAFF 
            SET department_id = ?, manager_id = ?, job_description = ?, salary = ? 
            WHERE staff_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);
            stmt.setLong(2, managerId);
            stmt.setString(3, jobDesc);
            stmt.setDouble(4, salary);
            stmt.setLong(5, staffId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            handleSQLException("Error updating staff details", e);
            return false;
        }
    }

    /**
     * Delete: Remove staff (Admin use).
     */
    public boolean delete(long staffId) {
        String sql = "DELETE FROM STAFF WHERE staff_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, staffId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            handleSQLException("Error deleting staff", e);
            return false;
        }
    }

    // ───────── MAPPER ─────────
    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();

        s.setStaffId(rs.getLong("staff_id"));
        s.setUserId(rs.getLong("user_id"));
        s.setDepartmentId(rs.getLong("department_id"));
        s.setManagerId(rs.getLong("manager_id"));
        s.setJobDescription(rs.getString("job_description"));
        s.setSalary(rs.getDouble("salary"));

        // User fields
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhoneCountryCode(rs.getString("phone_country_code"));
        s.setPhoneNumber(rs.getString("phone_number"));

        return s;
    }

    private void handleSQLException(String message, SQLException e) {
        System.err.println("StaffRepository: " + message + " -> " + e.getMessage());
    }
}