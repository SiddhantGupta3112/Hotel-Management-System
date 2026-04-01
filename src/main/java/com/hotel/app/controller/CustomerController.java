package com.hotel.app.controller;

import com.hotel.app.entity.Customer;
import com.hotel.app.repository.CustomerRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CustomerController {

    @FXML private TableView<Customer>            customerTable;
    @FXML private TableColumn<Customer, Long>    colId;
    @FXML private TableColumn<Customer, String>  colName;
    @FXML private TableColumn<Customer, String>  colEmail;
    @FXML private TableColumn<Customer, String>  colNationality;
    @FXML private TableColumn<Customer, String>  colIdProof;
    @FXML private TableColumn<Customer, Integer> colPoints;

    @FXML private TextField searchField;

    // Add-customer form
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField idProofField;
    @FXML private TextField nationalityField;
    @FXML private Label     formErrorLabel;
    @FXML private Label     totalLabel;

    private final CustomerRepository repo = new CustomerRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadCustomers();
    }

    private void setupColumns() {
        colId         .setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName       .setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail      .setCellValueFactory(new PropertyValueFactory<>("email"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colIdProof    .setCellValueFactory(new PropertyValueFactory<>("idProof"));
        colPoints     .setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCustomers() {
        List<Customer> list = repo.findAll();
        customerTable.setItems(FXCollections.observableArrayList(list));
        totalLabel.setText("Total customers: " + list.size());
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadCustomers(); return; }
        List<Customer> results = repo.search(kw);
        customerTable.setItems(FXCollections.observableArrayList(results));
        totalLabel.setText("Results: " + results.size());
    }

    @FXML
    private void handleAddCustomer() {
        String name        = nameField.getText().trim();
        String email       = emailField.getText().trim();
        String phone       = phoneField.getText().trim();
        String address     = addressField.getText().trim();
        String idProof     = idProofField.getText().trim();
        String nationality = nationalityField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Name, email and phone are required.");
            return;
        }

        boolean ok = repo.save(name, email, phone, address, idProof, nationality);
        if (ok) {
            clearForm();
            loadCustomers();
        } else {
            showError("Failed to add customer. Email may already exist.");
        }
    }

    @FXML
    private void handleRefresh() { loadCustomers(); }

    private void clearForm() {
        nameField.clear(); emailField.clear(); phoneField.clear();
        addressField.clear(); idProofField.clear(); nationalityField.clear();
        formErrorLabel.setVisible(false);
    }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
    }
}
