package com.hotel.app.controller;

import com.hotel.app.util.SessionManager;

/**
 * Manager / Admin dashboard.
 * Shared screen — admin sections hidden for ROLE_MANAGER via onDashboardReady().
 * Modules live in: /fxml/modules/manager/
 */
public class DashboardController extends BaseDashboardController {

    @Override
    protected String getModulePath() {
        return "manager/";
    }

    @Override
    protected String getDefaultModule() {
        return "overview.fxml";
    }

    /**
     * Hide admin-only nav items from managers.
     * Called automatically by BaseDashboardController.initialize().
     */
    @Override
    protected void onDashboardReady() {
        boolean isAdmin = SessionManager.getInstance().hasRole("ROLE_ADMIN");
        boolean isManager = SessionManager.getInstance().hasRole("ROLE_MANAGER");

        navContainer.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.control.Label label) {
                String text = label.getText();
                // Hide if it's Staff Management AND the user is NEITHER an admin nor a manager
                if (text.equals("Staff Management") && !(isAdmin || isManager)) {
                    label.setVisible(false);
                    label.setManaged(false);
                }
            }
        });
    }
}