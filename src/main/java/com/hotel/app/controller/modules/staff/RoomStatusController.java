package com.hotel.app.controller.modules.staff;

import com.hotel.app.entity.Room;
import com.hotel.app.service.RoomService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class RoomStatusController {

    @FXML private TableView<Room>               roomTable;
    @FXML private TableColumn<Room, Integer>    colRoomNo;
    @FXML private TableColumn<Room, String>     colType;
    @FXML private TableColumn<Room, Integer>    colFloor;
    @FXML private TableColumn<Room, String>     colPrice;
    @FXML private TableColumn<Room, String>     colStatus;

    @FXML private Label totalLabel;
    @FXML private Label availableLabel;
    @FXML private Label occupiedLabel;

    private final RoomService roomService = new RoomService();

    @FXML
    public void initialize() {
        setupColumns();
        loadRooms();
    }

    private void setupColumns() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType  .setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colFloor .setCellValueFactory(new PropertyValueFactory<>("floor"));
        colPrice .setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₹ %,.0f/night", data.getValue().getPricePerNight())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status) {
                    case "AVAILABLE"   -> setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    case "OCCUPIED"    -> setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                    case "MAINTENANCE" -> setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    case "RESERVED"    -> setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold;");
                    default            -> setStyle("-fx-text-fill: #e2e8f0;");
                }
            }
        });

        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadRooms() {
        List<Room> rooms = roomService.getAllRooms();
        roomTable.setItems(FXCollections.observableArrayList(rooms));

        long available = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupied  = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();

        totalLabel    .setText("Total: " + rooms.size());
        availableLabel.setText("Available: " + available);
        occupiedLabel .setText("Occupied: " + occupied);
    }

    @FXML
    private void handleRefresh() {
        loadRooms();
    }
}
