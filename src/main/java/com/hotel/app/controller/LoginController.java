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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // Updated to 4-arg constructor to match the merged AuthService
    private final AuthService authService = new AuthService(
            new UserRepository(),
            new CustomerRepository(),
            new StaffRepository(),
            new ManagerRepository()
    );

    private final BookingService bookingService = new BookingService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();

        Optional<User> userOptional = authService.login(email, password);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            List<String> roles = authService.getRolesForUser(user.getUserId());

            // Single login call — duplicate removed
            SessionManager.getInstance().login(user, roles);

            // Cache customerId in session for all customer operations.
            // Only done for ROLE_CUSTOMER since staff/manager don't use customerId.
            if (roles.contains("ROLE_CUSTOMER")) {
                bookingService.getCustomerByUserId(user.getUserId())
                        .ifPresent(c -> SessionManager.getInstance().setCustomerId(c.getCustomerId()));
            }

            // Route to the correct dashboard based on role
            if (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN")) {
                navigateTo("/fxml/ManagerDashboard.fxml");
            } else if (roles.contains("ROLE_STAFF")) {
                navigateTo("/fxml/StaffDashboard.fxml");
            } else {
                navigateTo("/fxml/CustomerDashboard.fxml");
            }

        } else {
            showError("Invalid email or password.");
        }
    }

    @FXML
    private void handleSignUp() {
        navigateTo("/fxml/Signup.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateTo(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("LoginController: Failed to navigate to " + path + " — " + e.getMessage());
        }
    }
}