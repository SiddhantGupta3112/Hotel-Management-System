package com.hotel.app.controller;

/**
 * Alias for DashboardController — both manager and admin use the same screen.
 * ManagerDashboard.fxml points to this controller.
 * Modules live in: /fxml/modules/manager/
 */
public class ManagerDashboardController extends DashboardController {
    // Inherits everything from DashboardController.
    // No additional logic needed — role hiding is handled in DashboardController.onDashboardReady()
}