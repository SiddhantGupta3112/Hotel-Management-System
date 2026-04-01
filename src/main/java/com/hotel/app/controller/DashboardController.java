package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.User;
import com.hotel.app.repository.DashboardRepository;
import com.hotel.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List; 


public class DashboardController {

    // ── Top bar ──────────────────────────────────────────────
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label avatarLabel;

    // ── Sidebar nav items ────────────────────────────────────
    @FXML private Label navDashboard;
    @FXML private Label navRooms;
    @FXML private Label navBookings;
    @FXML private Label navCustomers;
    @FXML private Label navBilling;

    // ── Content swap pane ────────────────────────────────────
    @FXML private StackPane contentPane;

    // ── Stat cards ───────────────────────────────────────────
    @FXML private Label statTotalRooms;
    @FXML private Label statAvailableRooms;
    @FXML private Label statTodayCheckIns;
    @FXML private Label statTodayCheckOuts;
    @FXML private Label statTodayRevenue;
    @FXML private Label statMonthRevenue;

    // ── Recent bookings table ────────────────────────────────
    @FXML private TableView<Booking> recentBookingsTable;
    @FXML private TableColumn<Booking, Long>   colBookingId;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, Integer> colRoom;
    @FXML private TableColumn<Booking, String> colRoomType;
    @FXML private TableColumn<Booking, String> colCheckIn;
    @FXML private TableColumn<Booking, String> colCheckOut;
    @FXML private TableColumn<Booking, String> colStatus;

    private final DashboardRepository dashRepo = new DashboardRepository();
    private Node dashboardContent;

    // ── Init ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        dashboardContent = contentPane.getChildren().get(0);
        setupUserInfo();
        setupTableColumns();
        loadStats();
        loadRecentBookings();
        setActiveNav(navDashboard);
    }

    private void setupUserInfo() {
        SessionManager session = SessionManager.getInstance();
        User user = session.getCurrentUser();
        List<String> roles = session.getRoles();
        String primaryRole = roles.isEmpty() ? "No Role" : roles.get(0);

        userNameLabel.setText(user.getName());
        userRoleLabel.setText(primaryRole);
        avatarLabel.setText(getInitials(user.getName()));
    }

    private void setupTableColumns() {
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colCustomer .setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom     .setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomType .setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colStatus   .setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

        // Format dates as strings
        colCheckIn.setCellValueFactory(data -> {
            Booking b = data.getValue();
            String s = b.getCheckInDate() != null ? b.getCheckInDate().toString() : "-";
            return new javafx.beans.property.SimpleStringProperty(s);
        });
        colCheckOut.setCellValueFactory(data -> {
            Booking b = data.getValue();
            String s = b.getCheckOutDate() != null ? b.getCheckOutDate().toString() : "-";
            return new javafx.beans.property.SimpleStringProperty(s);
        });

        recentBookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadStats() {
        statTotalRooms    .setText(String.valueOf(dashRepo.getTotalRooms()));
        statAvailableRooms.setText(String.valueOf(dashRepo.getAvailableRooms()));
        statTodayCheckIns .setText(String.valueOf(dashRepo.getTodayCheckIns()));
        statTodayCheckOuts.setText(String.valueOf(dashRepo.getTodayCheckOuts()));
        statTodayRevenue  .setText(String.format("₹ %,.2f", dashRepo.getTodayRevenue()));
        statMonthRevenue  .setText(String.format("₹ %,.2f", dashRepo.getMonthRevenue()));
    }

    private void loadRecentBookings() {
        List<Booking> bookings = dashRepo.getRecentBookings();
        recentBookingsTable.setItems(FXCollections.observableArrayList(bookings));
    }

    // ── Sidebar navigation ───────────────────────────────────

    @FXML private void handleNavDashboard() {
        setActiveNav(navDashboard);
        contentPane.getChildren().setAll(dashboardContent);
        // Already on home — just reload stats
        loadStats();
        loadRecentBookings();
    }

    @FXML private void handleNavRooms() {
        setActiveNav(navRooms);
        loadSection("/fxml/Rooms.fxml");
    }

    @FXML private void handleNavBookings() {
        setActiveNav(navBookings);
        loadSection("/fxml/Bookings.fxml");
    }

    @FXML private void handleNavCustomers() {
        setActiveNav(navCustomers);
        loadSection("/fxml/Customers.fxml");
    }

    @FXML private void handleNavBilling() {
        setActiveNav(navBilling);
        loadSection("/fxml/Billing.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigateToLogin();
    }

    // ── Helpers ──────────────────────────────────────────────

    /**
     * Loads an FXML into the center contentPane (sidebar stays fixed).
     */
    private void loadSection(String fxmlPath) {
        try {
            Node section = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(section);
        } catch (IOException e) {
            System.err.println("Failed to load section: " + fxmlPath + " — " + e.getMessage());
        }
    }

    private void setActiveNav(Label active) {
        Label[] all = {navDashboard, navRooms, navBookings, navCustomers, navBilling};
        for (Label nav : all) {
            nav.getStyleClass().removeAll("nav-item-active");
            nav.getStyleClass().add("nav-item");
        }
        active.getStyleClass().removeAll("nav-item");
        active.getStyleClass().add("nav-item-active");
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}
