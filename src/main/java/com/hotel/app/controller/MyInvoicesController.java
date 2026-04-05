package com.hotel.app.controller;

import com.hotel.app.entity.Invoice;
import com.hotel.app.service.BillingService;
import com.hotel.app.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyInvoicesController {
    @FXML private TableView<Invoice> invoicesTable;
    @FXML private TableColumn<Invoice, Long> colInvId;
    @FXML private TableColumn<Invoice, Integer> colRoom;
    @FXML private TableColumn<Invoice, String> colCheckIn, colCheckOut, colSubtotal, colTax, colTotal, colDate;
    @FXML private Label selectedInvoiceLabel, paymentsLabel;

    private final BillingService billingService = new BillingService();

    @FXML
    public void initialize() {
        setupColumns();

        invoicesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) updateInvoiceDetails(val);
        });

        loadMyInvoices();
    }

    private void setupColumns() {
        colInvId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%,.2f", c.getValue().getTotalAmount())));
        colTax.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%,.2f", c.getValue().getTax())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%,.2f", c.getValue().getGrandTotal())));
        colDate.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));
    }

    private void loadMyInvoices() {
        long cid = SessionManager.getInstance().getCustomerId();
        invoicesTable.setItems(FXCollections.observableArrayList(billingService.getInvoicesForCustomer(cid)));
    }

    private void updateInvoiceDetails(Invoice inv) {
        selectedInvoiceLabel.setText("Invoice breakdown for Booking #" + inv.getBookingId());
        var payments = billingService.getPaymentsForBooking(inv.getBookingId());

        StringBuilder details = new StringBuilder("Payments history:\n");
        if (payments.isEmpty()) details.append("- No payments recorded yet.");
        else payments.forEach(p -> details.append(String.format("- %s: ₹%,.2f (%s)\n", p.getMethodName(), p.getAmount(), p.getStatus())));

        paymentsLabel.setText(details.toString());
    }
}