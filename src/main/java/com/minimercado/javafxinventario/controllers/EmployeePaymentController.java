package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.EmployeeDAO;
import com.minimercado.javafxinventario.modules.Employee;
import com.minimercado.javafxinventario.modules.EmployeePayment;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Controlador para el registro de pagos a empleados.
 */
public class EmployeePaymentController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeePositionLabel;
    @FXML private Label baseSalaryLabel;
    
    @FXML private DatePicker paymentDatePicker;
    @FXML private ComboBox<String> paymentTypeCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private ComboBox<String> periodCombo;
    @FXML private TextField amountField;
    @FXML private TextField referenceField;
    @FXML private TextArea descriptionArea;
    @FXML private Label statusLabel;

    private Employee employee;
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        // Configurar opciones para los combos
        paymentTypeCombo.setItems(FXCollections.observableArrayList(
                "Salario", "Bono", "Comisión", "Adelanto", "Vacaciones", "Aguinaldo", "Otro"
        ));
        
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Efectivo", "Transferencia", "Cheque", "Depósito Bancario", "Otro"
        ));
        
        // Configurar fecha actual
        paymentDatePicker.setValue(LocalDate.now());
        
        // Generar periodos para el combo (últimos 12 meses)
        generatePeriods();
        
        // Seleccionar valores por defecto
        paymentTypeCombo.setValue("Salario");
        paymentMethodCombo.setValue("Efectivo");
        periodCombo.setValue(getCurrentMonthPeriod());
    }

    /**
     * Inicializa los datos del empleado.
     * @param employee Empleado seleccionado
     */
    public void initData(Employee employee) {
        this.employee = employee;
        
        employeeNameLabel.setText(employee.getFullName());
        employeePositionLabel.setText(employee.getPosition());
        baseSalaryLabel.setText(String.format("$%.2f", employee.getBaseSalary()));
        
        // Predefinir el monto con el salario base
        amountField.setText(String.valueOf(employee.getBaseSalary()));
    }

    /**
     * Genera las opciones de periodos para el combo.
     */
    private void generatePeriods() {
        String[] periods = new String[12];
        YearMonth current = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        
        for (int i = 0; i < 12; i++) {
            periods[i] = current.format(formatter);
            current = current.minusMonths(1);
        }
        
        periodCombo.setItems(FXCollections.observableArrayList(periods));
    }

    /**
     * Obtiene el periodo del mes actual.
     * @return String con el periodo actual en formato "Mes Año"
     */
    private String getCurrentMonthPeriod() {
        return YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    /**
     * Maneja el evento de registrar un pago.
     */
    @FXML
    private void handleRegisterPayment() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Crear objeto de pago
            EmployeePayment payment = new EmployeePayment();
            payment.setEmployeeId(employee.getId());
            payment.setPaymentDate(paymentDatePicker.getValue());
            payment.setAmount(Double.parseDouble(amountField.getText().trim()));
            payment.setPaymentType(paymentTypeCombo.getValue());
            payment.setPaymentMethod(paymentMethodCombo.getValue());
            payment.setPeriod(periodCombo.getValue());
            payment.setDescription(descriptionArea.getText().trim());
            payment.setReferenceNumber(referenceField.getText().trim());
            
            // Guardar el pago
            boolean success = employeeDAO.insertEmployeePayment(payment);
            
            if (success) {
                showInformationMessage("Pago Registrado", 
                        "El pago a " + employee.getFullName() + " ha sido registrado correctamente.");
                closeWindow();
            } else {
                statusLabel.setText("Error: No se pudo registrar el pago");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: El monto debe ser un número válido");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento de cancelar el registro.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Cierra la ventana actual.
     */
    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    /**
     * Valida los campos requeridos del formulario.
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (paymentDatePicker.getValue() == null) {
            errorMessage.append("- Fecha de pago es requerida\n");
        }
        
        if (amountField.getText().trim().isEmpty()) {
            errorMessage.append("- Monto es requerido\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    errorMessage.append("- El monto debe ser mayor que cero\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- El monto debe ser un número válido\n");
            }
        }
        
        if (paymentTypeCombo.getValue() == null) {
            errorMessage.append("- Tipo de pago es requerido\n");
        }
        
        if (paymentMethodCombo.getValue() == null) {
            errorMessage.append("- Método de pago es requerido\n");
        }
        
        if (errorMessage.length() > 0) {
            statusLabel.setText("Error: Verifique los campos requeridos");
            showErrorMessage("Error de validación", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    /**
     * Muestra un mensaje de información.
     * @param title Título del mensaje
     * @param message Contenido del mensaje
     */
    private void showInformationMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
