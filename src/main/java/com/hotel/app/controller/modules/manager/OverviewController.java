package com.hotel.app.controller.modules.manager;

import com.hotel.app.repository.InvoiceRepository;
import com.hotel.app.repository.RoomRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Manager Overview Dashboard.
 * Handles real-time statistics for rooms, revenue, and alerts.
 */
public class OverviewController {

    // Room Stats
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label maintenanceRoomsLabel;

    // Revenue Stats
    @FXML private Label todayRevenueLabel;
    @FXML private Label monthRevenueLabel;
    @FXML private Label yearRevenueLabel;

    // Activity Stats
    @FXML private Label todayCheckInsLabel;
    @FXML private Label todayCheckOutsLabel;
    @FXML private Label pendingInvoicesLabel;

    // Alert Stats
    @FXML private Label overdueCheckoutsLabel;
    @FXML private Label missedCheckinsLabel;
    @FXML private Label pendingServicesLabel;

    private final RoomRepository roomRepo = new RoomRepository();
    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    // Assuming you have these or similar names in your teammate's new work
    // private final BookingRepository bookingRepo = new BookingRepository();

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    @FXML
    private void handleRefresh() {
        refreshDashboard();
    }

    /**
     * Fetches data asynchronously to keep the UI responsive during DB calls.
     * Important for WSL environments where network latency can occur.
     */
    private void refreshDashboard() {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Fetch Room Data
                int total = roomRepo.getRoomCount();
                int available = roomRepo.getRoomCountByStatus("AVAILABLE");
                int occupied = roomRepo.getRoomCountByStatus("OCCUPIED");
                int maintenance = roomRepo.getRoomCountByStatus("MAINTENANCE");

                // 2. Fetch Revenue Data (From teammate's new InvoiceRepository)
                double todayRev = invoiceRepo.getRevenueByDays(1);
                double monthRev = invoiceRepo.getRevenueByMonths(1);
                double yearRev = invoiceRepo.getRevenueByYears(1);
                int pendingInv = 0;

                // 3. Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    updateRoomLabels(total, available, occupied, maintenance);
                    updateRevenueLabels(todayRev, monthRev, yearRev);
                    pendingInvoicesLabel.setText(String.valueOf(pendingInv));

                    // Placeholder logic for activity/alerts until those methods are in repos
                    todayCheckInsLabel.setText("5");
                    todayCheckOutsLabel.setText("3");
                    overdueCheckoutsLabel.setText("1");
                    missedCheckinsLabel.setText("0");
                    pendingServicesLabel.setText("4");
                });

            } catch (Exception e) {
                System.err.println("Failed to refresh overview data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void updateRoomLabels(int total, int available, int occupied, int maintenance) {
        totalRoomsLabel.setText(String.valueOf(total));
        availableRoomsLabel.setText(String.valueOf(available));
        occupiedRoomsLabel.setText(String.valueOf(occupied));
        maintenanceRoomsLabel.setText(String.valueOf(maintenance));
    }

    private void updateRevenueLabels(double today, double month, double year) {
        todayRevenueLabel.setText(currencyFormat.format(today));
        monthRevenueLabel.setText(currencyFormat.format(month));
        yearRevenueLabel.setText(currencyFormat.format(year));
    }
}
