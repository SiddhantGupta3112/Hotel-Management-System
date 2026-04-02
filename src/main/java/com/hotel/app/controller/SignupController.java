package com.hotel.app.controller;

import com.hotel.app.entity.User;
import com.hotel.app.repository.UserRepository;
import com.hotel.app.service.AuthService;
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
import java.util.regex.*;

public class SignupController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField countryCodeField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService(new UserRepository());

    @FXML
    private  void handleSignup(){
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String countryCode = countryCodeField.getText().trim();
        String phone = phoneNumberField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if(validateInput(name, email, password, confirmPassword, countryCode, phone)){

            boolean registered = authService.register(email, password, name, countryCode, phone);

            if(! registered){
                showError("Error signing up user");
                return;
            }

            Optional<User> userOptional = authService.login(email, password);

            if(userOptional.isPresent()){
                User user = userOptional.get();

                List<String> roles = authService.getRolesForUser(user.getUserId());

                SessionManager.getInstance().login(user, roles);

                navigateTo("/fxml/CustomerDashboard.fxml");
            }
            else{
                showError("Error Signing up. Please try again later");
            }
        }
    }

    private boolean validateInput(String name, String email, String pass, String confirm, String countryCode, String phone) {
        if(name.isEmpty() || email.isEmpty() || pass.isEmpty()
                || countryCode.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all required fields.");
            return false;
        }

        String regex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches()){
            showError("Invalid email format");
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
        if(authService.emailExists(email)){
            showError("Email already in use");
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
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}