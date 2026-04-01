package com.hotel.app.controller;

import com.hotel.app.entity.Invoice;
import com.hotel.app.entity.Payment;
import com.hotel.app.repository.BookingRepository;
import com.hotel.app.repository.InvoiceRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class BillingController {

    @FXML private TableView<Invoice>              invoiceTable;
    @FXML private TableColumn<Invoice, Long>      colInvId;
    @FXML private TableColumn<Invoice, Long>      colBookingId;
    @FXML private TableColumn<Invoice, String>    colGuest;
    @FXML private TableColumn<Invoice, Integer>   colRoom;
    @FXML private TableColumn<Invoice, String>    colSubtotal;
    @FXML private TableColumn<Invoice, String>    colTax;
    @FXML private TableColumn<Invoice, String>    colTotal;
    @FXML private TableColumn<Invoice, String>    colDate;

    @FXML private Label     selectedInfoLabel;
    @FXML private ComboBox<String> payMethodCombo;
    @FXML private TextField payAmountField;
    @FXML private Label     formErrorLabel;
    @FXML private Label     totalInvoicesLabel;

    // Payment method IDs match seed order (1=Cash,2=Credit,3=Debit,4=UPI,5=NetBanking)
    private static final List<String> METHODS =
            List.of("Cash", "Credit Card", "Debit Card", "UPI", "Net Banking");

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final BookingRepository bookingRepo  = new BookingRepository();

    @FXML
    public void initialize() {
        setupColumns();
        payMethodCombo.setItems(FXCollections.observableArrayList(METHODS));
        payMethodCombo.setValue("Cash");
        loadInvoices();

        // When a row is selected, fill in the payment amount automatically
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, inv) -> {
                    if (inv != null) {
                        selectedInfoLabel.setText(
                                "Invoice #" + inv.getInvoiceId() +
                                " — " + inv.getCustomerName() +
                                " | Grand total: ₹ " + String.format("%,.2f", inv.getGrandTotal()));
                        payAmountField.setText(String.format("%.2f", inv.getGrandTotal()));
                    }
                });
    }

    private void setupColumns() {
        colInvId    .setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colGuest    .setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRoom     .setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colSubtotal .setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₹ %,.2f", data.getValue().getTotalAmount())));
        colTax      .setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₹ %,.2f", data.getValue().getTax())));
        colTotal    .setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₹ %,.2f", data.getValue().getGrandTotal())));
        colDate     .setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getGeneratedDate() != null
                        ? data.getValue().getGeneratedDate().toString() : "-"));
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadInvoices() {
        List<Invoice> list = invoiceRepo.findAll();
        invoiceTable.setItems(FXCollections.observableArrayList(list));
        totalInvoicesLabel.setText("Total invoices: " + list.size());
    }

    /** Generate invoice for the selected booking (entered by booking ID). */
    @FXML
    private void handleGenerateInvoice() {
        String bookingIdStr = payAmountField.getText().trim(); // reuse field for booking ID
        // Actually we use a separate field — see FXML. Use selectedInfoLabel booking id.
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        // If no invoice selected, ask user to pick a booking from Bookings screen first
        showError("Select a booking from the Bookings tab and click 'Generate Invoice' there, " +
                  "or select an existing invoice below to record a payment.");
    }

    /** Record payment for the selected invoice. */
    @FXML
    private void handleRecordPayment() {
        Invoice inv = invoiceTable.getSelectionModel().getSelectedItem();
        if (inv == null) { showError("Select an invoice first."); return; }

        String amtStr = payAmountField.getText().trim();
        if (amtStr.isEmpty()) { showError("Enter payment amount."); return; }

        double amount;
        try { amount = Double.parseDouble(amtStr); }
        catch (NumberFormatException e) { showError("Invalid amount."); return; }

        int methodIdx = METHODS.indexOf(payMethodCombo.getValue()) + 1; // DB IDs start at 1
        boolean ok = invoiceRepo.recordPayment(inv.getBookingId(), methodIdx, amount);
        if (ok) {
            formErrorLabel.setStyle("-fx-text-fill: #22c55e;");
            showError("Payment recorded successfully!");
            loadInvoices();
        } else {
            formErrorLabel.setStyle("");
            showError("Failed to record payment.");
        }
    }

    @FXML
    private void handleRefresh() { loadInvoices(); }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
    }
}
