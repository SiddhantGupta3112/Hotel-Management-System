package com.hotel.app.controller;

/**
 * Customer dashboard controller.
 * Modules live in: /fxml/modules/customer/
 * Default module: my_bookings.fxml
 *
 * Nav items defined in CustomerDashboard.fxml:
 *   My Bookings    → my_bookings.fxml
 *   Book a Room    → book_a_room.fxml
 *   My Services    → availaible_rooms.fxml
 *   My Invoices    → my_invoices.fxml
 *   My Profile     → my_profile.fxml
 */
public class CustomerDashboardController extends BaseDashboardController {

    @Override
    protected String getModulePath() {
        return "customer/";
    }

    @Override
    protected String getDefaultModule() {
        return "my_bookings.fxml";
    }
}