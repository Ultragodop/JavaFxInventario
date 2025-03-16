package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.EmployeeDAO;
import com.minimercado.javafxinventario.modules.Employee;
import com.minimercado.javafxinventario.modules.EmployeePayment;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// Add the missing import statement
import com.minimercado.javafxinventario.controllers.EmployeePaymentHistoryController;

/**
 * Controlador para la gestión de empleados.
 */
public class EmployeeController {

    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, String> firstNameColumn;
    @FXML private TableColumn<Employee, String> lastNameColumn;
    @FXML private TableColumn<Employee, String> documentIdColumn;
    @FXML private TableColumn<Employee, String> positionColumn;
    @FXML private TableColumn<Employee, String> baseSalaryColumn;
    @FXML private TableColumn<Employee, String> hireDateColumn;
    @FXML private TableColumn<Employee, String> contactPhoneColumn;

    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField documentIdField;
    @FXML private TextField positionField;
    @FXML private TextField baseSalaryField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField contactPhoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private CheckBox activeCheckBox;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private Employee selectedEmployee;

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        documentIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentId()));
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        baseSalaryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("$%.2f", cellData.getValue().getBaseSalary())));
        hireDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getHireDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        contactPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactPhone()));

        // Cargar datos iniciales
        loadEmployees();

        // Configurar listener para selección de la tabla
        employeesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectEmployee(newValue));

        // Configurar búsqueda en tiempo real
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadEmployees();
            } else {
                searchEmployees(newValue);
            }
        });

        // Configurar fecha actual para el DatePicker por defecto
        hireDatePicker.setValue(LocalDate.now());
    }

    /**
     * Carga todos los empleados en la tabla.
     */
    private void loadEmployees() {
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            employeeList.setAll(employees);
            employeesTable.setItems(employeeList);
            updateStatusLabel("Se cargaron " + employees.size() + " empleados");
        } catch (Exception e) {
            showErrorMessage("Error al cargar empleados", e.getMessage());
        }
    }

    /**
     * Busca empleados según el término ingresado.
     * @param searchTerm Término de búsqueda
     */
    private void searchEmployees(String searchTerm) {
        try {
            List<Employee> filteredEmployees = employeeDAO.searchEmployees(searchTerm);
            employeeList.setAll(filteredEmployees);
            updateStatusLabel("Se encontraron " + filteredEmployees.size() + " resultados");
        } catch (Exception e) {
            showErrorMessage("Error al buscar empleados", e.getMessage());
        }
    }

    /**
     * Selecciona un empleado y muestra sus datos en el formulario.
     * @param employee Empleado seleccionado
     */
    private void selectEmployee(Employee employee) {
        selectedEmployee = employee;

        if (employee != null) {
            // Cargar datos del empleado en los campos del formulario
            firstNameField.setText(employee.getFirstName());
            lastNameField.setText(employee.getLastName());
            documentIdField.setText(employee.getDocumentId());
            positionField.setText(employee.getPosition());
            baseSalaryField.setText(String.valueOf(employee.getBaseSalary()));
            hireDatePicker.setValue(employee.getHireDate());
            contactPhoneField.setText(employee.getContactPhone());
            emailField.setText(employee.getEmail());
            addressArea.setText(employee.getAddress());
            activeCheckBox.setSelected(employee.isActive());

            updateStatusLabel("Empleado seleccionado: " + employee.getFullName());
        } else {
            clearForm();
        }
    }

    /**
     * Limpia el formulario de empleado.
     */
    @FXML
    private void handleClearForm() {
        clearForm();
        selectedEmployee = null;
        updateStatusLabel("Formulario limpio para nuevo empleado");
    }

    /**
     * Guarda o actualiza un empleado.
     */
    @FXML
    private void handleSaveEmployee() {
        try {
            if (!validateForm()) {
                return;
            }

            // Crear o actualizar empleado según corresponda
            Employee employee = (selectedEmployee != null) ? selectedEmployee : new Employee();
            
            // Mapear campos del formulario al objeto
            employee.setFirstName(firstNameField.getText().trim());
            employee.setLastName(lastNameField.getText().trim());
            employee.setDocumentId(documentIdField.getText().trim());
            employee.setPosition(positionField.getText().trim());
            employee.setBaseSalary(Double.parseDouble(baseSalaryField.getText().trim()));
            employee.setHireDate(hireDatePicker.getValue());
            employee.setContactPhone(contactPhoneField.getText().trim());
            employee.setEmail(emailField.getText().trim());
            employee.setAddress(addressArea.getText().trim());
            employee.setActive(activeCheckBox.isSelected());

            boolean success;
            if (selectedEmployee == null) {
                // Es un nuevo empleado
                success = employeeDAO.insertEmployee(employee);
                if (success) {
                    updateStatusLabel("Empleado agregado correctamente");
                }
            } else {
                // Es una actualización
                success = employeeDAO.updateEmployee(employee);
                if (success) {
                    updateStatusLabel("Empleado actualizado correctamente");
                }
            }

            if (success) {
                loadEmployees();
                clearForm();
                selectedEmployee = null;
            } else {
                updateStatusLabel("No se pudo guardar el empleado");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("Error de formato", "El salario base debe ser un número válido");
        } catch (Exception e) {
            showErrorMessage("Error al guardar empleado", e.getMessage());
        }
    }

    /**
     * Maneja el evento de eliminar un empleado.
     */
    @FXML
    private void handleDeleteEmployee() {
        if (selectedEmployee == null) {
            updateStatusLabel("Seleccione un empleado para eliminar");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este empleado?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = employeeDAO.deleteEmployee(selectedEmployee.getId());
            if (deleted) {
                updateStatusLabel("Empleado eliminado: " + selectedEmployee.getFullName());
                loadEmployees();
                clearForm();
                selectedEmployee = null;
            } else {
                updateStatusLabel("No se pudo eliminar el empleado");
            }
        }
    }

    /**
     * Abre el formulario para registrar un pago a empleado.
     */
    @FXML
    private void handleRegisterPayment() {
        if (selectedEmployee == null) {
            updateStatusLabel("Seleccione un empleado para registrar un pago");
            return;
        }

        try {
            // Cargar la vista de registro de pago
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/employee-payment.fxml"));
            BorderPane paymentPane = loader.load();
            
            // Obtener el controlador y pasarle el empleado seleccionado
            EmployeePaymentController paymentController = loader.getController();
            paymentController.initData(selectedEmployee);
            
            // Crear y configurar la nueva ventana
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Registrar Pago a " + selectedEmployee.getFullName());
            paymentStage.initModality(Modality.WINDOW_MODAL);
            paymentStage.initOwner(employeesTable.getScene().getWindow());
            paymentStage.setScene(new Scene(paymentPane));
            
            // Mostrar la ventana y esperar a que se cierre
            paymentStage.showAndWait();
            
        } catch (IOException e) {
            showErrorMessage("Error al abrir el formulario de pago", e.getMessage());
        }
    }

    /**
     * Abre la vista para ver el historial de pagos de un empleado.
     */
    @FXML
    private void handleViewPaymentHistory() {
        if (selectedEmployee == null) {
            updateStatusLabel("Seleccione un empleado para ver su historial de pagos");
            return;
        }

        try {
            // Cargar la vista de historial de pagos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/payment-history.fxml"));
            BorderPane historyPane = loader.load();
            
            // Obtener el controlador y pasarle el empleado seleccionado
            EmployeePaymentHistoryController historyController = loader.getController();
            historyController.initData(selectedEmployee);
            
            // Crear y configurar la nueva ventana
            Stage historyStage = new Stage();
            historyStage.setTitle("Historial de Pagos - " + selectedEmployee.getFullName());
            historyStage.initModality(Modality.WINDOW_MODAL);
            historyStage.initOwner(employeesTable.getScene().getWindow());
            historyStage.setScene(new Scene(historyPane));
            
            // Mostrar la ventana
            historyStage.show();
            
        } catch (IOException e) {
            showErrorMessage("Error al abrir el historial de pagos", e.getMessage());
        }
    }

    /**
     * Limpia el formulario de empleado.
     */
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        documentIdField.clear();
        positionField.clear();
        baseSalaryField.clear();
        hireDatePicker.setValue(LocalDate.now());
        contactPhoneField.clear();
        emailField.clear();
        addressArea.clear();
        activeCheckBox.setSelected(true);
    }

    /**
     * Valida los campos requeridos del formulario.
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Nombre es requerido\n");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Apellido es requerido\n");
        }
        if (documentIdField.getText().trim().isEmpty()) {
            errorMessage.append("- Documento de identidad es requerido\n");
        }
        if (positionField.getText().trim().isEmpty()) {
            errorMessage.append("- Cargo es requerido\n");
        }
        if (baseSalaryField.getText().trim().isEmpty()) {
            errorMessage.append("- Salario base es requerido\n");
        } else {
            try {
                double salary = Double.parseDouble(baseSalaryField.getText().trim());
                if (salary < 0) {
                    errorMessage.append("- El salario no puede ser negativo\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- El salario debe ser un número válido\n");
            }
        }

        if (errorMessage.length() > 0) {
            showErrorMessage("Error de validación", errorMessage.toString());
            return false;
        }
        return true;
    }

    /**
     * Actualiza el mensaje de estado.
     * @param message Mensaje a mostrar
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }

    /**
     * Muestra un mensaje de error.
     * @param title Título del mensaje
     * @param message Contenido del mensaje
     */
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the search button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadEmployees();
        } else {
            searchEmployees(searchTerm);
        }
    }

    /**
     * Handles the clear button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleClear(ActionEvent actionEvent) {
        searchField.clear();
        loadEmployees();
        clearForm();
        selectedEmployee = null;
        updateStatusLabel("Búsqueda reiniciada");
    }

    /**
     * Handles the new employee button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleNewEmployee(ActionEvent actionEvent) {
        clearForm();
        selectedEmployee = null;
        updateStatusLabel("Complete el formulario para agregar un nuevo empleado");
        
        // Set focus to the first name field
        firstNameField.requestFocus();
    }

    /**
     * Handles the edit employee button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleEditEmployee(ActionEvent actionEvent) {
        if (selectedEmployee == null) {
            Employee employee = employeesTable.getSelectionModel().getSelectedItem();
            if (employee != null) {
                selectEmployee(employee);
                updateStatusLabel("Editando empleado: " + employee.getFullName());
            } else {
                updateStatusLabel("Seleccione un empleado para editar");
            }
        } else {
            updateStatusLabel("Editando empleado: " + selectedEmployee.getFullName());
        }
        
        if (selectedEmployee != null) {
            firstNameField.requestFocus();
        }
    }

    /**
     * Handles the view employee details button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleViewEmployee(ActionEvent actionEvent) {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee == null) {
            updateStatusLabel("Seleccione un empleado para ver detalles");
            return;
        }

        try {
            // Create a dialog to show employee details
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Detalles del Empleado");
            dialog.setHeaderText("Información de " + employee.getFullName());
            
            // Create a grid pane for the details
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Add employee information to the grid
            int row = 0;
            grid.add(new Label("Nombre completo:"), 0, row);
            grid.add(new Label(employee.getFullName()), 1, row++);
            
            grid.add(new Label("Documento:"), 0, row);
            grid.add(new Label(employee.getDocumentId()), 1, row++);
            
            grid.add(new Label("Cargo:"), 0, row);
            grid.add(new Label(employee.getPosition()), 1, row++);
            
            grid.add(new Label("Salario base:"), 0, row);
            grid.add(new Label(String.format("$%.2f", employee.getBaseSalary())), 1, row++);
            
            grid.add(new Label("Fecha de contratación:"), 0, row);
            grid.add(new Label(employee.getHireDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 1, row++);
            
            grid.add(new Label("Teléfono:"), 0, row);
            grid.add(new Label(employee.getContactPhone() != null ? employee.getContactPhone() : "No disponible"), 1, row++);
            
            grid.add(new Label("Email:"), 0, row);
            grid.add(new Label(employee.getEmail() != null ? employee.getEmail() : "No disponible"), 1, row++);
            
            grid.add(new Label("Dirección:"), 0, row);
            grid.add(new Label(employee.getAddress() != null ? employee.getAddress() : "No disponible"), 1, row++);
            
            grid.add(new Label("Estado:"), 0, row);
            grid.add(new Label(employee.isActive() ? "Activo" : "Inactivo"), 1, row);
            
            dialog.getDialogPane().setContent(grid);
            
            // Add close button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            showErrorMessage("Error al mostrar detalles", e.getMessage());
        }
    }

    /**
     * Handles the toggle status button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleToggleStatus(ActionEvent actionEvent) {
        Employee employee = employeesTable.getSelectionModel().getSelectedItem();
        if (employee == null) {
            updateStatusLabel("Seleccione un empleado para cambiar su estado");
            return;
        }

        // Confirm the status toggle
        String action = employee.isActive() ? "desactivar" : "activar";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar cambio de estado");
        alert.setHeaderText("¿Está seguro que desea " + action + " este empleado?");
        alert.setContentText("Empleado: " + employee.getFullName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Toggle the status
            employee.setActive(!employee.isActive());
            
            // Update in database
            boolean updated = employeeDAO.updateEmployee(employee);
            if (updated) {
                updateStatusLabel("Estado del empleado actualizado: " + 
                                 (employee.isActive() ? "Activo" : "Inactivo"));
                
                // Refresh employee list to reflect the change
                loadEmployees();
                
                // If we were editing this employee, update the form
                if (selectedEmployee != null && selectedEmployee.getId() == employee.getId()) {
                    selectEmployee(employee);
                }
            } else {
                updateStatusLabel("No se pudo actualizar el estado del empleado");
            }
        }
    }

    /**
     * Handles the reports button action
     * @param actionEvent The event object
     */
    @FXML
    public void handleReports(ActionEvent actionEvent) {
        try {
            // Create a dropdown menu of report options
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Reporte de Nómina", 
                "Reporte de Nómina", 
                "Historial de Pagos", 
                "Empleados por Departamento",
                "Cumpleaños del Mes"
            );
            
            dialog.setTitle("Reportes de Empleados");
            dialog.setHeaderText("Seleccione un tipo de reporte");
            dialog.setContentText("Tipo de reporte:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String reportType = result.get();
                
                switch (reportType) {
                    case "Reporte de Nómina":
                        generatePayrollReport();
                        break;
                    case "Historial de Pagos":
                        generatePaymentHistoryReport();
                        break;
                    case "Empleados por Departamento":
                        generateDepartmentReport();
                        break;
                    case "Cumpleaños del Mes":
                        generateBirthdayReport();
                        break;
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error al generar reporte", e.getMessage());
        }
    }
    
    /**
     * Generates a payroll report
     */
    private void generatePayrollReport() {
        // This would typically involve generating a report of all active employees
        // and their salary information
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reporte de Nómina");
        alert.setHeaderText("Generando reporte de nómina");
        alert.setContentText("Esta funcionalidad será implementada próximamente.");
        alert.showAndWait();
    }
    
    /**
     * Generates a payment history report
     */
    private void generatePaymentHistoryReport() {
        // Prompt user to select an employee or generate for all
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historial de Pagos");
        alert.setHeaderText("Generando reporte de historial de pagos");
        alert.setContentText("Esta funcionalidad será implementada próximamente.");
        alert.showAndWait();
    }
    
    /**
     * Generates a department report
     */
    private void generateDepartmentReport() {
        // This would generate a report showing employee counts by department
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Empleados por Departamento");
        alert.setHeaderText("Generando reporte de empleados por departamento");
        alert.setContentText("Esta funcionalidad será implementada próximamente.");
        alert.showAndWait();
    }
    
    /**
     * Generates a birthday report
     */
    private void generateBirthdayReport() {
        // This would generate a report of employees with birthdays in the current month
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cumpleaños del Mes");
        alert.setHeaderText("Generando reporte de cumpleaños del mes");
        alert.setContentText("Esta funcionalidad será implementada próximamente.");
        alert.showAndWait();
    }
}
