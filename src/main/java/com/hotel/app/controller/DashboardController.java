package com.hotel.app.controller;

import com.hotel.app.entity.User;
import com.hotel.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label avatarLabel;
    @FXML private Label statNameLabel;
    @FXML private Label statRoleLabel;
    @FXML private Label infoNameLabel;
    @FXML private Label infoEmailLabel;
    @FXML private Label infoRoleCheckLabel;
    @FXML private Label infoRoleResultLabel;

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        User user = session.getCurrentUser();
        List<String> roles = session.getRoles();
        String primaryRole = roles.isEmpty() ? "No Role" : roles.get(0);

        // Top bar
        userNameLabel.setText(user.getName());
        userRoleLabel.setText(primaryRole);
        avatarLabel.setText(getInitials(user.getName()));

        // Stat cards
        statNameLabel.setText(user.getName());
        statRoleLabel.setText(primaryRole);

        // Session info
        infoNameLabel.setText(user.getName());
        infoEmailLabel.setText(user.getEmail());
        infoRoleCheckLabel.setText("hasRole(\"" + primaryRole + "\")");
        infoRoleResultLabel.setText(
                String.valueOf(session.hasRole(primaryRole))
        );
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml");
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void navigateTo(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }
}