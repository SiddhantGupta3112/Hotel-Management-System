package com.hotel.app.controller.modules.staff;

import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.repository.ServiceRepository;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hotel.app.util.DBConnection;

public class CompletedTasksController {

    @FXML private TableView<ServiceRequest>           completedTable;
    @FXML private TableColumn<ServiceRequest, String> colRoom;
    @FXML private TableColumn<ServiceRequest, String> colGuest;
    @FXML private TableColumn<ServiceRequest, String> colService;
    @FXML private TableColumn<ServiceRequest, Number> colQty;
    @FXML private TableColumn<ServiceRequest, String> colCharge;
    @FXML private TableColumn<ServiceRequest, String> colServedAt;

    @FXML private Label totalServedLabel;
    @FXML private Label totalEarnedLabel;

    @FXML
    public void initialize() {
        setupColumns();
        loadCompletedTasks();
    }

    private void setupColumns() {
        colRoom   .setCellValueFactory(data ->
                new SimpleStringProperty("Room " + data.getValue().getRoomNumber()));
        colGuest  .setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colService.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colQty    .setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCharge .setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getChargeDisplay()));
        colServedAt.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRequestedAtFormatted()));

        completedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCompletedTasks() {
        long userId = SessionManager.getInstance().getCurrentUser().getUserId();
        List<ServiceRequest> tasks = findServedByUser(userId);
        completedTable.setItems(FXCollections.observableArrayList(tasks));

        double totalEarned = tasks.stream()
                .mapToDouble(r -> r.getServicePrice() * r.getQuantity())
                .sum();

        totalServedLabel.setText("Tasks completed: " + tasks.size());
        totalEarnedLabel.setText(String.format("Total billed: ₹ %,.0f", totalEarned));
    }

    @FXML
    private void handleRefresh() {
        loadCompletedTasks();
    }

    /**
     * Fetches all SERVED requests handled by this staff member.
     * Kept here (not in ServiceRepository) because it's a staff-specific
     * view — ServiceRepository is focused on customer and queue operations.
     */
    private List<ServiceRequest> findServedByUser(long userId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql =
            "SELECT sr.request_id, sr.booking_id, sr.service_id, sr.quantity, " +
            "       sr.notes, sr.status, sr.served_at AS requested_at, " +
            "       s.name AS service_name, s.base_price, " +
            "       u.name AS guest_name, r.room_number " +
            "FROM SERVICE_REQUESTS sr " +
            "JOIN SERVICES  s  ON sr.service_id  = s.service_id " +
            "JOIN BOOKINGS  b  ON sr.booking_id  = b.booking_id " +
            "JOIN CUSTOMERS c  ON b.customer_id  = c.customer_id " +
            "JOIN USERS     u  ON c.user_id      = u.user_id " +
            "JOIN ROOMS     r  ON b.room_id      = r.room_id " +
            "WHERE sr.status = 'SERVED' AND sr.served_by = ? " +
            "ORDER BY sr.served_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRequest req = new ServiceRequest();
                    req.setRequestId(rs.getInt("request_id"));
                    req.setBookingId(rs.getInt("booking_id"));
                    req.setServiceId(rs.getInt("service_id"));
                    req.setServiceName(rs.getString("service_name"));
                    req.setServicePrice(rs.getDouble("base_price"));
                    req.setQuantity(rs.getInt("quantity"));
                    req.setNotes(rs.getString("notes"));
                    req.setStatus(rs.getString("status"));
                    req.setGuestName(rs.getString("guest_name"));
                    req.setRoomNumber(rs.getInt("room_number"));
                    Timestamp ts = rs.getTimestamp("requested_at");
                    if (ts != null) req.setRequestedAt(ts.toLocalDateTime());
                    list.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("CompletedTasksController.findServedByUser: " + e.getMessage());
        }
        return list;
    }
}
