package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Optional;

public class SuppliersController {

    // Table view and columns
    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactNameColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> lastOrderColumn;
    
    // Form fields
    @FXML private TextField nameField;
    @FXML private TextField contactNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private TextArea notesField;
    @FXML private CheckBox activeCheckbox;
    
    // Search field
    @FXML private TextField searchField;
    
    // Status label
    @FXML private Label statusLabel;
    
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
    private Supplier selectedSupplier;
    
    @FXML
    public void initialize() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactNameColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Custom cell factory for formatting dates
        lastOrderColumn.setCellValueFactory(new PropertyValueFactory<>("lastOrderDate"));
        lastOrderColumn.setCellFactory(column -> new TableCell<Supplier, String>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Supplier supplier = (Supplier) getTableRow().getItem();
                    if (supplier.getLastOrderDate() != null) {
                        setText(dateFormat.format(supplier.getLastOrderDate()));
                    } else {
                        setText("Sin pedidos");
                    }
                }
            }
        });
        
        // Set table data source
        suppliersTable.setItems(suppliersList);
        
        // Load suppliers
        loadSuppliers();
        
        // Setup table row selection handler
        suppliersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showSupplierDetails(newSelection);
                    selectedSupplier = newSelection;
                }
            });
        
        // Setup search field listener
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadSuppliers();
            } else {
                filterSuppliers(newValue);
            }
        });
        
        // Set initial focus
        nameField.requestFocus();
    }
    
    /**
     * Loads all suppliers from the database
     */
    private void loadSuppliers() {
        suppliersList.setAll(inventoryDAO.getAllSuppliers());
    }
    
    /**
     * Filters the suppliers list based on search term
     * @param searchTerm Term to search for
     */
    private void filterSuppliers(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            loadSuppliers();
            return;
        }
        
        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        
        // Get all suppliers and filter in memory (alternative to a database query)
        ObservableList<Supplier> filteredList = FXCollections.observableArrayList();
        for (Supplier supplier : inventoryDAO.getAllSuppliers()) {
            if (supplier.getName().toLowerCase().contains(lowerCaseSearchTerm) || 
                (supplier.getContactName() != null && supplier.getContactName().toLowerCase().contains(lowerCaseSearchTerm)) ||
                (supplier.getPhone() != null && supplier.getPhone().contains(lowerCaseSearchTerm)) ||
                (supplier.getEmail() != null && supplier.getEmail().toLowerCase().contains(lowerCaseSearchTerm))) {
                filteredList.add(supplier);
            }
        }
        
        suppliersList.setAll(filteredList);
    }
    
    /**
     * Displays supplier details in the form
     * @param supplier Supplier to display
     */
    private void showSupplierDetails(Supplier supplier) {
        nameField.setText(supplier.getName());
        contactNameField.setText(supplier.getContactName());
        phoneField.setText(supplier.getPhone());
        emailField.setText(supplier.getEmail());
        addressField.setText(supplier.getAddress());
        notesField.setText(supplier.getNotes());
        activeCheckbox.setSelected(supplier.isActive());
    }
    
    /**
     * Clears the form fields
     */
    private void clearForm() {
        nameField.clear();
        contactNameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        notesField.clear();
        activeCheckbox.setSelected(true);
        selectedSupplier = null;
    }
    
    /**
     * Creates a Supplier object from form fields
     * @return New Supplier object
     */
    private Supplier createSupplierFromForm() {
        Supplier supplier = new Supplier();
        supplier.setName(nameField.getText().trim());
        supplier.setContactName(contactNameField.getText().trim());
        supplier.setPhone(phoneField.getText().trim());
        supplier.setEmail(emailField.getText().trim());
        supplier.setAddress(addressField.getText().trim());
        supplier.setNotes(notesField.getText().trim());
        supplier.setActive(activeCheckbox.isSelected());
        
        if (selectedSupplier != null) {
            supplier.setId(selectedSupplier.getId());
            supplier.setLastOrderDate(selectedSupplier.getLastOrderDate());
        }
        
        return supplier;
    }
    
    /**
     * Validates supplier form data
     * @return true if data is valid, false otherwise
     */
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("El nombre del proveedor es obligatorio");
            return false;
        }
        return true;
    }
    
    /**
     * Shows an error message
     * @param message Error message to show
     */
    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation dialog
     * @param title Dialog title
     * @param message Dialog message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Handles the Save button click
     */
    @FXML
    private void handleSaveSupplier() {
        if (!validateForm()) {
            return;
        }
        
        Supplier supplier = createSupplierFromForm();
        
        try {
            boolean success;
            if (selectedSupplier == null) {
                // Add new supplier
                success = inventoryDAO.addSupplier(supplier);
                if (success) {
                    statusLabel.setText("Proveedor agregado exitosamente");
                }
            } else {
                // Update existing supplier
                success = inventoryDAO.updateSupplier(supplier);
                if (success) {
                    statusLabel.setText("Proveedor actualizado exitosamente");
                }
            }
            
            if (success) {
                loadSuppliers();
                clearForm();
            } else {
                showError("No se pudo guardar el proveedor");
            }
        } catch (Exception e) {
            showError("Error al guardar: " + e.getMessage());
        }
    }
    
    /**
     * Handles the New button click
     */
    @FXML
    private void handleNewSupplier() {
        clearForm();
        nameField.requestFocus();
    }
    
    /**
     * Handles the Delete button click
     */
    @FXML
    private void handleDeleteSupplier() {
        if (selectedSupplier == null) {
            showError("No hay proveedor seleccionado");
            return;
        }
        
        if (showConfirmation("Eliminar Proveedor", 
                "¿Está seguro de eliminar el proveedor '" + selectedSupplier.getName() + "'?")) {
            try {
                boolean success = inventoryDAO.deleteSupplier(selectedSupplier.getId());
                if (success) {
                    statusLabel.setText("Proveedor eliminado exitosamente");
                    loadSuppliers();
                    clearForm();
                } else {
                    showError("No se pudo eliminar el proveedor");
                }
            } catch (Exception e) {
                showError("Error al eliminar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles the Clear button click
     */
    @FXML
    private void handleClearForm() {
        clearForm();
    }
}
