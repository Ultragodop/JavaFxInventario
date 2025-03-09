package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SupplierController {
    // Campos del formulario
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

    // Tabla de proveedores
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> idColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Inicializar columnas
        idColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        contactColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContactName()));
        phoneColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhone()));
        emailColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        addressColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAddress()));
        supplierTable.setItems(supplierList);
        
        loadSuppliers();
    }
    
    private void loadSuppliers() {
        supplierList.setAll(inventoryDAO.getAllSuppliers());
    }
    
    @FXML
    private void handleAddSupplier() {
        try {
            Supplier supplier = new Supplier();
            supplier.setName(nameField.getText().trim());
            supplier.setContactName(contactField.getText().trim());
            supplier.setPhone(phoneField.getText().trim());
            supplier.setEmail(emailField.getText().trim());
            supplier.setAddress(addressField.getText().trim());
            supplier.setNotes(notesArea.getText().trim());
            
            if (inventoryDAO.addSupplier(supplier)) {
                showMessage("Proveedor agregado exitosamente.");
                loadSuppliers();
                clearForm();
            } else {
                showError("No se pudo agregar el proveedor.");
            }
        } catch (Exception e) {
            showError("Error agregando proveedor: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleccione un proveedor de la tabla.");
            return;
        }
        try {
            selected.setName(nameField.getText().trim());
            selected.setContactName(contactField.getText().trim());
            selected.setPhone(phoneField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setAddress(addressField.getText().trim());
            selected.setNotes(notesArea.getText().trim());
            
            if (inventoryDAO.updateSupplier(selected)) {
                showMessage("Proveedor actualizado exitosamente.");
                loadSuppliers();
                clearForm();
            } else {
                showError("No se pudo actualizar el proveedor.");
            }
        } catch (Exception e) {
            showError("Error actualizando proveedor: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Seleccione un proveedor de la tabla.");
            return;
        }
        try {
            if (inventoryDAO.deleteSupplier(selected.getId())) {
                showMessage("Proveedor eliminado exitosamente.");
                loadSuppliers();
                clearForm();
            } else {
                showError("No se pudo eliminar el proveedor.");
            }
        } catch (Exception e) {
            showError("Error eliminando proveedor: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        nameField.clear();
        contactField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        notesArea.clear();
    }
    
    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
    
    private void showMessage(String message) {
        statusLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}
