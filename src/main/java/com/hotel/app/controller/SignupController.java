package com.hotel.app.controller;

import com.hotel.app.entity.User;
import com.hotel.app.repository.CustomerRepository;
import com.hotel.app.repository.ManagerRepository;
import com.hotel.app.repository.StaffRepository;
import com.hotel.app.repository.UserRepository;
import com.hotel.app.service.AuthService;
import com.hotel.app.service.BookingService;
import com.hotel.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField countryCodeField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    // Updated to 4-arg constructor to match the merged AuthService
    private final AuthService authService = new AuthService(
            new UserRepository(),
            new CustomerRepository(),
            new StaffRepository(),
            new ManagerRepository()
    );

    @FXML
    private void handleSignup() {
        String name        = nameField.getText().trim();
        String email       = emailField.getText().trim().toLowerCase();
        String countryCode = countryCodeField.getText().trim();
        String phone       = phoneNumberField.getText().trim();
        String password    = passwordField.getText();
        String confirm     = confirmPasswordField.getText();

        if (!validateInput(name, email, password, confirm, countryCode, phone)) {
            return;
        }

        // registerCustomer now also creates the CUSTOMERS profile row automatically
        boolean registered = authService.registerCustomer(email, password, name, countryCode, phone);

        if (!registered) {
            showError("Error signing up. Please try again.");
            return;
        }

        // Auto-login after successful registration
        Optional<User> userOptional = authService.login(email, password);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            List<String> roles = authService.getRolesForUser(user.getUserId());
            SessionManager.getInstance().login(user, roles);

            // Cache customerId immediately — the CUSTOMERS row was just created
            // by registerCustomer() so this lookup will always succeed
            BookingService bookingService = new BookingService();
            bookingService.getCustomerByUserId(user.getUserId())
                    .ifPresent(c -> SessionManager.getInstance().setCustomerId(c.getCustomerId()));

            navigateTo("/fxml/CustomerDashboard.fxml");

        } else {
            showError("Registration succeeded but auto-login failed. Please log in manually.");
        }
    }

    private boolean validateInput(String name, String email, String pass,
                                  String confirm, String countryCode, String phone) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()
                || countryCode.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all required fields.");
            return false;
        }

        Pattern pattern = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            showError("Invalid email format.");
            return false;
        }

        if (pass.length() < 8) {
            showError("Password must be at least 8 characters.");
            return false;
        }

        if (!pass.equals(confirm)) {
            showError("Passwords do not match.");
            return false;
        }

        if (authService.emailExists(email)) {
            showError("Email is already in use.");
            return false;
        }

        return true;
    }

    @FXML
    private void handleLogin() {
        navigateTo("/fxml/Login.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateTo(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("SignupController: Navigation error to " + path + " — " + e.getMessage());
        }
    }
}