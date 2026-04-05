package com.hotel.app.controller;

import com.hotel.app.entity.Invoice;
import com.hotel.app.entity.ServiceCharge;
import com.hotel.app.service.BillingService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class MyInvoicesController {

    // Invoice list (Left Side)
    @FXML private TableView<Invoice> invoicesTable;
    @FXML private TableColumn<Invoice, Long> colInvId;
    @FXML private TableColumn<Invoice, Integer> colRoom;
    @FXML private TableColumn<Invoice, String> colCheckIn;
    @FXML private TableColumn<Invoice, String> colCheckOut;
    @FXML private TableColumn<Invoice, String> colTotal;
    @FXML private TableColumn<Invoice, String> colDate;
    @FXML private TableColumn<Invoice, String> colPayStatus;

    // Bill detail panel (Right side)
    @FXML private VBox detailPanel; 
    @FXML private Label detailBookingId;
    @FXML private Label detailGuestName;
    @FXML private Label detailRoom;
    @FXML private Label detailCheckIn;
    @FXML private Label detailCheckOut;
    @FXML private Label detailNights;
    @FXML private Label detailRoomCharge;
    @FXML private VBox serviceChargesBox;
    @FXML private Label detailSubtotal;
    @FXML private Label detailTax;
    @FXML private Label detailGrandTotal;
    @FXML private Label detailAmountPaid;
    @FXML private Label detailBalanceDue;
    @FXML private VBox paymentHistoryBox;

    // Payment form
    @FXML private VBox paymentFormBox;
    @FXML private ComboBox<String> payMethodCombo;
    @FXML private TextField payAmountField;
    @FXML private Button payButton;
    @FXML private Label payFeedbackLabel;

    private final BillingService billingService = new BillingService();
    private Invoice selectedInvoice;

    @FXML
    public void initialize() {
        setupColumns();

        // Initial UI State
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);

        // Selection Listener
        invoicesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) showInvoiceDetail(val);
        });

        // Populate Payment Methods
        List<String[]> methods = billingService.getPaymentMethods();
        payMethodCombo.setItems(FXCollections.observableArrayList(
            methods.stream().map(m -> m[1]).collect(Collectors.toList())
        ));

        loadMyInvoices();
    }

    private void setupColumns() {
        colInvId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%,.2f", c.getValue().getGrandTotal())));
        colDate.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));

        // Status Column with logic and styling
        colPayStatus.setCellValueFactory(data -> {
            double paid = billingService.getTotalPaidForBooking(data.getValue().getBookingId());
            boolean settled = paid >= data.getValue().getGrandTotal();
            return new SimpleStringProperty(settled ? "PAID" : "OUTSTANDING");
        });

        colPayStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle("PAID".equals(s)
                    ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                    : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
    }

    private void loadMyInvoices() {
        long cid = SessionManager.getInstance().getCustomerId();
        List<Invoice> invoices = billingService.getInvoicesForCustomer(cid);
        invoicesTable.setItems(FXCollections.observableArrayList(invoices));
    }

    private void showInvoiceDetail(Invoice inv) {
        this.selectedInvoice = inv;
        
        // 1. Show the panel
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        // 2. Header info
        detailBookingId.setText("Booking #" + inv.getBookingId());
        detailGuestName.setText(inv.getCustomerName());
        detailRoom.setText("Room " + inv.getRoomNumber());
        detailCheckIn.setText(inv.getCheckInDate().toString());
        detailCheckOut.setText(inv.getCheckOutDate().toString());
        long nights = ChronoUnit.DAYS.between(inv.getCheckInDate(), inv.getCheckOutDate());
        detailNights.setText(nights + " night(s)");

        // 3. Room charge calculation
        List<ServiceCharge> charges = billingService.getServiceChargesForBooking(inv.getBookingId());
        double serviceTotal = charges.stream().mapToDouble(ServiceCharge::getPrice).sum();
        double roomCharge = inv.getTotalAmount() - serviceTotal;
        detailRoomCharge.setText(String.format("₹%,.2f", roomCharge));

        // 4. Service charges - Dynamic Rows
        serviceChargesBox.getChildren().clear();
        if (charges.isEmpty()) {
            Label none = new Label("No additional services");
            none.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            serviceChargesBox.getChildren().add(none);
        } else {
            for (ServiceCharge sc : charges) {
                HBox row = new HBox();
                row.setSpacing(10);
                Label name = new Label(sc.getServiceName() + " × " + sc.getQuantity());
                name.setStyle("-fx-text-fill: #e2e8f0;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label price = new Label(String.format("₹%,.2f", sc.getPrice()));
                price.setStyle("-fx-text-fill: #e2e8f0;");
                row.getChildren().addAll(name, spacer, price);
                serviceChargesBox.getChildren().add(row);
            }
        }

        // 5. Totals
        detailSubtotal.setText(String.format("₹%,.2f", inv.getTotalAmount()));
        detailTax.setText(String.format("₹%,.2f", inv.getTax()));
        detailGrandTotal.setText(String.format("₹%,.2f", inv.getGrandTotal()));

        // 6. Payments & Balance
        double paid = billingService.getTotalPaidForBooking(inv.getBookingId());
        double balanceDue = inv.getGrandTotal() - paid;
        detailAmountPaid.setText(String.format("₹%,.2f", paid));
        detailBalanceDue.setText(String.format("₹%,.2f", Math.max(0, balanceDue)));
        
        detailBalanceDue.setStyle(balanceDue > 0.01 
            ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;" 
            : "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        // 7. Payment history
        paymentHistoryBox.getChildren().clear();
        var payments = billingService.getPaymentsForBooking(inv.getBookingId());
        if (payments.isEmpty()) {
            Label none = new Label("No payments recorded yet.");
            none.setStyle("-fx-text-fill: #95a5a6;");
            paymentHistoryBox.getChildren().add(none);
        } else {
            for (var p : payments) {
                HBox row = new HBox();
                row.setSpacing(10);
                Label info = new Label(p.getPaymentDate() + "  " + p.getMethodName());
                info.setStyle("-fx-text-fill: #94a3b8;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label amt = new Label(String.format("₹%,.2f", p.getAmount()));
                amt.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                row.getChildren().addAll(info, spacer, amt);
                paymentHistoryBox.getChildren().add(row);
            }
        }

        // 8. Payment form visibility
        boolean settled = balanceDue <= 0.01; // Using threshold for double comparison
        paymentFormBox.setVisible(!settled);
        paymentFormBox.setManaged(!settled);
        if (!settled) {
            payAmountField.setText(String.format("%.2f", balanceDue));
        }
        payFeedbackLabel.setVisible(false);
    }

    @FXML
    private void handlePay() {
        if (selectedInvoice == null) return;

        String methodStr = payMethodCombo.getValue();
        String amtStr = payAmountField.getText().trim();

        if (methodStr == null || amtStr.isEmpty()) {
            showPayFeedback("Please select a method and enter amount.", true);
            return;
        }

        double amount;
        try { 
            amount = Double.parseDouble(amtStr); 
        } catch (NumberFormatException e) {
            showPayFeedback("Invalid amount.", true);
            return;
        }

        // Map method name back to ID
        long methodId = billingService.getPaymentMethods().stream()
            .filter(m -> m[1].equals(methodStr))
            .mapToLong(m -> Long.parseLong(m[0]))
            .findFirst()
            .orElse(-1);

        if (methodId == -1) {
            showPayFeedback("Invalid payment method.", true);
            return;
        }

        var result = billingService.recordPayment(selectedInvoice.getBookingId(), methodId, amount);
        
        if (result.isSuccess()) {
            showPayFeedback("Payment recorded successfully!", false);
            showInvoiceDetail(selectedInvoice); // Refresh right panel
            loadMyInvoices();                  // Refresh table status
        } else {
            showPayFeedback(result.getErrorMessage(), true);
        }
    }

    private void showPayFeedback(String msg, boolean error) {
        payFeedbackLabel.setText(msg);
        payFeedbackLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        payFeedbackLabel.setVisible(true);
        payFeedbackLabel.setManaged(true);
    }
}