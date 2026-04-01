package com.hotel.app.controller;

import com.hotel.app.entity.Room;
import com.hotel.app.entity.RoomType;
import com.hotel.app.repository.RoomRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class RoomController {

    @FXML private TableView<Room>               roomTable;
    @FXML private TableColumn<Room, Integer>    colRoomNo;
    @FXML private TableColumn<Room, String>     colType;
    @FXML private TableColumn<Room, Integer>    colFloor;
    @FXML private TableColumn<Room, String>     colPrice;
    @FXML private TableColumn<Room, String>     colStatus;

    @FXML private TextField     roomNumberField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private TextField     floorField;
    @FXML private Label         formErrorLabel;
    @FXML private Label         totalLabel;
    @FXML private Label         availableLabel;

    private final RoomRepository repo = new RoomRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadRoomTypes();
        loadRooms();
    }

    private void setupColumns() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colType  .setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        colFloor .setCellValueFactory(new PropertyValueFactory<>("floor"));
        colPrice .setCellValueFactory(data ->
            new SimpleStringProperty(String.format("₹ %,.0f", data.getValue().getPricePerNight())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Colour-code status column
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
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

    private void loadRoomTypes() {
        List<RoomType> types = repo.findAllTypes();
        roomTypeCombo.setItems(FXCollections.observableArrayList(types));
    }

    private void loadRooms() {
        List<Room> rooms = repo.findAll();
        roomTable.setItems(FXCollections.observableArrayList(rooms));
        totalLabel    .setText("Total: " + rooms.size());
        long available = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        availableLabel.setText("Available: " + available);
    }

    @FXML
    private void handleAddRoom() {
        String roomNoStr = roomNumberField.getText().trim();
        String floorStr  = floorField.getText().trim();
        RoomType selected = roomTypeCombo.getValue();

        if (roomNoStr.isEmpty() || floorStr.isEmpty() || selected == null) {
            showError("Please fill in all fields.");
            return;
        }

        int roomNo, floor;
        try {
            roomNo = Integer.parseInt(roomNoStr);
            floor  = Integer.parseInt(floorStr);
        } catch (NumberFormatException e) {
            showError("Room number and floor must be numbers.");
            return;
        }

        boolean ok = repo.save(roomNo, selected.getRoomTypeId(), floor);
        if (ok) {
            clearForm();
            loadRooms();
        } else {
            showError("Failed to add room. Room number may already exist.");
        }
    }

    @FXML
    private void handleChangeStatus() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a room first."); return; }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(),
                "AVAILABLE", "OCCUPIED", "MAINTENANCE", "RESERVED");
        dialog.setTitle("Change Room Status");
        dialog.setHeaderText("Room " + selected.getRoomNumber());
        dialog.setContentText("New status:");
        styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(status -> {
            repo.updateStatus(selected.getRoomId(), status);
            loadRooms();
        });
    }

    @FXML
    private void handleDeleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a room first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Room " + selected.getRoomNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        styleDialog(confirm);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                repo.delete(selected.getRoomId());
                loadRooms();
            }
        });
    }

    @FXML
    private void handleRefresh() { loadRooms(); }

    private void clearForm() {
        roomNumberField.clear();
        floorField.clear();
        roomTypeCombo.setValue(null);
        formErrorLabel.setVisible(false);
    }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
    }

    private void styleDialog(Dialog<?> d) {
        d.getDialogPane().getStylesheets()
         .add(getClass().getResource("/css/main.css").toExternalForm());
        d.getDialogPane().getStyleClass().add("login-card");
    }
}
