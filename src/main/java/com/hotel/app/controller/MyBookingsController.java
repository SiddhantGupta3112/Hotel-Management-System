package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.service.BookingService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;

public class MyBookingsController {
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> colRoom;
    @FXML private TableColumn<Booking, String> colCheckIn, colCheckOut, colStatus, colTotalCost;

    @FXML private Button btnRequestCheckIn, btnRequestCheckOut, btnCancel;
    @FXML private Label statActiveBookings, statLoyaltyPoints, feedbackLabel;

    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        setupColumns();

        // Selection Listener for dynamic button enabling
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                btnRequestCheckIn.setDisable(!newVal.canRequestCheckIn());
                btnRequestCheckOut.setDisable(!newVal.canRequestCheckOut());
                btnCancel.setDisable(!newVal.canCancel());
            } else {
                setButtonsDisabled(true);
            }
        });

        loadMyBookings();
    }

    private void setupColumns() {
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colTotalCost.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%,.0f", c.getValue().getTotalCost())));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (item) {
                        case "PENDING" -> { setText("Awaiting Approval"); setTextFill(Color.DARKORANGE); }
                        case "APPROVED" -> { setText("Approved"); setTextFill(Color.BLUE); }
                        case "REJECTED" -> { setText("Rejected"); setTextFill(Color.RED); }
                        case "CHECKIN_PENDING" -> { setText("Check-in Requested"); setTextFill(Color.DARKORANGE); }
                        case "CHECKED_IN" -> { setText("Checked In"); setTextFill(Color.GREEN); }
                        case "CHECKOUT_PENDING" -> { setText("Check-out Requested"); setTextFill(Color.DARKORANGE); }
                        case "CHECKED_OUT" -> { setText("Checked Out"); setTextFill(Color.GRAY); }
                        case "CANCELLED" -> { setText("Cancelled"); setTextFill(Color.RED); }
                        default -> setText(item);
                    }
                }
            }
        });
    }

    private void loadMyBookings() {
        long cid = SessionManager.getInstance().getCustomerId();
        var bookings = bookingService.getBookingsForCustomer(cid);
        bookingsTable.setItems(FXCollections.observableArrayList(bookings));

        // Stats updates
        long active = bookings.stream().filter(b -> List.of("APPROVED", "CHECKED_IN", "CHECKIN_PENDING", "CHECKOUT_PENDING").contains(b.getBookingStatus())).count();
        statActiveBookings.setText(String.valueOf(active));

        bookingService.getCustomerByUserId(SessionManager.getInstance().getCurrentUser().getUserId())
                .ifPresent(c -> statLoyaltyPoints.setText(c.getLoyaltyPoints() + " pts"));
    }

    @FXML
    private void handleRequestCheckIn() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        var res = bookingService.requestCheckIn(selected.getBookingId(), SessionManager.getInstance().getCustomerId());
        handleResult(res, "Check-in requested. Awaiting admin approval.");
    }

    @FXML
    private void handleRequestCheckOut() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        var res = bookingService.requestCheckOut(selected.getBookingId(), SessionManager.getInstance().getCustomerId());
        handleResult(res, "Check-out requested. Awaiting admin approval.");
    }

    @FXML
    private void handleCancel() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        var res = bookingService.cancelBooking(selected.getBookingId(), SessionManager.getInstance().getCustomerId());
        handleResult(res, "Booking cancelled successfully.");
    }

    private void handleResult(BookingService.Result<Void> res, String successMsg) {
        if (res.isSuccess()) {
            showFeedback(successMsg, false);
            loadMyBookings();
        } else {
            showFeedback(res.getErrorMessage(), true);
        }
    }

    private void setButtonsDisabled(boolean disabled) {
        btnRequestCheckIn.setDisable(disabled);
        btnRequestCheckOut.setDisable(disabled);
        btnCancel.setDisable(disabled);
    }

    private void showFeedback(String msg, boolean error) {
        feedbackLabel.setText(msg);
        feedbackLabel.setTextFill(error ? Color.RED : Color.GREEN);
        feedbackLabel.setVisible(true);
    }
}