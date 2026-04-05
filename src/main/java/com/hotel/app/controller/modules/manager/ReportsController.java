package com.hotel.app.controller.modules.manager;

import com.hotel.app.repository.ReportRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Reports module.
 * Visualizes hotel data using Line, Bar, and Pie charts with async data fetching.
 */
public class ReportsController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> bookingsChart;
    @FXML private BarChart<String, Number> servicesChart;
    @FXML private PieChart bookingStatusChart;
    @FXML private PieChart roomTypeChart;

    private final ReportRepository reportRepo = new ReportRepository();

    @FXML
    public void initialize() {
        // Default range: Last 6 months to today
        fromDatePicker.setValue(LocalDate.now().minusMonths(6));
        toDatePicker.setValue(LocalDate.now());

        loadAllReports();
    }

    @FXML
    private void handleApplyFilter() {
        loadAllReports();
    }

    @FXML
    private void handleResetFilter() {
        fromDatePicker.setValue(LocalDate.now().minusMonths(6));
        toDatePicker.setValue(LocalDate.now());
        loadAllReports();
    }

    private void loadAllReports() {
        LocalDate start = fromDatePicker.getValue();
        LocalDate end = toDatePicker.getValue();

        // Run heavy DB aggregation in the background
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Fetch data from the new ReportRepository
                Map<String, Double> revenueData = reportRepo.getMonthlyRevenue(start, end);
                Map<String, Integer> bookingData = reportRepo.getMonthlyBookingCount(start, end);
                Map<String, Integer> statusData = reportRepo.getBookingStatusDistribution(start, end);
                Map<String, Integer> typeData = reportRepo.getRoomTypeDistribution();
                Map<String, Integer> serviceData = reportRepo.getPopularServices(start, end);

                // 2. Update the UI on the FX Thread
                Platform.runLater(() -> {
                    updateLineChart(revenueChart, revenueData, "Revenue (₹)");
                    updateBarChart(bookingsChart, bookingData, "Total Bookings");
                    updateBarChart(servicesChart, serviceData, "Times Requested");
                    updatePieChart(bookingStatusChart, statusData);
                    updatePieChart(roomTypeChart, typeData);
                });

            } catch (Exception e) {
                System.err.println("Critical error generating reports: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ── Chart Update Helpers ──────────────────────────────────────────

    private void updateLineChart(LineChart<String, Number> chart, Map<String, Double> data, String seriesName) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        data.forEach((month, val) -> {
            series.getData().add(new XYChart.Data<>(month, val));
        });

        chart.getData().add(series);
    }

    private void updateBarChart(BarChart<String, Number> chart, Map<String, Integer> data, String seriesName) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        data.forEach((label, count) -> {
            series.getData().add(new XYChart.Data<>(label, count));
        });

        chart.getData().add(series);
    }

    private void updatePieChart(PieChart chart, Map<String, Integer> data) {
        chart.getData().clear();
        data.forEach((name, count) -> {
            chart.getData().add(new PieChart.Data(name + " (" + count + ")", count));
        });
    }
}