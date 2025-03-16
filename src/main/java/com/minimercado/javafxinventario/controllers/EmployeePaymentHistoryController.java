package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.EmployeeDAO;
import com.minimercado.javafxinventario.modules.Employee;
import com.minimercado.javafxinventario.modules.EmployeePayment;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para el historial de pagos a empleados.
 */
public class EmployeePaymentHistoryController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeePositionLabel;
    @FXML private Label totalPaidLabel;
    
    @FXML private TableView<EmployeePayment> paymentsTable;
    @FXML private TableColumn<EmployeePayment, LocalDate> paymentDateColumn;
    @FXML private TableColumn<EmployeePayment, String> paymentTypeColumn;
    @FXML private TableColumn<EmployeePayment, Double> amountColumn;
    @FXML private TableColumn<EmployeePayment, String> paymentMethodColumn;
    @FXML private TableColumn<EmployeePayment, String> periodColumn;
    @FXML private TableColumn<EmployeePayment, String> descriptionColumn;
    @FXML private TableColumn<EmployeePayment, String> reconcileColumn;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> paymentTypeCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button searchButton;
    
    @FXML private Label statusLabel;
    
    private EmployeeDAO paymentDAO = new EmployeeDAO();
    private ObservableList<EmployeePayment> paymentList = FXCollections.observableArrayList();
    private Employee selectedEmployee;

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        reconcileColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isReconciled() ? "Sí" : "No"));
        
        // Formatear la columna de fecha
        paymentDateColumn.setCellFactory(column -> new TableCell<EmployeePayment, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        // Formatear la columna de monto
        amountColumn.setCellFactory(column -> new TableCell<EmployeePayment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Configurar opciones para los combos de filtro
        paymentTypeCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Salario", "Bono", "Adelanto", "Comisión", "Aguinaldo", "Otro"));
        paymentTypeCombo.setValue("Todos");
        
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Efectivo", "Transferencia", "Cheque", "Depósito", "Otro"));
        paymentMethodCombo.setValue("Todos");
        
        // Establecer fechas por defecto (último mes)
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        startDatePicker.setValue(oneMonthAgo);
        endDatePicker.setValue(now);
        
        // Configurar tabla
        paymentsTable.setItems(paymentList);
    }
    
    /**
     * Inicializa los datos del empleado seleccionado.
     * @param employee Empleado seleccionado
     */
    public void initData(Employee employee) {
        this.selectedEmployee = employee;
        
        if (employee != null) {
            // Mostrar información del empleado
            employeeNameLabel.setText(employee.getFirstName() + " " + employee.getLastName());
            employeePositionLabel.setText(employee.getPosition());
            
            // Cargar pagos del empleado
            loadPayments();
        }
    }
    
    /**
     * Carga los pagos del empleado según los filtros aplicados.
     */
    private void loadPayments() {
        if (selectedEmployee == null) return;
        
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String paymentType = paymentTypeCombo.getValue().equals("Todos") ? null : paymentTypeCombo.getValue();
            String paymentMethod = paymentMethodCombo.getValue().equals("Todos") ? null : paymentMethodCombo.getValue();
            
            // Obtener pagos filtrados
            List<EmployeePayment> payments = paymentDAO.getEmployeePaymentsByFilters(
                    selectedEmployee.getId(), startDate, endDate, paymentType, paymentMethod);
            
            paymentList.setAll(payments);
            
            // Calcular y mostrar el total pagado
            double totalPaid = payments.stream()
                    .mapToDouble(EmployeePayment::getAmount)
                    .sum();
            totalPaidLabel.setText(String.format("$%.2f", totalPaid));
            
            updateStatusLabel("Se encontraron " + payments.size() + " pagos");
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar pagos", e.getMessage());
        }
    }
    
    /**
     * Maneja el evento del botón de búsqueda.
     */
    @FXML
    private void handleSearch() {
        loadPayments();
    }
    
    /**
     * Maneja el evento para exportar el historial de pagos a CSV.
     */
    @FXML
    private void handleExportToCSV() {
        if (paymentList.isEmpty()) {
            updateStatusLabel("No hay datos para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Historial de Pagos");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        fileChooser.setInitialFileName("historial_pagos_" + 
                selectedEmployee.getLastName().toLowerCase() + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        
        Stage stage = (Stage) paymentsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Escribir encabezado
                writer.println("Fecha,Tipo,Monto,Método,Período,Descripción,Conciliado");
                
                // Escribir datos
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (EmployeePayment payment : paymentList) {
                    writer.println(
                            payment.getPaymentDate().format(dateFormat) + "," +
                            escapeCSV(payment.getPaymentType()) + "," +
                            payment.getAmount() + "," +
                            escapeCSV(payment.getPaymentMethod()) + "," +
                            escapeCSV(payment.getPeriod()) + "," +
                            escapeCSV(payment.getDescription()) + "," +
                            (payment.isReconciled() ? "Sí" : "No")
                    );
                }
                
                updateStatusLabel("Datos exportados a: " + file.getAbsolutePath());
            } catch (IOException e) {
                showErrorMessage("Error al exportar", e.getMessage());
            }
        }
    }
    
    /**
     * Escapa caracteres especiales para formato CSV.
     * @param text Texto a escapar
     * @return Texto escapado
     */
    private String escapeCSV(String text) {
        if (text == null) return "";
        
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = text.replace("\"", "\"\"");
            return "\"" + text + "\"";
        }
        return text;
    }
    
    /**
     * Maneja el evento para generar un resumen por tipo de pago.
     */
    @FXML
    private void handleGenerateSummary() {
        if (paymentList.isEmpty()) {
            updateStatusLabel("No hay datos para generar resumen");
            return;
        }
        
        // Agrupar pagos por tipo y calcular el total para cada tipo
        Map<String, Double> summary = paymentList.stream()
                .collect(Collectors.groupingBy(
                        EmployeePayment::getPaymentType,
                        Collectors.summingDouble(EmployeePayment::getAmount)
                ));
        
        // Crear un diálogo para mostrar el resumen
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Resumen de Pagos por Tipo");
        dialog.setHeaderText("Empleado: " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
        
        // Crear una tabla para el resumen
        TableView<Map.Entry<String, Double>> summaryTable = new TableView<>();
        
        // Configurar columnas
        TableColumn<Map.Entry<String, Double>, String> typeColumn = new TableColumn<>("Tipo de Pago");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
        
        TableColumn<Map.Entry<String, Double>, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValue()).asObject());
        totalColumn.setCellFactory(column -> new TableCell<Map.Entry<String, Double>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Añadir columnas a la tabla
        summaryTable.getColumns().addAll(typeColumn, totalColumn);
        
        // Configurar datos de la tabla
        summaryTable.setItems(FXCollections.observableArrayList(summary.entrySet()));
        
        // Añadir la tabla al diálogo
        dialog.getDialogPane().setContent(summaryTable);
        dialog.getDialogPane().setPrefWidth(400);
        dialog.getDialogPane().setPrefHeight(300);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Maneja el evento para cerrar la ventana.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) paymentsTable.getScene().getWindow();
        stage.close();
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
}
