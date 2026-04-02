package com.hotel.app.controller;

/**
 * Staff dashboard controller.
 * Modules live in: /fxml/modules/staff/
 * Default module: my_tasks.fxml
 *
 * Nav items defined in StaffDashboard.fxml:
 *   My Tasks        → my_tasks.fxml
 *   Room Status     → room_status.fxml
 *   Completed Tasks → completed_tasks.fxml
 *   My Profile      → my_profile.fxml
 */
public class StaffDashboardController extends BaseDashboardController {

    @Override
    protected String getModulePath() {
        return "staff/";
    }

    @Override
    protected String getDefaultModule() {
        return "my_tasks.fxml";
    }
}