package com.hotel.app.controller;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.Service;
import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.service.ServiceRequestService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ServiceController {

    // ── Booking status section ────────────────────────────────
    @FXML private VBox  bookingStatusBox;
    @FXML private Label bookingRoomLabel;
    @FXML private Label bookingCheckInLabel;
    @FXML private Label bookingCheckOutLabel;
    @FXML private Label bookingStatusLabel;
    @FXML private VBox  noBookingBox;
    @FXML private VBox  serviceSection;

    // ── Service catalog table ─────────────────────────────────
    @FXML private TableView<Service>           serviceTable;
    @FXML private TableColumn<Service, String> colServiceName;
    @FXML private TableColumn<Service, String> colServiceCategory;
    @FXML private TableColumn<Service, String> colServiceDesc;
    @FXML private TableColumn<Service, String> colServicePrice;
    @FXML private TableColumn<Service, Void>   colServiceAction;

    // ── Request form ──────────────────────────────────────────
    @FXML private TextField quantityField;
    @FXML private TextField notesField;
    @FXML private Label     requestErrorLabel;
    @FXML private Label     requestSuccessLabel;

    // ── My requests table ─────────────────────────────────────
    @FXML private TableView<ServiceRequest>           myRequestsTable;
    @FXML private TableColumn<ServiceRequest, String> colReqService;
    @FXML private TableColumn<ServiceRequest, Number> colReqQty;
    @FXML private TableColumn<ServiceRequest, String> colReqNotes;
    @FXML private TableColumn<ServiceRequest, String> colReqStatus;
    @FXML private TableColumn<ServiceRequest, String> colReqTime;
    @FXML private TableColumn<ServiceRequest, Void>   colReqCancel;

    private final ServiceRequestService serviceRequestService = new ServiceRequestService();
    private Booking activeBooking;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    // ── Initialise ────────────────────────────────────────────

    @FXML
    public void initialize() {
        long userId = SessionManager.getInstance().getCurrentUser().getUserId();
        Optional<Booking> opt = serviceRequestService.getActiveBooking(userId);

        if (opt.isPresent()) {
            activeBooking = opt.get();
            populateBookingCard(activeBooking);
            reveal(serviceSection);
            setupServiceTable();
            setupMyRequestsTable();
            loadServices();
            loadMyRequests();
        } else {
            hide(bookingStatusBox);
            reveal(noBookingBox);
        }
    }

    // ── Setup ─────────────────────────────────────────────────

    private void populateBookingCard(Booking b) {
        bookingRoomLabel.setText("Room " + b.getRoomNumber());
        bookingCheckInLabel.setText(b.getCheckInDate() != null
                ? b.getCheckInDate().toString() : "—");
        bookingCheckOutLabel.setText(b.getCheckOutDate() != null
                ? b.getCheckOutDate().toString() : "—");
        bookingStatusLabel.setText(b.getBookingStatus());
        // Green for checked-in, blue for confirmed
        bookingStatusLabel.setStyle("CHECKED_IN".equals(b.getBookingStatus())
                ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                : "-fx-text-fill: #60a5fa; -fx-font-weight: bold;");
    }

    private void setupServiceTable() {
        colServiceName    .setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colServiceCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colServiceDesc    .setCellValueFactory(new PropertyValueFactory<>("description"));

        colServicePrice.setCellValueFactory(data -> {
            double p = data.getValue().getPrice();
            return new SimpleStringProperty(p == 0 ? "Free" : String.format("₹ %.0f", p));
        });

        // "Request" button column — clicking sends the selected service + form values
        colServiceAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Request");
            {
                btn.getStyleClass().add("primary-btn");
                btn.setPrefWidth(78);
                btn.setOnAction(e -> {
                    Service s = getTableView().getItems().get(getIndex());
                    handleRequest(s);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupMyRequestsTable() {
        colReqService.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        colReqQty    .setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReqNotes  .setCellValueFactory(data -> {
            String n = data.getValue().getNotes();
            return new SimpleStringProperty(n == null ? "—" : n);
        });
        colReqStatus .setCellValueFactory(new PropertyValueFactory<>("status"));
        colReqTime   .setCellValueFactory(data -> {
            var r = data.getValue();
            return new SimpleStringProperty(r.getRequestedAt() != null
                    ? r.getRequestedAt().format(DT_FMT) : "—");
        });

        // Colour-code status text
        colReqStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(switch (s) {
                    case "PENDING"     -> "-fx-text-fill: #EF9F27; -fx-font-weight: bold;";
                    case "IN_PROGRESS" -> "-fx-text-fill: #60a5fa; -fx-font-weight: bold;";
                    case "SERVED"      -> "-fx-text-fill: #22c55e; -fx-font-weight: bold;";
                    case "CANCELLED"   -> "-fx-text-fill: #e94560; -fx-font-weight: bold;";
                    default            -> "-fx-text-fill: #e2e8f0;";
                });
            }
        });

        // Cancel button — only shown for PENDING and IN_PROGRESS rows
        colReqCancel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cancel");
            {
                btn.getStyleClass().add("logout-btn");
                btn.setPrefWidth(72);
                btn.setOnAction(e -> {
                    ServiceRequest r = getTableView().getItems().get(getIndex());
                    handleCancelMyRequest(r);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ServiceRequest r = getTableView().getItems().get(getIndex());
                boolean cancellable = "PENDING".equals(r.getStatus())
                        || "IN_PROGRESS".equals(r.getStatus());
                setGraphic(cancellable ? btn : null);
            }
        });

        myRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Data loading ──────────────────────────────────────────

    private void loadServices() {
        List<Service> services = serviceRequestService.getAllServices();
        serviceTable.setItems(FXCollections.observableArrayList(services));
    }

    private void loadMyRequests() {
        if (activeBooking == null) return;
        List<ServiceRequest> requests =
                serviceRequestService.getRequestsForBooking(activeBooking.getBookingId());
        myRequestsTable.setItems(FXCollections.observableArrayList(requests));
    }

    // ── Actions ───────────────────────────────────────────────

    /** Called when the customer clicks Request next to a service row. */
    private void handleRequest(Service svc) {
        clearFeedback();

        // Parse quantity
        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
            if (qty < 1) throw new NumberFormatException("must be positive");
        } catch (NumberFormatException e) {
            showError("Please enter a valid quantity (1 or more).");
            return;
        }

        String notes = notesField.getText().trim();

        try {
            boolean ok = serviceRequestService.placeRequest(
                    activeBooking.getBookingId(),
                    activeBooking.getBookingStatus(),
                    svc.getServiceId(),
                    qty,
                    notes);

            if (ok) {
                showSuccess("'" + svc.getServiceName() +
                        "' requested. Our staff will attend to you shortly.");
                notesField.clear();
                quantityField.setText("1");
                loadMyRequests();
            } else {
                showError("Could not submit your request. Please try again.");
            }

        } catch (IllegalStateException ex) {
            // Ineligible booking status — show the service's message
            showError(ex.getMessage());
        }
    }

    /** Customer cancels one of their own pending requests. */
    private void handleCancelMyRequest(ServiceRequest r) {
        boolean ok = serviceRequestService.markCancelled(r.getRequestId());
        if (ok) {
            showSuccess("Request for '" + r.getServiceName() + "' has been cancelled.");
            loadMyRequests();
        } else {
            showError("Could not cancel — the request may already be served.");
        }
    }

    @FXML
    private void handleRefresh() {
        clearFeedback();
        if (activeBooking != null) {
            loadServices();
            loadMyRequests();
        }
    }

    // ── Feedback helpers ──────────────────────────────────────

    private void showError(String msg) {
        requestErrorLabel.setText(msg);
        reveal(requestErrorLabel);
        hide(requestSuccessLabel);
    }

    private void showSuccess(String msg) {
        requestSuccessLabel.setText("✓  " + msg);
        reveal(requestSuccessLabel);
        hide(requestErrorLabel);
    }

    private void clearFeedback() {
        hide(requestErrorLabel);
        hide(requestSuccessLabel);
    }

    // ── Visibility helpers ────────────────────────────────────

    private void reveal(javafx.scene.Node node) {
        node.setVisible(true);
        node.setManaged(true);
    }

    private void hide(javafx.scene.Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }
}
