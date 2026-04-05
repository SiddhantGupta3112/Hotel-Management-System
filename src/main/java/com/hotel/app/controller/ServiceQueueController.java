package com.hotel.app.controller;

import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.service.ServiceRequestService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceQueueController {

    // ── Header ────────────────────────────────────────────────
    @FXML private Label pendingCountLabel;

    // ── Filter buttons ────────────────────────────────────────
    @FXML private Button btnAll;
    @FXML private Button btnPending;
    @FXML private Button btnInProgress;

    // ── Queue table ───────────────────────────────────────────
    @FXML private TableView<ServiceRequest>           queueTable;
    @FXML private TableColumn<ServiceRequest, Number> colQRoom;
    @FXML private TableColumn<ServiceRequest, String> colQGuest;
    @FXML private TableColumn<ServiceRequest, String> colQService;
    @FXML private TableColumn<ServiceRequest, Number> colQQty;
    @FXML private TableColumn<ServiceRequest, String> colQNotes;
    @FXML private TableColumn<ServiceRequest, String> colQStatus;
    @FXML private TableColumn<ServiceRequest, String> colQTime;
    @FXML private TableColumn<ServiceRequest, String> colQCharge;
    @FXML private TableColumn<ServiceRequest, Void>   colQActions;

    // ── Feedback ──────────────────────────────────────────────
    @FXML private Label feedbackLabel;

    private final ServiceRequestService service = new ServiceRequestService();
    private ObservableList<ServiceRequest> allRequests = FXCollections.observableArrayList();
    private String activeFilter = "ALL";

    // ── Initialize ────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        loadRequests();
    }

    // ── Column setup ──────────────────────────────────────────

    private void setupColumns() {
        colQRoom   .setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colQGuest  .setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colQService.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colQQty    .setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colQNotes.setCellValueFactory(data -> {
            String n = data.getValue().getNotes();
            return new SimpleStringProperty(n == null || n.isBlank() ? "—" : n);
        });

        colQCharge.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getChargeDisplay()));

        colQTime.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRequestedAtFormatted()));

        // Status styling
        colQStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(switch (s) {
                    case "PENDING"     -> "-fx-text-fill: #EF9F27; -fx-font-weight: bold;";
                    case "IN_PROGRESS" -> "-fx-text-fill: #60a5fa; -fx-font-weight: bold;";
                    default            -> "-fx-text-fill: #e2e8f0;";
                });
            }
        });

        // ── FIXED ACTION BUTTONS ──
        colQActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnProgress = new Button("Start");
            private final Button btnServed   = new Button("Serve");
            private final Button btnCancel   = new Button("Cancel");
            private final HBox box = new HBox(6, btnProgress, btnServed, btnCancel);

            {
                // Applying the small 'action-btn' styles from main.css
                btnProgress.getStyleClass().addAll("action-btn", "btn-start");
                btnServed.getStyleClass().addAll("action-btn", "btn-complete");
                btnCancel.getStyleClass().addAll("action-btn", "btn-cancel");

                btnProgress.setPrefWidth(65);
                btnServed.setPrefWidth(65);
                btnCancel.setPrefWidth(65);

                box.setAlignment(Pos.CENTER);

                btnProgress.setOnAction(e -> handleMarkInProgress(getCurrentRequest()));
                btnServed.setOnAction(e -> handleMarkServed(getCurrentRequest()));
                btnCancel.setOnAction(e -> handleMarkCancelled(getCurrentRequest()));
            }

            private ServiceRequest getCurrentRequest() {
                if (getTableRow() == null) return null;
                return getTableRow().getItem();
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                ServiceRequest r = getTableRow().getItem();
                boolean isPending    = "PENDING".equals(r.getStatus());
                boolean isInProgress = "IN_PROGRESS".equals(r.getStatus());

                btnProgress.setVisible(isPending);
                btnProgress.setManaged(isPending);

                btnServed.setVisible(isPending || isInProgress);
                btnServed.setManaged(isPending || isInProgress);

                btnCancel.setVisible(isPending || isInProgress);
                btnCancel.setManaged(isPending || isInProgress);

                setGraphic(box);
            }
        });

        queueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Data loading ──────────────────────────────────────────

    private void loadRequests() {
        allRequests = FXCollections.observableArrayList(service.getPendingRequests());
        applyFilter();
        long pending = allRequests.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        pendingCountLabel.setText(pending + " pending  |  " + allRequests.size() + " total active");
    }

    private void applyFilter() {
        List<ServiceRequest> filtered = switch (activeFilter) {
            case "PENDING"     -> allRequests.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .collect(Collectors.toList());
            case "IN_PROGRESS" -> allRequests.stream()
                    .filter(r -> "IN_PROGRESS".equals(r.getStatus()))
                    .collect(Collectors.toList());
            default            -> allRequests;
        };
        queueTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ── Action handlers ───────────────────────────────────────

    private void handleMarkInProgress(ServiceRequest r) {
        if (r == null) return;
        long staffId = SessionManager.getInstance().getCurrentUser().getUserId();
        boolean ok = service.markInProgress(r.getRequestId(), staffId);
        if (ok) {
            showFeedback("Marked in-progress: " + r.getServiceName() + " for Room " + r.getRoomNumber());
            loadRequests();
        } else {
            showFeedback("Could not update status.");
        }
    }

    private void handleMarkServed(ServiceRequest r) {
        if (r == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Service");
        confirm.setHeaderText("Mark as served?");
        confirm.setContentText("Service: " + r.getServiceName() + "\nCharge: " + r.getChargeDisplay());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                long staffId = SessionManager.getInstance().getCurrentUser().getUserId();
                if (service.markServed(r.getRequestId(), staffId)) {
                    showFeedback("Served: " + r.getServiceName() + " billed to Room " + r.getRoomNumber());
                    loadRequests();
                } else {
                    showFeedback("Error marking served.");
                }
            }
        });
    }

    private void handleMarkCancelled(ServiceRequest r) {
        if (r == null) return;
        if (service.markCancelled(r.getRequestId())) {
            showFeedback("Cancelled: " + r.getServiceName());
            loadRequests();
        } else {
            showFeedback("Could not cancel request.");
        }
    }

    // ── Filter handlers ───────────────────────────────────────

    @FXML private void handleFilterAll() {
        activeFilter = "ALL";
        setActiveFilter(btnAll);
        applyFilter();
    }

    @FXML private void handleFilterPending() {
        activeFilter = "PENDING";
        setActiveFilter(btnPending);
        applyFilter();
    }

    @FXML private void handleFilterInProgress() {
        activeFilter = "IN_PROGRESS";
        setActiveFilter(btnInProgress);
        applyFilter();
    }

    @FXML private void handleRefresh() {
        clearFeedback();
        loadRequests();
    }

    private void setActiveFilter(Button active) {
        for (Button b : new Button[]{btnAll, btnPending, btnInProgress}) {
            b.getStyleClass().removeAll("primary-btn", "logout-btn");
            b.getStyleClass().add(b == active ? "primary-btn" : "logout-btn");
        }
    }

    private void showFeedback(String msg) {
        feedbackLabel.setText(msg);
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    private void clearFeedback() {
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }
}