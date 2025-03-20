package com.minimercado.javafxinventario.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.minimercado.javafxinventario.modules.Employee;
import com.minimercado.javafxinventario.utils.*;

public class EmployeeController {
    
    @FXML private TextField searchField;
    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> lastNameColumn;
    @FXML private TableColumn<Employee, String> documentColumn;
    @FXML private TableColumn<Employee, String> positionColumn;
    @FXML private TableColumn<Employee, Double> salaryColumn;
    @FXML private TableColumn<Employee, LocalDate> startDateColumn;
    @FXML private TableColumn<Employee, String> statusColumn;
    @FXML private Label statusLabel;
    
    private ObservableList<Employee> employeesList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;
    
    @FXML
    private void initialize() {
        setupTableColumns();
        loadEmployees();
        setupSearch();
        
        // Update status label
        updateStatusLabel();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        documentColumn.setCellValueFactory(new PropertyValueFactory<>("documentId"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        
        // Reemplazar la implementación que usa activeProperty()
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isActive() ? "Activo" : "Inactivo"));
        
        // Format salary column to show currency
        salaryColumn.setCellFactory(column -> new TableCell<Employee, Double>() {
            @Override
            protected void updateItem(Double salary, boolean empty) {
                super.updateItem(salary, empty);
                if (empty || salary == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", salary));
                }
            }
        });
        
        // Format status column to show "Activo" or "Inactivo" (now handled by the cell value factory)
        statusColumn.setCellFactory(column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    setStyle("Activo".equals(status) ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }
    
    private void loadEmployees() {
        employeesList.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM employees ORDER BY last_name, first_name";
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Employee employee = new Employee();
                    employee.setId(rs.getInt("id"));
                    employee.setFirstName(rs.getString("first_name"));
                    employee.setLastName(rs.getString("last_name"));
                    employee.setDocumentId(rs.getString("document_id")); // Cambiado de document_number a document_id
                    employee.setPosition(rs.getString("position"));
                    employee.setBaseSalary(rs.getDouble("base_salary")); // Cambiado de salary a base_salary
                    employee.setHireDate(rs.getDate("hire_date").toLocalDate()); // Cambiado de start_date a hire_date
                    employee.setActive(rs.getString("status").equals("A")); // Convertir status (A/I) a boolean active
                    employeesList.add(employee);
                }
            }
            
            // Set items to table
            filteredEmployees = new FilteredList<>(employeesList, p -> true);
            employeesTable.setItems(filteredEmployees);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error de Base de Datos", "Error al cargar empleados: " + e.getMessage());
        }
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEmployees.setPredicate(employee -> {
                // If search field is empty, show all employees
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (employee.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (employee.getLastName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (employee.getDocumentId().toLowerCase().contains(lowerCaseFilter)) { // Cambiado de documentNumber a documentId
                    return true;
                } else if (employee.getPosition().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
            
            updateStatusLabel();
        });
    }
    
    private void updateStatusLabel() {
        int count = filteredEmployees != null ? filteredEmployees.size() : 0;
        statusLabel.setText(count + " empleados encontrados");
    }
    
    @FXML
    private void handleSearch() {
        // Search is already handled by the text property listener
        // This method is here to support the search button
        String searchText = searchField.getText();
        if (searchText != null && !searchText.isEmpty()) {
            System.out.println("Searching for: " + searchText);
        }
    }

    /**
     * Alternative name for handleSearch to support FXML reference
     */
    @FXML
    private void handleSearchEmployees() {
        // Simply forward to the existing implementation
        handleSearch();
    }
    
    @FXML
    private void handleClear() {
        searchField.clear();
        // The listener will automatically update the table
    }
    
    @FXML
    private void handleNewEmployee() {
        showNotImplemented("Nuevo Empleado");
        // TODO: Implement employee creation form
    }
    
    @FXML
    private void handleEditEmployee() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("Selección Requerida", 
                     "Por favor, seleccione un empleado para editar.", 
                     Alert.AlertType.WARNING);
            return;
        }
        
        showNotImplemented("Editar Empleado");
        // TODO: Implement employee edit form
    }
    
    @FXML
    private void handleViewEmployee() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("Selección Requerida", 
                     "Por favor, seleccione un empleado para ver detalles.", 
                     Alert.AlertType.WARNING);
            return;
        }
        
        showNotImplemented("Ver Detalles de Empleado");
        // TODO: Implement employee details view
    }
    
    @FXML
    private void handleToggleStatus() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("Selección Requerida", 
                     "Por favor, seleccione un empleado para cambiar su estado.", 
                     Alert.AlertType.WARNING);
            return;
        }
        
        boolean isActive = selectedEmployee.isActive(); // Usando el método isActive() directamente
        String action = isActive ? "desactivar" : "activar";
        boolean newStatus = !isActive; // Invertir el estado actual
        
        boolean confirmed = showConfirmation("Cambiar Estado",
                                           "¿Está seguro que desea " + action + " al empleado " + 
                                           selectedEmployee.getFullName() + "?"); // Usando getFullName() en lugar de concatenar
        
        if (confirmed) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE employees SET status = ? WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newStatus ? "A" : "I");
                    stmt.setInt(2, selectedEmployee.getId());
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        selectedEmployee.setActive(newStatus); // Cambiado de setStatus a setActive
                        employeesTable.refresh();
                        showAlert("Operación Exitosa", 
                                "El estado del empleado ha sido actualizado.", 
                                Alert.AlertType.INFORMATION);
                    } else {
                        showError("Error de Actualización", 
                                 "No se pudo actualizar el estado del empleado.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error de Base de Datos", 
                         "Error al actualizar el estado: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleReports() {
        showNotImplemented("Reportes de Empleados");
        // TODO: Implement employee reports view
    }
    
    private void showNotImplemented(String feature) {
        showAlert("Funcionalidad No Implementada", 
                 "La funcionalidad '" + feature + "' aún no está implementada.", 
                 Alert.AlertType.INFORMATION);
    }
    
    private void showError(String title, String content) {
        showAlert(title, content, Alert.AlertType.ERROR);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
