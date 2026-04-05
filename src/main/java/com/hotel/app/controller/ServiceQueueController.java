package com.hotel.app.controller;

import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.service.ServiceRequestService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    /** All requests loaded from DB — filter runs against this list without a new DB call. */
    private ObservableList<ServiceRequest> allRequests = FXCollections.observableArrayList();

    /** Active filter: "ALL", "PENDING", or "IN_PROGRESS" */
    private String activeFilter = "ALL";

    // ── Initialise ────────────────────────────────────────────

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

        // Colour-coded status
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

        // Action buttons: [In Progress] [Served] [Cancel]
        colQActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnProgress = new Button("In Progress");
            private final Button btnServed   = new Button("Served");
            private final Button btnCancel   = new Button("Cancel");
            private final HBox   box         = new HBox(6, btnProgress, btnServed, btnCancel);

            {
                btnProgress.getStyleClass().add("logout-btn");
                btnServed  .getStyleClass().add("primary-btn");
                btnCancel  .getStyleClass().add("logout-btn");

                btnProgress.setPrefWidth(88);
                btnServed  .setPrefWidth(68);
                btnCancel  .setPrefWidth(60);

                btnProgress.setOnAction(e -> handleMarkInProgress(getCurrentRequest()));
                btnServed  .setOnAction(e -> handleMarkServed(getCurrentRequest()));
                btnCancel  .setOnAction(e -> handleMarkCancelled(getCurrentRequest()));
            }

            private ServiceRequest getCurrentRequest() {
                return getTableView().getItems().get(getIndex());
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ServiceRequest r = getCurrentRequest();

                // Show only the buttons that make sense for the current status
                boolean isPending    = "PENDING".equals(r.getStatus());
                boolean isInProgress = "IN_PROGRESS".equals(r.getStatus());

                btnProgress.setVisible(isPending);
                btnProgress.setManaged(isPending);
                btnServed  .setVisible(isPending || isInProgress);
                btnServed  .setManaged(isPending || isInProgress);
                btnCancel  .setVisible(isPending || isInProgress);
                btnCancel  .setManaged(isPending || isInProgress);

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
            default            -> allRequests;   // ALL
        };
        queueTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ── Action handlers ───────────────────────────────────────

    private void handleMarkInProgress(ServiceRequest r) {
        long staffId = SessionManager.getInstance().getCurrentUser().getUserId();
        boolean ok = service.markInProgress(r.getRequestId(), staffId);
        if (ok) {
            showFeedback("Marked in-progress: " + r.getServiceName()
                    + " for Room " + r.getRoomNumber());
            loadRequests();
        } else {
            showFeedback("Could not update status — request may have changed.");
        }
    }

    private void handleMarkServed(ServiceRequest r) {
        // Confirmation dialog before billing the guest
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Service");
        confirm.setHeaderText("Mark as served?");
        confirm.setContentText(
                "Service: " + r.getServiceName() + "\n" +
                        "Guest:   " + r.getGuestName() + "  (Room " + r.getRoomNumber() + ")\n" +
                        "Charge:  " + r.getChargeDisplay() + "\n\n" +
                        "This will add the charge to the guest's bill.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                long staffId = SessionManager.getInstance().getCurrentUser().getUserId();
                boolean ok = service.markServed(r.getRequestId(), staffId);
                if (ok) {
                    showFeedback("Served: " + r.getServiceName()
                            + " — " + r.getChargeDisplay()
                            + " added to Room " + r.getRoomNumber() + "'s bill.");
                    loadRequests();
                } else {
                    showFeedback("Error marking served. Please try again.");
                }
            }
        });
    }

    private void handleMarkCancelled(ServiceRequest r) {
        boolean ok = service.markCancelled(r.getRequestId());
        if (ok) {
            showFeedback("Cancelled: " + r.getServiceName()
                    + " for Room " + r.getRoomNumber() + ". No charge applied.");
            loadRequests();
        } else {
            showFeedback("Could not cancel — request may already be completed.");
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

    // ── Helpers ───────────────────────────────────────────────

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
