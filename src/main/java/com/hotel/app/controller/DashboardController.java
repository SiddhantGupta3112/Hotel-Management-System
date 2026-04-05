package com.hotel.app.controller;

import com.hotel.app.util.SessionManager;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

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
     * Customizes the navigation sidebar based on specific roles.
     */
    @Override
    protected void onDashboardReady() {
        boolean isAdmin = SessionManager.getInstance().hasRole("ROLE_ADMIN");

        navContainer.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                String text = label.getText();

                // 1. Security Check: Only ROLE_ADMIN sees Staff Management
                boolean isAdminOrManager =
                        SessionManager.getInstance().hasRole("ROLE_ADMIN") ||
                                SessionManager.getInstance().hasRole("ROLE_MANAGER");

                if (text.equals("Staff Management") && !isAdminOrManager) {
                    label.setVisible(false);
                    label.setManaged(false);
                }

                // 2. Ensure "My Profile" is always visible and managed
                if (text.equals("My Profile")) {
                    label.setVisible(true);
                    label.setManaged(true);
                }

                if (text.equals("Department Management") && !isAdmin) {
                    label.setVisible(false);
                    label.setManaged(false);
                }
            }
        });

        // 3. Update Topbar with Session Data (If not handled in Base)
        updateHeaderInfo();
    }

    private void updateHeaderInfo() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getName());
            userRoleLabel.setText(SessionManager.getInstance().getRoles().get(0).replace("ROLE_", ""));

            // Set Avatar Initials (e.g., "John Doe" -> "JD")
            String initials = user.getName().replaceAll("^\\s*(\\w).*\\s+(\\w).*$", "$1$2").toUpperCase();
            avatarLabel.setText(initials.length() > 2 ? initials.substring(0, 2) : initials);
        }
    }

    /**
     * Handles the specific case for the Profile module since it lives in 'shared'
     * instead of the 'manager/' subdirectory.
     */
    @Override
    protected void handleNavigation(MouseEvent event) {
        // 1. Always call super first to handle the CSS class switching (highlighting)
        super.handleNavigation(event);

        Label source = (Label) event.getSource();

        if (source.getText().equals("My Profile")) {
            loadSharedModule("my_profile.fxml");
        }
    }
}