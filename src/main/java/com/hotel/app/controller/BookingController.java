package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.service.BillingService;
import com.hotel.app.service.BookingService;
import com.hotel.app.service.BookingService.Result;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;

public class BookingController {

    // Tables
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableView<Booking> pendingTable;
    @FXML private TableView<Booking> checkInTable;
    @FXML private TableView<Booking> checkOutTable;

    // Stats
    @FXML private Label statPending;
    @FXML private Label statCheckInRequests;
    @FXML private Label statCheckOutRequests;
    @FXML private Label statTotalActive;

    // Feedback
    @FXML private Label feedbackLabel;

    private final BookingService bookingService = new BookingService();
    private final BillingService billingService = new BillingService();

    @FXML
    public void initialize() {
        // Initialize columns for all 4 tables using the same structure
        setupTableColumns(bookingsTable);
        setupTableColumns(pendingTable);
        setupTableColumns(checkInTable);
        setupTableColumns(checkOutTable);

        loadAllData();
    }


    private void setupTableColumns(TableView<Booking> table) {
        TableColumn<Booking, Long> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colId.setPrefWidth(50);

        TableColumn<Booking, String> colGuest = new TableColumn<>("Guest");
        colGuest.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colGuest.setPrefWidth(140);

        TableColumn<Booking, Integer> colRoom = new TableColumn<>("Room");
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Booking, String> colIn = new TableColumn<>("Check In");
        colIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));

        TableColumn<Booking, String> colOut = new TableColumn<>("Check Out");
        colOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

        TableColumn<Booking, String> colCost = new TableColumn<>("Cost");
        colCost.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("₹%,.0f", cellData.getValue().getTotalCost())));

        TableColumn<Booking, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("PENDING")) setTextFill(Color.ORANGE);
                    else if (item.equals("APPROVED") || item.equals("CHECKED_IN")) setTextFill(Color.GREEN);
                    else if (item.equals("REJECTED") || item.equals("CANCELLED")) setTextFill(Color.RED);
                    else setTextFill(Color.GRAY);
                }
            }
        });

        table.getColumns().setAll(colId, colGuest, colRoom, colIn, colOut, colCost, colStatus);
    }

    private void loadAllData() {
        List<Booking> pending = bookingService.getBookingsByStatus("PENDING");
        List<Booking> checkIns = bookingService.getBookingsByStatus("CHECKIN_PENDING");
        List<Booking> checkOuts = bookingService.getBookingsByStatus("CHECKOUT_PENDING");
        List<Booking> all = bookingService.getAllBookings();

        pendingTable.setItems(FXCollections.observableArrayList(pending));
        checkInTable.setItems(FXCollections.observableArrayList(checkIns));
        checkOutTable.setItems(FXCollections.observableArrayList(checkOuts));
        bookingsTable.setItems(FXCollections.observableArrayList(all));

        statPending.setText(String.valueOf(pending.size()));
        statCheckInRequests.setText(String.valueOf(checkIns.size()));
        statCheckOutRequests.setText(String.valueOf(checkOuts.size()));

        // Active = anything not checked out or cancelled
        long activeCount = all.stream()
                .filter(b -> !List.of("CHECKED_OUT", "CANCELLED", "REJECTED").contains(b.getBookingStatus()))
                .count();
        statTotalActive.setText(String.valueOf(activeCount));
    }

    // --- Action Handlers ---

    @FXML void handleApproveBooking() {
        processAction(bookingService.approveBooking(getSelectedId(pendingTable)));
    }

    @FXML void handleRejectBooking() {
        processAction(bookingService.rejectBooking(getSelectedId(pendingTable)));
    }

    @FXML void handleApproveCheckIn() {
        processAction(bookingService.approveCheckIn(getSelectedId(checkInTable)));
    }

    @FXML void handleRejectCheckIn() {
        processAction(bookingService.rejectCheckIn(getSelectedId(checkInTable)));
    }

    @FXML void handleApproveCheckOut() {
        processAction(bookingService.approveCheckOut(getSelectedId(checkOutTable)));
    }

    @FXML void handleRejectCheckOut() {
        processAction(bookingService.rejectCheckOut(getSelectedId(checkOutTable)));
    }

    @FXML void handleGenerateInvoice() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Please select a booking from the main table", true);
            return;
        }
        processAction(billingService.generateInvoice(selected.getBookingId()));
    }

    @FXML void handleRefresh() {
        loadAllData();
        showFeedback("Data Refreshed", false);
    }

    // --- Helpers ---

    private long getSelectedId(TableView<Booking> table) {
        Booking selected = table.getSelectionModel().getSelectedItem();
        return selected != null ? selected.getBookingId() : -1;
    }

    private void processAction(Result<?> result) {
        if (result.isSuccess()) {
            showFeedback("Action Successful", false);
            loadAllData();
        } else {
            showFeedback(result.getErrorMessage(), true);
        }
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}