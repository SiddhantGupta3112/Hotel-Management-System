package com.hotel.app.controller;

import com.hotel.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Base class for all dashboard controllers.
 * Handles: navigation, module loading, user info setup, logout.
 *
 * Subclasses must implement:
 *   - getModulePath()   → folder under /fxml/modules/ e.g. "manager/" or "customer/"
 *   - getDefaultModule() → first module to load e.g. "overview.fxml"
 *   - onDashboardReady() → called after user info is set, for role-specific setup
 */
public abstract class BaseDashboardController {

    @FXML protected StackPane contentPlaceholder;
    @FXML protected Label     pageTitle;
    @FXML protected Label     userNameLabel;
    @FXML protected Label     userRoleLabel;
    @FXML protected Label     avatarLabel;
    @FXML protected VBox      navContainer;

    // ── Called by JavaFX after FXML fields are injected ──────────────

    @FXML
    public final void initialize() {
        setupUserInfo();
        onDashboardReady();
        loadModule(getDefaultModule());
    }

    // ── Abstract contract ─────────────────────────────────────────────

    /** Returns the subfolder path under /fxml/modules/, e.g. "manager/" or "customer/" */
    protected abstract String getModulePath();

    /** Returns the default FXML filename to load on startup, e.g. "overview.fxml" */
    protected abstract String getDefaultModule();

    /**
     * Called after user info is set up.
     * Use this for role-specific nav hiding or extra initialisation.
     */
    protected void onDashboardReady() {}

    // ── Navigation ────────────────────────────────────────────────────

    @FXML
    protected void handleNavigation(MouseEvent event) {
        Label selected = (Label) event.getSource();

        // Reset all nav items
        navContainer.getChildren().forEach(node -> {
            node.getStyleClass().remove("nav-item-active");
            if (!node.getStyleClass().contains("nav-item")) {
                node.getStyleClass().add("nav-item");
            }
        });

        // Activate selected
        selected.getStyleClass().remove("nav-item");
        selected.getStyleClass().add("nav-item-active");

        String title = selected.getText();
        if (pageTitle != null) pageTitle.setText(title);

        // Convert nav text to filename: "Rooms & Inventory" -> "rooms_inventory.fxml"
        String fileName = title.toLowerCase()
                .replace(" & ", "_")
                .replace(" ", "_")
                .replace("&", "")
                + ".fxml";

        loadModule(fileName);
    }

    // ── Module loading ────────────────────────────────────────────────

    protected void loadModule(String fxmlFile) {
        try {
            String path = "/fxml/modules/" + getModulePath() + fxmlFile;
            Node node = FXMLLoader.load(getClass().getResource(path));
            contentPlaceholder.getChildren().setAll(node);
        } catch (Exception e) {
            System.err.println("Could not load module: " + fxmlFile + " — " + e.getMessage());
            Label placeholder = new Label("'" + fxmlFile.replace(".fxml", "").replace("_", " ") + "' is coming soon");
            placeholder.getStyleClass().add("muted-text");
            contentPlaceholder.getChildren().setAll(placeholder);
        }
    }

    // ── Logout ────────────────────────────────────────────────────────

    @FXML
    protected void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage  = (Stage) contentPlaceholder.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Logout navigation failed: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void setupUserInfo() {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) return;

        String name = session.getCurrentUser().getName();
        String role = session.getRoles().isEmpty() ? "" : session.getRoles().get(0);

        if (userNameLabel != null) userNameLabel.setText(name);
        if (userRoleLabel  != null) userRoleLabel.setText(role);
        if (avatarLabel    != null) avatarLabel.setText(getInitials(name));
    }

    protected static String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}