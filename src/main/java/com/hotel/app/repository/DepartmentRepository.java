package com.hotel.app.repository;

import com.hotel.app.entity.Department;
import com.hotel.app.entity.Manager;
import com.hotel.app.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentRepository {

    /**
     * Finds a department where the given manager is assigned as the Head (HOD).
     * Used by the Profile Controller to determine if a manager is an HOD.
     */
    public Optional<Department> findByHeadManagerId(long headManagerId) {
        String sql = "SELECT * FROM DEPARTMENTS WHERE head_manager_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, headManagerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error finding department by Head Manager ID", e);
        }
        return Optional.empty();
    }

    /**
     * Admin: Create a new department.
     */
    public boolean save(String name, Long headManagerId) {
        String sql = "INSERT INTO DEPARTMENTS (department_name, head_manager_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            if (headManagerId == null || headManagerId == 0) {
                stmt.setNull(2, Types.NUMERIC);
            } else {
                stmt.setLong(2, headManagerId);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error creating department", e);
            return false;
        }
    }

    /**
     * Admin: Update department details.
     */
    public boolean update(long id, String newName, Long newHeadId) {
        String sql = "UPDATE DEPARTMENTS SET department_name = ?, head_manager_id = ? WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName.trim());
            if (newHeadId == null || newHeadId == 0) {
                stmt.setNull(2, Types.NUMERIC);
            } else {
                stmt.setLong(2, newHeadId);
            }
            stmt.setLong(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating department", e);
            return false;
        }
    }

    /**
     * Admin: Delete a department.
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM DEPARTMENTS WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting department (Ensure it's empty first)", e);
            return false;
        }
    }

    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM DEPARTMENTS ORDER BY department_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error fetching all departments", e);
        }
        return list;
    }

    public Optional<Department> findById(long id) {
        String sql = "SELECT * FROM DEPARTMENTS WHERE department_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error finding department by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Check if a department name already exists.
     */
    public boolean exists(String name) {
        String sql = "SELECT 1 FROM DEPARTMENTS WHERE LOWER(department_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getLong("department_id"));
        d.setDepartmentName(rs.getString("department_name"));

        long headId = rs.getLong("head_manager_id");
        d.setHeadManagerId(rs.wasNull() ? 0 : headId);

        return d;
    }

    private void handleSQLException(String message, SQLException e) {
        System.err.println("DepartmentRepository: " + message + " -> " + e.getMessage());
    }

    public boolean updateHead(long deptId, long managerId) {
        String sql = "UPDATE DEPARTMENTS SET head_manager_id = ? WHERE department_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, managerId);
            stmt.setLong(2, deptId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating HOD: " + e.getMessage());
            return false;
        }
    }

    public Optional<Manager> getHOD(long deptId) {
        String sql = """
            SELECT 
                D.DEPARTMENT_ID, 
                D.DEPARTMENT_NAME, 
                U.NAME AS HOD_NAME,
                M.JOB_DESCRIPTION AS HOD_TITLE,
                U.EMAIL AS HOD_EMAIL,
                D.HEAD_MANAGER_ID
            FROM DEPARTMENTS D
            LEFT JOIN MANAGERS M ON D.HEAD_MANAGER_ID = M.MANAGER_ID
            LEFT JOIN USERS U    ON M.USER_ID = U.USER_ID
            WHERE D.department_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, deptId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Check if an HOD actually exists for this department
                    long managerId = rs.getLong("HEAD_MANAGER_ID");
                    if (rs.wasNull()) {
                        return Optional.empty();
                    }

                    // Create the Manager object
                    Manager manager = new Manager();
                    manager.setManagerId(managerId);
                    manager.setName(rs.getString("HOD_NAME"));
                    manager.setJobDescription(rs.getString("HOD_TITLE"));
                    manager.setEmail(rs.getString("HOD_EMAIL"));
                    manager.setDepartmentId(rs.getLong("DEPARTMENT_ID"));

                    return Optional.of(manager);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching HOD: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Manager> getAllHODs() {
        List<Manager> hodList = new ArrayList<>();
        String sql = """
        SELECT D.DEPARTMENT_ID, U.NAME AS HOD_NAME, M.JOB_DESCRIPTION AS HOD_TITLE,
               U.EMAIL AS HOD_EMAIL, D.HEAD_MANAGER_ID
        FROM DEPARTMENTS D
        JOIN MANAGERS M ON D.HEAD_MANAGER_ID = M.MANAGER_ID
        JOIN USERS U ON M.USER_ID = U.USER_ID
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Manager m = new Manager();
                m.setManagerId(rs.getLong("HEAD_MANAGER_ID"));
                m.setName(rs.getString("HOD_NAME"));
                m.setJobDescription(rs.getString("HOD_TITLE"));
                m.setEmail(rs.getString("HOD_EMAIL"));
                m.setDepartmentId(rs.getLong("DEPARTMENT_ID"));
                hodList.add(m);
            }
        } catch (SQLException e) {
            handleSQLException("Error fetching HOD list", e);
        }
        return hodList;
    }
}