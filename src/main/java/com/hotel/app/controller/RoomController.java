package com.hotel.app.controller;

import com.hotel.app.entity.Room;
import com.hotel.app.entity.RoomType;
import com.hotel.app.service.RoomService;
import com.hotel.app.service.RoomService.Result;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RoomController {

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> colRoomNo;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Integer> colFloor;
    @FXML private TableColumn<Room, String> colPrice;
    @FXML private TableColumn<Room, String> colStatus;

    @FXML private TableView<RoomType> roomTypeTable;
    @FXML private TableColumn<RoomType, String> colTypeName;
    @FXML private TableColumn<RoomType, Integer> colCapacity;
    @FXML private TableColumn<RoomType, Double> colTypePrice;
    @FXML private TableColumn<RoomType, String> colDescription;

    @FXML private TextField roomNumberField, floorField, typeNameField, capacityField, priceField, descriptionField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private Label formFeedbackLabel;

    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        setupRoomTypeColumns();
        setupRoomColumns();
        refreshData();
    }

    private void setupRoomTypeColumns() {
        colTypeName.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colTypePrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void setupRoomColumns() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // CUSTOM COLOR CODING FOR STATUS
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "AVAILABLE" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green
                        case "OCCUPIED" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");  // Red
                        case "MAINTENANCE" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Amber
                        case "RESERVED" -> setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");   // Blue
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    @FXML
    private void handleAddRoomType() {
        try {
            String name = typeNameField.getText();
            int cap = Integer.parseInt(capacityField.getText());
            double price = Double.parseDouble(priceField.getText());
            String desc = descriptionField.getText();

            Result<Long> result = roomService.addRoomType(name, cap, price, desc);
            handleResult(result);
        } catch (NumberFormatException e) {
            showFeedback("Invalid number format in Capacity or Price.", true);
        }
    }

    @FXML
    private void handleAddRoom() {
        try {
            int num = Integer.parseInt(roomNumberField.getText());
            int floor = Integer.parseInt(floorField.getText());
            RoomType selectedType = roomTypeCombo.getValue();

            if (selectedType == null) {
                showFeedback("Please select a Room Type.", true);
                return;
            }

            Result<Long> result = roomService.addRoom(num, selectedType.getRoomTypeId(), floor);
            handleResult(result);
        } catch (NumberFormatException e) {
            showFeedback("Room number and floor must be numbers.", true);
        }
    }

    @FXML
    private void handleChangeStatus() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        List<String> choices = Arrays.asList("AVAILABLE", "OCCUPIED", "MAINTENANCE", "RESERVED");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(), choices);
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Change status for Room " + selected.getRoomNumber());

        dialog.showAndWait().ifPresent(newStatus -> {
            Result<Void> result = roomService.changeRoomStatus(selected.getRoomId(), newStatus);
            handleResult(result);
        });
    }

    @FXML
    private void handleDeleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Room " + selected.getRoomNumber() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                handleResult(roomService.deleteRoom(selected.getRoomId()));
            }
        });
    }

    private void handleResult(Result<?> result) {
        if (result.isSuccess()) {
            showFeedback("Success!", false);
            refreshData();
            clearFields();
        } else {
            showFeedback(result.getError(), true);
        }
    }

    private void refreshData() {
        roomTypeTable.setItems(FXCollections.observableArrayList(roomService.getAllRoomTypes()));
        roomTable.setItems(FXCollections.observableArrayList(roomService.getAllRooms()));
        roomTypeCombo.setItems(FXCollections.observableArrayList(roomService.getAllRoomTypes()));
    }

    private void showFeedback(String message, boolean isError) {
        formFeedbackLabel.setText(message);
        formFeedbackLabel.setVisible(true);
        formFeedbackLabel.setManaged(true);
        formFeedbackLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    private void clearFields() {
        roomNumberField.clear(); floorField.clear();
        typeNameField.clear(); capacityField.clear();
        priceField.clear(); descriptionField.clear();
    }
}