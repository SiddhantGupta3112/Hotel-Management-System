package com.hotel.app.repository;

import com.hotel.app.entity.Manager;
import com.hotel.app.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManagerRepository {

    /**
     * Admin: Promote a User to a Manager.
     */
    public boolean save(long userId, long deptId, Long reportsToId, String jobDesc, double salary) {
        String sql = "INSERT INTO MANAGERS (USER_ID, DEPARTMENT_ID, REPORTS_TO_MANAGER_ID, JOB_DESCRIPTION, SALARY) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, deptId);

            // Oracle NULL handling
            if (reportsToId == null || reportsToId == 0) {
                pstmt.setNull(3, java.sql.Types.NUMERIC);
            } else {
                pstmt.setLong(3, reportsToId);
            }

            pstmt.setString(4, jobDesc);
            pstmt.setDouble(5, salary);

            int rowsAffected = pstmt.executeUpdate();

            // If your connection doesn't auto-commit, you MUST do it here:
            // conn.commit();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL ERROR in ManagerRepository: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find a Manager by their associated User ID (Used for Login/Profile).
     */
    public Optional<Manager> findByUserId(long userId) {
        String sql = """
            SELECT m.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM MANAGERS m 
            JOIN USERS u ON m.user_id = u.user_id 
            WHERE m.user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error finding manager by user ID", e);
        }
        return Optional.empty();
    }

    public Optional<Manager> findById(long managerId) {
        String sql = """
        SELECT m.manager_id, m.user_id, m.department_id, m.reports_to_manager_id,
               m.job_description, m.salary,
               u.name, u.email, u.phone_country_code, u.phone_number
        FROM MANAGERS m
        JOIN USERS u ON m.user_id = u.user_id
        WHERE m.manager_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, managerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error finding manager by manager ID", e);
        }
        return Optional.empty();
    }

    /**
     * Admin: Get all managers in a specific department.
     */
    public List<Manager> findByDepartment(long deptId) {
        List<Manager> managers = new ArrayList<>();
        String sql = """
            SELECT m.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM MANAGERS m 
            JOIN USERS u ON m.user_id = u.user_id 
            WHERE m.department_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) managers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error fetching department managers", e);
        }
        return managers;
    }

    public List<Manager> findAll() {
        List<Manager> managers = new ArrayList<>();

        String sql = """
        SELECT m.*, u.name, u.email, u.phone_country_code, u.phone_number 
        FROM MANAGERS m 
        JOIN USERS u ON m.user_id = u.user_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                managers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching all managers", e);
        }

        return managers;
    }

    public List<Manager> findByDepartmentExcludingSelf(long deptId, long userId) {
        List<Manager> managers = new ArrayList<>();

        String sql = """
        SELECT m.*, u.name, u.email, u.phone_country_code, u.phone_number 
        FROM MANAGERS m 
        JOIN USERS u ON m.user_id = u.user_id 
        WHERE m.department_id = ? AND m.user_id != ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) managers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            handleSQLException("Error fetching filtered managers", e);
        }

        return managers;
    }

    /**
     * Hierarchy Logic: Find all managers who report to a specific manager.
     */
    public List<Manager> findSubordinateManagers(long managerId) {
        List<Manager> subs = new ArrayList<>();
        String sql = """
            SELECT m.*, u.name, u.email, u.phone_country_code, u.phone_number 
            FROM MANAGERS m 
            JOIN USERS u ON m.user_id = u.user_id 
            WHERE m.reports_to_manager_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) subs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error fetching subordinate managers", e);
        }
        return subs;
    }

    /**
     * Admin: Update manager professional details.
     */
    public boolean updateManagerDetails(long managerId, long deptId, Long reportsTo, String jobDesc, double salary) {
        String sql = """
            UPDATE MANAGERS 
            SET department_id = ?, reports_to_manager_id = ?, job_description = ?, salary = ? 
            WHERE manager_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);
            if (reportsTo == null || reportsTo == 0) {
                stmt.setNull(2, Types.NUMERIC);
            } else {
                stmt.setLong(2, reportsTo);
            }
            stmt.setString(3, jobDesc);
            stmt.setDouble(4, salary);
            stmt.setLong(5, managerId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating manager details", e);
            return false;
        }
    }

    private Manager mapRow(ResultSet rs) throws SQLException {
        Manager m = new Manager();
        m.setManagerId(rs.getLong("manager_id"));
        m.setUserId(rs.getLong("user_id"));
        m.setDepartmentId(rs.getLong("department_id"));

        long reportsTo = rs.getLong("reports_to_manager_id");
        m.setReportsToManagerId(rs.wasNull() ? null : reportsTo);

        m.setJobDescription(rs.getString("job_description"));
        m.setSalary(rs.getDouble("salary"));

        // Mapped User Info
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setPhoneCountryCode(rs.getString("phone_country_code"));
        m.setPhoneNumber(rs.getString("phone_number"));

        return m;
    }

    private void handleSQLException(String message, SQLException e) {
        System.err.println("ManagerRepository: " + message + " -> " + e.getMessage());
    }
}