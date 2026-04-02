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

        navContainer.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.control.Label label) {
                String text = label.getText();
                // Only ROLE_ADMIN sees Staff Management
                if (text.equals("Staff Management") && !isAdmin) {
                    label.setVisible(false);
                    label.setManaged(false);
                }
            }
        });
    }
}