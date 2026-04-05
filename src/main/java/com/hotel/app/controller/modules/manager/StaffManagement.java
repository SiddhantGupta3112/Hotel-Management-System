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
import javafx.scene.layout.VBox;
import java.util.*;

public class StaffManagement {

    @FXML private TableView<Object> staffTable;
    @FXML private TableColumn<Object, Long> colId;
    @FXML private TableColumn<Object, String> colName, colEmail, colRole, colDept, colStatus;

    @FXML private TextField searchField, nameField, emailField, jobTitleField, salaryField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private ComboBox<Department> departmentCombo;
    @FXML private ComboBox<Manager> managerCombo;
    @FXML private CheckBox hodCheckBox;
    @FXML private VBox managerFieldBox;
    @FXML private Label formErrorLabel;

    private final DepartmentRepository departmentRepo = new DepartmentRepository();
    private final ManagerRepository managerRepo = new ManagerRepository();
    private final StaffRepository staffRepo = new StaffRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AuthService authService = new AuthService(userRepository, null, staffRepo, managerRepo);

    List<Staff> masterStaffList = new ArrayList<>();
    List<Manager> masterManagerList = new ArrayList<>();

    private boolean isAdmin, isHod;
    private long currentUserId, currentDeptId;

    @FXML
    public void initialize() {
        setupRoleContext();
        setupTable();
        setupDropdownDisplay();
        loadDropdowns();
        setupListeners();
        configureForm();
        loadStaffData();
    }

    private void setupRoleContext() {
        SessionManager session = SessionManager.getInstance();
        currentUserId = session.getCurrentUser().getUserId();
        isAdmin = session.hasRole("ROLE_ADMIN");

        managerRepo.findByUserId(currentUserId).ifPresentOrElse(m -> {
            currentDeptId = m.getDepartmentId();
            departmentRepo.findById(currentDeptId).ifPresent(d -> {
                isHod = (d.getHeadManagerId() != 0 && d.getHeadManagerId() == m.getManagerId());
            });
        }, () -> { isAdmin = true; });
    }

    private void setupListeners() {
        departmentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateManagerDropdown(newVal.getDepartmentId());
            }
        });
    }

    private void setupTable() {
        // ID Column
        colId.setCellValueFactory(c -> {
            Object item = c.getValue();
            if (item instanceof Staff s) return new SimpleLongProperty(s.getStaffId()).asObject();
            if (item instanceof Manager m) return new SimpleLongProperty(m.getManagerId()).asObject();
            return new SimpleLongProperty(0).asObject();
        });

        // Name Column
        colName.setCellValueFactory(c -> {
            Object item = c.getValue();
            if (item instanceof Staff s) return new SimpleStringProperty(s.getName());
            if (item instanceof Manager m) return new SimpleStringProperty(m.getName());
            return new SimpleStringProperty("");
        });

        // Email Column
        colEmail.setCellValueFactory(c -> {
            Object item = c.getValue();
            if (item instanceof Staff s) return new SimpleStringProperty(s.getEmail());
            if (item instanceof Manager m) return new SimpleStringProperty(m.getEmail());
            return new SimpleStringProperty("");
        });

        // Role Column
        colRole.setCellValueFactory(c -> {
            if (c.getValue() instanceof Manager) return new SimpleStringProperty("MANAGER");
            return new SimpleStringProperty("STAFF");
        });

        // Dept ID Column
        colDept.setCellValueFactory(c -> {
            Object item = c.getValue();
            long deptId = (item instanceof Staff s) ? s.getDepartmentId() : ((Manager) item).getDepartmentId();
            return new SimpleStringProperty(String.valueOf(deptId));
        });

        colStatus.setCellValueFactory(c -> new SimpleStringProperty("ACTIVE"));
    }

    private void loadStaffData() {


        if (isAdmin) {
            masterStaffList = staffRepo.findAll();
            masterManagerList = managerRepo.findAll();
        } else if (isHod) {
            masterStaffList = staffRepo.findByDepartment(currentDeptId);
            masterManagerList = managerRepo.findByDepartment(currentDeptId);
        } else {
            managerRepo.findByUserId(currentUserId).ifPresent(m -> {
                masterStaffList = staffRepo.findByManagerId(m.getManagerId());
            });
        }

        List<Object> combinedList = new ArrayList<>();
        combinedList.addAll(masterManagerList);
        combinedList.addAll(masterStaffList);
        staffTable.setItems(FXCollections.observableArrayList(combinedList));
    }

    private void updateManagerDropdown(long deptId) {
        List<Manager> managers = managerRepo.findByDepartment(deptId);
        managerCombo.setItems(FXCollections.observableArrayList(managers));

        // Auto-select the HOD if they are in the list
        if (isHod) {
            managers.stream()
                    .filter(m -> m.getUserId() == currentUserId)
                    .findFirst()
                    .ifPresent(managerCombo::setValue);
        }
    }

    private void configureForm() {
        hodCheckBox.setVisible(isAdmin);
        hodCheckBox.setManaged(isAdmin);

        if (!isAdmin) {
            departmentRepo.findById(currentDeptId).ifPresent(d -> {
                departmentCombo.setValue(d);
                departmentCombo.setDisable(true);
                // Triggered because listener was set up before this call
            });

            if (!isHod) {
                managerRepo.findByUserId(currentUserId).ifPresent(m -> {
                    managerCombo.setValue(m);
                    managerCombo.setDisable(true);
                });
            }
        }
    }

    @FXML
    private void handleCreateStaff() {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String name = nameField.getText().trim();
            String role = roleCombo.getValue();
            Department dept = departmentCombo.getValue();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || role == null || dept == null) {
                showStatus("Please fill all required fields", true);
                return;
            }

            Long reportingMgrId = null;
            if (!hodCheckBox.isSelected()) {
                Manager m = managerCombo.getValue();
                if (m == null) {
                    showStatus("Please select a reporting manager", true);
                    return;
                }
                reportingMgrId = m.getManagerId();
            }

            boolean success = "ROLE_MANAGER".equals(role)
                    ? authService.registerManager(email, password, name, "+91", "0000000000", dept.getDepartmentId(), reportingMgrId, jobTitleField.getText(), Double.parseDouble(salaryField.getText()))
                    : authService.registerStaff(email, password, name, "+91", "0000000000", dept.getDepartmentId(), reportingMgrId, jobTitleField.getText(), Double.parseDouble(salaryField.getText()));

            if (success) {
                if (hodCheckBox.isSelected()) linkNewHodToDepartment(email, dept.getDepartmentId());
                showStatus("Account created successfully!", false);
                clearForm();
                loadStaffData();
            } else {
                showStatus("Creation failed. Check database.", true);
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    private void linkNewHodToDepartment(String email, long deptId) {
        userRepository.findByEmail(email).ifPresent(user -> {
            managerRepo.findByUserId(user.getUserId()).ifPresent(mgr -> {
                departmentRepo.updateHead(deptId, mgr.getManagerId());
            });
        });
    }

    @FXML
    private void handleHodCheckboxToggle() {
        boolean isChecked = hodCheckBox.isSelected();
        managerFieldBox.setDisable(isChecked);
        if (isChecked) {
            managerCombo.setValue(null);
            roleCombo.setValue("ROLE_MANAGER");
            roleCombo.setDisable(true);
        } else {
            roleCombo.setDisable(false);
        }
    }

    private void loadDropdowns() {
        roleCombo.setItems(FXCollections.observableArrayList("ROLE_STAFF", "ROLE_MANAGER"));
        departmentCombo.setItems(FXCollections.observableArrayList(departmentRepo.findAll()));
    }

    private void setupDropdownDisplay() {
        departmentCombo.setCellFactory(cb -> new ListCell<Department>() {
            @Override protected void updateItem(Department d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.getDepartmentName());
            }
        });
        departmentCombo.setButtonCell(departmentCombo.getCellFactory().call(null));

        managerCombo.setCellFactory(cb -> new ListCell<Manager>() {
            @Override protected void updateItem(Manager m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) {
                    setText(null);
                } else {
                    String text = m.getName();
                    if (m.getUserId() == currentUserId) text += " (Me / HOD)";
                    setText(text);
                }
            }
        });
        managerCombo.setButtonCell(managerCombo.getCellFactory().call(null));
    }

    private void showStatus(String msg, boolean isError) {
        formErrorLabel.setText(msg);
        formErrorLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        formErrorLabel.setVisible(true);
    }

    private void clearForm() {
        nameField.clear(); emailField.clear(); jobTitleField.clear(); salaryField.clear(); passwordField.clear();
        hodCheckBox.setSelected(false);
        managerFieldBox.setDisable(false);
        roleCombo.setDisable(false);
    }
}