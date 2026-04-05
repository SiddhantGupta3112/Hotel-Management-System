package com.hotel.app.controller.modules.manager;

import com.hotel.app.entity.*;
import com.hotel.app.repository.*;
import com.hotel.app.service.AuthService;
import com.hotel.app.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.*;


public class DepartmentManagement {
    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> nameCol;
    @FXML private TableColumn<Department, String> hodCol;
    @FXML private TableColumn<Department, String> emailCol;
    @FXML private TextField deptNameField;

    private final DepartmentRepository deptRepo = new DepartmentRepository();

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("departmentName"));

        // HOD Name Column
        hodCol.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                    deptRepo.getHOD(cellData.getValue().getDepartmentId())
                            .map(Manager::getName)
                            .orElse("Unassigned")
            );
        });

        // HOD Email Column
        emailCol.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                    deptRepo.getHOD(cellData.getValue().getDepartmentId())
                            .map(Manager::getEmail)
                            .orElse("-")
            );
        });

        loadDepartments();
    }

    private String fetchHodName(long deptId) {
        return deptRepo.getHOD(deptId)
                .map(Manager::getName)
                .orElse("Unassigned");
    }

    private void loadDepartments() {
        List<Department> departments = deptRepo.findAll();
        departmentTable.setItems(FXCollections.observableArrayList(departments));
    }

    @FXML
    private void handleCreateDepartment() {
        String name = deptNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert("Validation Error", "Department name cannot be empty.");
            return;
        }

        if (deptRepo.exists(name)) {
            showAlert("Duplicate Error", "A department with this name already exists.");
            return;
        }

        // Save with headManagerId as NULL (0) as requested
        boolean success = deptRepo.save(name, 0L);

        if (success) {
            deptNameField.clear();
            loadDepartments(); // Refresh table
        } else {
            showAlert("Database Error", "Could not create department.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}