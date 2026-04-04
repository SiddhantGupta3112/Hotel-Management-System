package com.hotel.app.controller;

import com.hotel.app.entity.Room;
import com.hotel.app.service.RoomService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyServicesController {

    @FXML private TableView<Room> availableRoomsTable;
    @FXML private TableColumn<Room, Integer> colRoomNo;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Integer> colFloor;
    @FXML private TableColumn<Room, Integer> colCapacity;
    @FXML private TableColumn<Room, String> colPrice;
    @FXML private TableColumn<Room, String> colStatus;

    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        setupColumns();
        loadAvailableRooms();
    }

    private void setupColumns() {
        // Standard Integer/String bindings
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom Currency Formatting (e.g., ₹4,000)
        colPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getPricePerNight();
            return new SimpleStringProperty(String.format("₹%,.0f", price));
        });

        // Status Color Coding (Consistent with Admin View)
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Since this table only shows available rooms, it will be green
                    if (item.equalsIgnoreCase("AVAILABLE")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });
    }

    private void loadAvailableRooms() {
        // Fetch only available rooms from the service layer
        availableRoomsTable.setItems(
                FXCollections.observableArrayList(roomService.getAvailableRooms())
        );
    }
}