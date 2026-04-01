package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.Customer;
import com.hotel.app.entity.Room;
import com.hotel.app.repository.BookingRepository;
import com.hotel.app.repository.CustomerRepository;
import com.hotel.app.repository.RoomRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class BookingController {

    @FXML private TableView<Booking>              bookingTable;
    @FXML private TableColumn<Booking, Long>      colId;
    @FXML private TableColumn<Booking, String>    colGuest;
    @FXML private TableColumn<Booking, Integer>   colRoom;
    @FXML private TableColumn<Booking, String>    colType;
    @FXML private TableColumn<Booking, String>    colCheckIn;
    @FXML private TableColumn<Booking, String>    colCheckOut;
    @FXML private TableColumn<Booking, String>    colNights;
    @FXML private TableColumn<Booking, String>    colStatus;

    // New booking form
    @FXML private ComboBox<Customer>  customerCombo;
    @FXML private ComboBox<Room>      roomCombo;
    @FXML private DatePicker          checkInPicker;
    @FXML private DatePicker          checkOutPicker;
    @FXML private Label               formErrorLabel;
    @FXML private Label               totalLabel;

    private final BookingRepository  bookRepo = new BookingRepository();
    private final CustomerRepository custRepo = new CustomerRepository();
    private final RoomRepository     roomRepo = new RoomRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadCombos();
        loadBookings();
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
    }

    private void setupColumns() {
        colId    .setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colGuest .setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom  .setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType  .setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colCheckIn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckInDate() != null
                        ? data.getValue().getCheckInDate().toString() : "-"));
        colCheckOut.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckOutDate() != null
                        ? data.getValue().getCheckOutDate().toString() : "-"));
        colNights.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNights() + " night(s)"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                switch (s) {
                    case "CONFIRMED"   -> setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold;");
                    case "CHECKED_IN"  -> setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    case "CHECKED_OUT" -> setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                    case "CANCELLED"   -> setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                    default            -> setStyle("-fx-text-fill: #e2e8f0;");
                }
            }
        });

        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCombos() {
        customerCombo.setItems(FXCollections.observableArrayList(custRepo.findAll()));
        // Only show available rooms for new bookings
        List<Room> rooms = roomRepo.findAll().stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .toList();
        roomCombo.setItems(FXCollections.observableArrayList(rooms));
    }

    private void loadBookings() {
        List<Booking> list = bookRepo.findAll();
        bookingTable.setItems(FXCollections.observableArrayList(list));
        totalLabel.setText("Total bookings: " + list.size());
    }

    @FXML
    private void handleNewBooking() {
        Customer customer = customerCombo.getValue();
        Room room         = roomCombo.getValue();
        LocalDate ci      = checkInPicker.getValue();
        LocalDate co      = checkOutPicker.getValue();

        if (customer == null || room == null || ci == null || co == null) {
            showError("Please fill in all fields.");
            return;
        }
        if (!co.isAfter(ci)) {
            showError("Check-out must be after check-in.");
            return;
        }

        boolean ok = bookRepo.save(customer.getCustomerId(), room.getRoomId(), ci, co);
        if (ok) {
            clearForm();
            loadCombos();  // refresh available rooms
            loadBookings();
        } else {
            showError("Failed to create booking.");
        }
    }

    @FXML
    private void handleCheckIn() {
        Booking b = bookingTable.getSelectionModel().getSelectedItem();
        if (b == null) { showError("Select a booking first."); return; }
        if (!"CONFIRMED".equals(b.getBookingStatus())) {
            showError("Only CONFIRMED bookings can be checked in."); return;
        }
        bookRepo.checkIn(b.getBookingId(), b.getRoomId());
        loadBookings();
    }

    @FXML
    private void handleCheckOut() {
        Booking b = bookingTable.getSelectionModel().getSelectedItem();
        if (b == null) { showError("Select a booking first."); return; }
        if (!"CHECKED_IN".equals(b.getBookingStatus())) {
            showError("Only CHECKED_IN bookings can be checked out."); return;
        }
        bookRepo.checkOut(b.getBookingId(), b.getRoomId());
        loadBookings();
    }

    @FXML
    private void handleCancel() {
        Booking b = bookingTable.getSelectionModel().getSelectedItem();
        if (b == null) { showError("Select a booking first."); return; }
        if ("CHECKED_OUT".equals(b.getBookingStatus()) ||
            "CANCELLED".equals(b.getBookingStatus())) {
            showError("Cannot cancel this booking."); return;
        }
        bookRepo.cancel(b.getBookingId(), b.getRoomId());
        loadBookings();
    }

    @FXML
    private void handleRefresh() { loadBookings(); }

    private void clearForm() {
        customerCombo.setValue(null);
        roomCombo.setValue(null);
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        formErrorLabel.setVisible(false);
    }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
    }
}
