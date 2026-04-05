package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.repository.DashboardRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for modules/manager/overview.fxml.
 *
 * This must be a SEPARATE class from DashboardController.
 * When BaseDashboardController.loadModule() loads overview.fxml, JavaFX
 * instantiates a fresh controller and calls initialize(). If overview.fxml
 * used DashboardController as its controller, initialize() would call
 * onDashboardReady() → navContainer.getChildren() → NullPointerException
 * (no navContainer in overview.fxml), and also loadModule("overview.fxml")
 * → infinite loop.
 */
public class OverviewController {

    @FXML private Label revenueLabel;
    @FXML private Label occupancyLabel;
    @FXML private Label pendingLabel;

    @FXML private TableView<Booking>           recentBookingsTable;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, String> colAmount;

    private final DashboardRepository dashRepo = new DashboardRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadStats();
        loadRecentBookings();
    }

    private void setupColumns() {
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom    .setCellValueFactory(data ->
                new SimpleStringProperty("Room " + data.getValue().getRoomNumber()));
        colStatus  .setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        colAmount  .setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₹ %,.0f", data.getValue().getTotalCost())));

        recentBookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadStats() {
        double revenue   = dashRepo.getTodayRevenue();
        int    total     = dashRepo.getTotalRooms();
        int    occupied  = dashRepo.getOccupiedRooms();
        int    pending   = dashRepo.getTodayCheckIns();

        revenueLabel  .setText(String.format("₹ %,.0f", revenue));
        occupancyLabel.setText(total > 0
                ? String.format("%.0f%%", (occupied * 100.0 / total))
                : "0%");
        pendingLabel  .setText(String.valueOf(pending));
    }

    private void loadRecentBookings() {
        List<Booking> bookings = dashRepo.getRecentBookings();
        recentBookingsTable.setItems(FXCollections.observableArrayList(bookings));
    }
}
