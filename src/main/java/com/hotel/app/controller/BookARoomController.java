package com.hotel.app.controller;

import com.hotel.app.entity.Room;
import com.hotel.app.service.BookingService;
import com.hotel.app.service.RoomService;
import com.hotel.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookARoomController {
    @FXML private ComboBox<Room> roomCombo;
    @FXML private DatePicker checkInPicker, checkOutPicker;
    @FXML private Label costPreviewLabel, feedbackLabel;
    @FXML private TableView<Room> myRoomsTable;
    @FXML private TableColumn<Room, Integer> colRoomNo, colFloor, colCapacity;
    @FXML private TableColumn<Room, String> colType, colStatus;
    @FXML private TableColumn<Room, Double> colPrice;

    private final BookingService bookingService = new BookingService();
    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        setupTable();
        loadRooms();

        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        roomCombo.valueProperty().addListener((o, old, newVal) -> updateCostPreview());
        checkInPicker.valueProperty().addListener((o, old, newVal) -> updateCostPreview());
        checkOutPicker.valueProperty().addListener((o, old, newVal) -> updateCostPreview());
    }

    private void setupTable() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        // FIXED: Changed from "typeName" to "roomTypeName" to match Room entity getter
        colType.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));

        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadRooms() {
        List<Room> available = roomService.getAllRooms().stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus())).toList();
        roomCombo.setItems(FXCollections.observableArrayList(available));
        myRoomsTable.setItems(FXCollections.observableArrayList(available));
    }

    private void updateCostPreview() {
        Room r = roomCombo.getValue();
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();

        if (r != null && in != null && out != null && out.isAfter(in)) {
            long nights = ChronoUnit.DAYS.between(in, out);
            double total = nights * r.getPricePerNight();
            costPreviewLabel.setText(String.format("Estimated cost: ₹%,.0f for %d nights", total, nights));
        } else {
            costPreviewLabel.setText("Select room and dates to see price");
        }
    }

    @FXML
    private void handleConfirmBooking() {
        long customerId = SessionManager.getInstance().getCustomerId();
        Room r = roomCombo.getValue();
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();

        if (r == null) { showFeedback("Please select a room", true); return; }

        var result = bookingService.makeBooking(customerId, r.getRoomId(), in, out);
        if (result.isSuccess()) {
            showFeedback("Booking request submitted! Awaiting admin approval.", false);
            loadRooms();
        } else {
            showFeedback(result.getErrorMessage(), true);
        }
    }

    private void showFeedback(String msg, boolean error) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle("-fx-text-fill: " + (error ? "red" : "green"));
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}