package com.hotel.app.controller.modules.shared;

import com.hotel.app.entity.User;
import com.hotel.app.repository.*;
import com.hotel.app.service.AuthService;
import com.hotel.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Shared Controller for the Profile Module.
 * Dynamically adapts UI based on whether the logged-in user is a Customer, Staff, or Manager.
 */
public class MyProfileController {

    // --- FXML Bindings: Shared Section ---
    @FXML private TextField nameField, emailField, countryCodeField, phoneNumberField, memberSinceField;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Label statusLabel;

    // --- FXML Bindings: Customer Section ---
    @FXML private VBox customerDetailsSection;
    @FXML private TextField addressField, nationalityField, idProofField;
    @FXML private Label loyaltyPointsLabel;

    // --- FXML Bindings: Employee (Staff/Manager) Section ---
    @FXML private VBox employeeDetailsSection;
    @FXML private TextField roleField, jobTitleField, departmentField, managerNameField;

    // --- Repositories & Services ---
    private final UserRepository userRepository = new UserRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final StaffRepository staffRepository = new StaffRepository();
    private final ManagerRepository managerRepository = new ManagerRepository();
    private final DepartmentRepository departmentRepository = new DepartmentRepository();

    private final AuthService authService = new AuthService(
            userRepository, customerRepository, staffRepository, managerRepository
    );

    @FXML
    public void initialize() {
        setupRoleVisibility();
        loadProfileData();
    }

    /**
     * Toggles which sections are visible and managed (taking up space)
     * based on the SessionManager roles.
     */
    private void setupRoleVisibility() {
        SessionManager session = SessionManager.getInstance();
        boolean isCustomer = session.hasRole("ROLE_CUSTOMER");
        boolean isEmployee = session.hasRole("ROLE_STAFF") || session.hasRole("ROLE_MANAGER");

        if (customerDetailsSection != null) {
            customerDetailsSection.setVisible(isCustomer);
            customerDetailsSection.setManaged(isCustomer);
        }

        if (employeeDetailsSection != null) {
            employeeDetailsSection.setVisible(isEmployee);
            employeeDetailsSection.setManaged(isEmployee);
        }
    }

    /**
     * Loads the base User data and then drills down into role-specific data.
     */
    private void loadProfileData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // 1. Shared User Data
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        countryCodeField.setText(currentUser.getPhoneCountryCode() != null ? currentUser.getPhoneCountryCode() : "+91");
        phoneNumberField.setText(currentUser.getPhoneNumber());

        if (currentUser.getCreatedAt() != null) {
            memberSinceField.setText(currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        // 2. Role-Specific Data Loading
        SessionManager session = SessionManager.getInstance();
        if (session.hasRole("ROLE_MANAGER")) {
            loadManagerData(currentUser.getUserId());
        } else if (session.hasRole("ROLE_STAFF")) {
            loadStaffData(currentUser.getUserId());
        } else if (session.hasRole("ROLE_CUSTOMER")) {
            loadCustomerData(currentUser.getUserId());
        }
    }

    private void loadCustomerData(long userId) {
        customerRepository.findByUserId(userId).ifPresent(c -> {
            addressField.setText(c.getAddress());
            nationalityField.setText(c.getNationality());
            idProofField.setText(c.getIdProof());
            loyaltyPointsLabel.setText(c.getLoyaltyPoints() + " pts");
        });
    }

    private void loadStaffData(long userId) {
        staffRepository.findById(userId).ifPresent(s -> {
            // Standard Staff Identification
            roleField.setText("Operational Staff");
            jobTitleField.setText(s.getJobDescription());

            departmentRepository.findById(s.getDepartmentId())
                    .ifPresent(d -> departmentField.setText(d.getDepartmentName()));

            managerNameField.setText(s.getManagerId() != 0 ? "Reporting to Manager ID: " + s.getManagerId() : "N/A");
        });
    }

    private void loadManagerData(long userId) {
        managerRepository.findByUserId(userId).ifPresent(m -> {
            jobTitleField.setText(m.getJobDescription());

            departmentRepository.findById(m.getDepartmentId()).ifPresent(d -> {
                departmentField.setText(d.getDepartmentName());

                // LOGIC: Check if this manager is the official Head (HOD) of the department
                // Uses the HeadManagerId logic from your DepartmentRepository
                if (d.getHeadManagerId() != 0 && d.getHeadManagerId() == m.getManagerId()) {
                    roleField.setText("Head of Department (HOD)");
                    roleField.setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;"); // Purple bold for HODs
                } else {
                    roleField.setText("Management Staff");
                    roleField.setStyle("-fx-text-fill: #2c3e50;");
                }
            });

            managerNameField.setText(m.getReportsToManagerId() == null || m.getReportsToManagerId() == 0 ?
                    "N/A (Top Level)" : "Reports to Manager ID: " + m.getReportsToManagerId());
        });
    }

    // --- Actions ---

    @FXML
    private void handleUpdateProfile() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        boolean success = userRepository.updateUser(
                user.getUserId(),
                nameField.getText(),
                emailField.getText(),
                countryCodeField.getText(),
                phoneNumberField.getText()
        );

        if (success) {
            showStatus("Profile updated successfully!", false);
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
        } else {
            showStatus("Update failed. Check email or connection.", true);
        }
    }

    @FXML
    private void handleSaveCustomerDetails() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        boolean success = customerRepository.updateProfileByUserId(
                currentUser.getUserId(),
                addressField.getText(),
                idProofField.getText(),
                nationalityField.getText()
        );

        if (success) {
            showStatus("Customer details saved!", false);
        } else {
            showStatus("Update failed.", true);
        }
    }

    @FXML
    private void handleChangePassword() {
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass.isEmpty() || !newPass.equals(confirmPass)) {
            showStatus("Passwords mismatch!", true);
            return;
        }

        boolean success = authService.updatePassword(SessionManager.getInstance().getCurrentUser().getUserId(), newPass);
        if (success) {
            showStatus("Password changed successfully!", false);
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showStatus("Failed to update password.", true);
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Deactivate account permanently?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Account Deletion");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                long userId = SessionManager.getInstance().getCurrentUser().getUserId();
                if (authService.requestAccountDeletion(userId)) {
                    SessionManager.getInstance().logout();
                    navigateToLogin();
                }
            }
        });
    }

    private void navigateToLogin() {
        try {
            // Strictly lowercase for WSL/Linux compatibility
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation to login failed: " + e.getMessage());
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}