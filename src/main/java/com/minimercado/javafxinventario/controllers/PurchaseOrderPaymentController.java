package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.PurchasePaymentDAO;
import com.minimercado.javafxinventario.modules.PurchaseOrder;
import com.minimercado.javafxinventario.modules.PurchasePayment;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controlador para el registro de pagos a órdenes de compra.
 */
public class PurchaseOrderPaymentController {

    @FXML private Label orderIdLabel;
    @FXML private Label supplierLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label balanceLabel;
    
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField referenceField;
    @FXML private CheckBox completePaymentCheckBox;
    @FXML private TextArea notesArea;
    
    @FXML private Label statusLabel;
    
    private PurchaseOrder purchaseOrder;
    private PurchasePaymentDAO paymentDAO = new PurchasePaymentDAO();
    private Double paidAmount = 0.0;

    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        // Configurar opciones para el combo de método de pago
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Efectivo", "Transferencia", "Cheque", "Tarjeta de Débito", "Tarjeta de Crédito", "Otro"
        ));
        paymentMethodCombo.setValue("Efectivo");
        
        // Configurar fecha actual por defecto
        paymentDatePicker.setValue(LocalDate.now());
        
        // Configurar listener para el checkBox
        completePaymentCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && purchaseOrder != null) {
                // Si se marca como pago completo, establecer el monto pendiente
                amountField.setText(String.valueOf(purchaseOrder.getTotalAmount() - paidAmount));
            }
        });
    }

    /**
     * Inicializa los datos de la orden de compra.
     * @param order Orden de compra seleccionada
     */
    public void initData(PurchaseOrder order) {
        this.purchaseOrder = order;
        
        // Calcular el monto ya pagado
        calculatePaidAmount();
        
        // Mostrar información de la orden
        orderIdLabel.setText(String.valueOf(order.getId()));
        supplierLabel.setText(order.getSupplierName());
        totalAmountLabel.setText(String.format("$%.2f", order.getTotalAmount()));
        
        double balance = order.getTotalAmount() - paidAmount;
        balanceLabel.setText(String.format("$%.2f", balance));
        
        // Establecer valor por defecto para el campo de monto
        amountField.setText(String.valueOf(balance));
        
        // Si el balance es cero, desactivar el formulario (ya está pagada)
        if (balance <= 0) {
            disableForm();
            statusLabel.setText("Esta orden ya ha sido pagada completamente");
        }
    }

    /**
     * Calcula el monto ya pagado de la orden.
     */
    private void calculatePaidAmount() {
        if (purchaseOrder == null) return;
        
        paidAmount = 0.0;
        // Obtener pagos previos
        var payments = paymentDAO.getPaymentsByPurchaseOrder(purchaseOrder.getId());
        for (PurchasePayment payment : payments) {
            paidAmount += payment.getAmount();
        }
    }

    /**
     * Desactiva el formulario si la orden ya está pagada.
     */
    private void disableForm() {
        paymentDatePicker.setDisable(true);
        amountField.setDisable(true);
        paymentMethodCombo.setDisable(true);
        referenceField.setDisable(true);
        completePaymentCheckBox.setDisable(true);
        notesArea.setDisable(true);
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
            PurchasePayment payment = new PurchasePayment();
            payment.setPurchaseOrderId(purchaseOrder.getId());
            payment.setPaymentDate(paymentDatePicker.getValue());
            payment.setAmount(Double.parseDouble(amountField.getText().trim()));
            payment.setOriginalAmount(purchaseOrder.getTotalAmount());
            payment.setPaymentMethod(paymentMethodCombo.getValue());
            payment.setCompletePayment(completePaymentCheckBox.isSelected());
            payment.setReferenceNumber(referenceField.getText().trim());
            payment.setNotes(notesArea.getText().trim());
            payment.setCreatedBy("Usuario"); // Esto debería venir del sistema de autenticación
            
            // Guardar el pago
            boolean success = paymentDAO.insertPayment(payment);
            
            if (success) {
                showInformationMessage("Pago Registrado", 
                        "El pago a la orden #" + purchaseOrder.getId() + " ha sido registrado correctamente.");
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
                double balance = purchaseOrder.getTotalAmount() - paidAmount;
                
                if (amount <= 0) {
                    errorMessage.append("- El monto debe ser mayor que cero\n");
                } else if (amount > balance) {
                    errorMessage.append("- El monto no puede ser mayor que el saldo pendiente\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- El monto debe ser un número válido\n");
            }
        }
        
        if (paymentMethodCombo.getValue() == null) {
            errorMessage.append("- Método de pago es requerido\n");
        }
        
        if (errorMessage.length() > 0) {
            showErrorMessage("Error de validación", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    /**
     * Cierra la ventana actual.
     */
    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
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
