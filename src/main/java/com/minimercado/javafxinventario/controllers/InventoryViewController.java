package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.InventoryMovement;
import com.minimercado.javafxinventario.modules.InventoryModule;
import com.minimercado.javafxinventario.modules.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Pagination;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.CheckBox;

public class InventoryViewController {
    @FXML private HBox searchBox;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> thresholdColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    
    @FXML private TextField idField;
    @FXML private TextField skuField; // Added
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo; // Added
    @FXML private TextArea descriptionArea; // Added
    @FXML private ComboBox<String> supplierCombo; // Added
    @FXML private TextField purchasePriceField; // Added
    @FXML private TextField sellingPriceField; // Added (replaces priceField)
    @FXML private TextField stockField;
    @FXML private TextField reorderLevelField; // Added (replaces thresholdField)
    @FXML private TextField discountField; // Added
    @FXML private TextField locationField; // Added
    @FXML private DatePicker expirationDatePicker; // Added
    @FXML private CheckBox activeCheckbox; // Added
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    
    // Paneles de opciones
    @FXML private VBox agregarPane;
    @FXML private VBox actualizarPane;
    @FXML private VBox eliminarPane;
    @FXML private VBox searchPane;
    
    // Panel búsqueda general
    @FXML private TextField searchFieldBuscar;
    @FXML private TableView<Product> searchResultsTable;
    @FXML private TableColumn<Product, String> searchIdColumn;
    @FXML private TableColumn<Product, String> searchNameColumn;
    @FXML private TableColumn<Product, String> searchStockColumn;
    @FXML private TableColumn<Product, String> searchThresholdColumn;
    @FXML private TableColumn<Product, String> searchPriceColumn;
    @FXML private Label statusLabelBuscar;
    
    // Panel de actualización
    @FXML private TextField searchFieldUpdate;
    @FXML private TableView<Product> updateResultsTable;
    @FXML private TableColumn<Product, String> updateIdColumn;
    @FXML private TableColumn<Product, String> updateNameColumn;
    @FXML private TextField newPriceField;
    @FXML private Label statusLabelUpdate;
    
    // Panel de eliminación
    @FXML private TextField searchFieldDelete;
    @FXML private TableView<Product> deleteResultsTable;
    @FXML private TableColumn<Product, String> deleteIdColumn;
    
    @FXML private Label statusLabelDelete;
    
    @FXML private StackPane optionsStack;
    @FXML private Pagination pagination;
    
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private InventoryModule inventoryModule = InventoryModule.getInstance();
    
    // Nuevos campos para "Editar Producto"
    @FXML private TextField searchEditField;
    @FXML private TableView<Product> editProductsTable;
    @FXML private TableColumn<Product, String> editBarcodeColumn;
    @FXML private TableColumn<Product, String> editNameColumn;
    @FXML private TextField editNameField;
    @FXML private TextField editPurchasePriceField;
    @FXML private TextField editSellingPriceField;
    @FXML private TextField editStockField;
    @FXML private TextField editReorderLevelField;

    // Nuevos campos para "Eliminar Producto"
    @FXML private TextField searchDeleteField;
    @FXML private TableView<Product> deleteProductsTable;
    @FXML private TableColumn<Product, String> deleteBarcodeColumn;
    @FXML private TableColumn<Product, String> deleteNameColumn;
    @FXML private TableColumn<Product, Integer> deleteStockColumn;
    @FXML private TableColumn<Product, Double> deletePriceColumn;
    @FXML private TableColumn<Product, String> deleteSupplierColumn;

    // Nuevos campos para "Stock Bajo"
    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> lowStockBarcodeColumn;
    @FXML private TableColumn<Product, String> lowStockNameColumn;
    @FXML private TableColumn<Product, Integer> lowStockQuantityColumn;
    @FXML private TableColumn<Product, Integer> lowStockReorderColumn;
    @FXML private TableColumn<Product, String> lowStockSupplierColumn;

    // Nuevos campos para "Movimientos"
    @FXML private TextField searchMovementsField;
    @FXML private TableView<InventoryMovement> movementsTable;
    @FXML private TableColumn<InventoryMovement, String> movementDateColumn;
    @FXML private TableColumn<InventoryMovement, String> movementTypeColumn;
    @FXML private TableColumn<InventoryMovement, Integer> movementQuantityColumn;
    @FXML private TableColumn<InventoryMovement, String> movementReferenceColumn;

    @FXML
    public void initialize() {
        // Inicialización de la tabla principal
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        stockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStockQuantity()).asObject());
        thresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReorderLevel()).asObject());
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asObject());
        productsTable.setItems(productList);
        loadProducts();
        
        // Inicialización de columnas para actualización (only if injected)
        if(updateIdColumn != null) {
            updateIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        }
        if(updateNameColumn != null) {
            updateNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        // Inicialización de columnas para eliminación, validando inyección desde el FXML
        if(deleteIdColumn == null || deleteNameColumn == null) {
            System.err.println("Verificar fx:id de las columnas de eliminación.");
        } else {
            deleteIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
            deleteNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        // Inicialización de columnas para el panel de búsqueda
        if(searchPane != null) {
            searchIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
            searchNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
            searchStockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStockQuantity()).asString());
            searchThresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReorderLevel()).asString());
            searchPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asString());
        }
        setupPagination();
        // Inicializar columnas y tablas de nuevos paneles
        if(editProductsTable != null) {
            editBarcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            editNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if(deleteProductsTable != null) {
            deleteBarcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            deleteNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            deleteStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
            deletePriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
            deleteSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        }
        if(lowStockTable != null) {
            lowStockBarcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            lowStockNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            lowStockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
            lowStockReorderColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
            lowStockSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
            
            // Load low stock products on initialization
            handleRefreshLowStock();
        }
        if(movementsTable != null) {
            movementDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            movementTypeColumn.setCellValueFactory(new PropertyValueFactory<>("movementType"));
            movementQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            movementReferenceColumn.setCellValueFactory(new PropertyValueFactory<>("reference"));
        }

        // Initialize supplier and category combos
        if(supplierCombo != null) {
            // Populate with suppliers from database
            List<Supplier> suppliers = inventoryDAO.getAllSuppliers();
            ObservableList<String> supplierNames = FXCollections.observableArrayList();
            for(Supplier supplier : suppliers) {
                supplierNames.add(supplier.getName());
            }
            supplierCombo.setItems(supplierNames);
        }
        
        if(categoryCombo != null) {
            // Populate with distinct categories from database
            List<String> categories = inventoryDAO.getDistinctCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
        }
    }
    
    private void loadProducts() {
        productList.setAll(inventoryDAO.getAllProducts());
    }
    
    private void setupPagination() {
        if(pagination != null) {
            pagination.setPageFactory(pageIndex -> {
                loadPage(pageIndex);
                return productsTable; 
            });
        }
    }

    private void loadPage(int pageIndex) {
        // ...implementación simple de paginación...
    }
    
    // Método genérico para actualizar resultados de búsqueda en un TableView según un TextField
    private void updateSearchResults(TextField queryField, TableView<Product> table, Label statusLabel, String emptyMsg, String notFoundMsg) {
        String query = queryField.getText().trim();
        if(query.isEmpty()){
            statusLabel.setText(emptyMsg);
            table.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        statusLabel.setText(results.isEmpty() ? notFoundMsg : "");
        table.setItems(results);
    }
    
    // Métodos de manejo de productos
    @FXML
    protected void handleAddProduct() {
        if(idField == null) { // added null check
            mostrarError("Campo ID no inyectado. Verifique el fx:id en FXML.");
            return;
        }
        try {
            // Validate required fields
            if(idField.getText().trim().isEmpty() || 
               nameField.getText().trim().isEmpty() ||
               stockField.getText().trim().isEmpty() ||
               reorderLevelField.getText().trim().isEmpty() ||
               sellingPriceField.getText().trim().isEmpty()) {
                mostrarError("Los campos con * son obligatorios");
                return;
            }
            
            // Create product object with all form fields
            Product p = new Product();
            p.setBarcode(idField.getText().trim());
            p.setSku(skuField.getText().trim());
            p.setName(nameField.getText().trim());
            p.setCategory(categoryCombo.getValue() != null ? categoryCombo.getValue() : "");
            p.setDescription(descriptionArea.getText().trim());
            p.setSupplier(supplierCombo.getValue() != null ? supplierCombo.getValue() : "");
            
            // Parse numeric fields
            try {
                p.setPurchasePrice(purchasePriceField.getText().trim().isEmpty() ? 
                    0.0 : Double.parseDouble(purchasePriceField.getText().trim()));
                p.setSellingPrice(Double.parseDouble(sellingPriceField.getText().trim()));
                p.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
                p.setReorderLevel(Integer.parseInt(reorderLevelField.getText().trim()));
                p.setDiscount(discountField.getText().trim().isEmpty() ? 
                    0.0 : Double.parseDouble(discountField.getText().trim()));
            } catch(NumberFormatException e) {
                mostrarError("Formato inválido en campos numéricos: " + e.getMessage());
                return;
            }
            
            // Set other fields
            p.setActive(activeCheckbox.isSelected());
            
            // Convert LocalDate to Date if expiration date is selected
            if(expirationDatePicker.getValue() != null) {
                LocalDate localDate = expirationDatePicker.getValue();
                Date expirationDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                p.setExpirationDate(expirationDate);
            }
            
            // Add product to database
            if(inventoryDAO.addProduct(p)) {
                mostrarMensaje("Producto agregado exitosamente");
                loadProducts();
                handleClearForm(); // Clear the form after successful add
            } else {
                mostrarError("No se pudo agregar el producto");
            }
        } catch(Exception e) {
            mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleUpdateProduct() {
        try {
            Product selected = productList.stream().filter(p -> p.getBarcode().equals(idField.getText())).findFirst().orElse(null);
            if (selected == null) {
                statusLabel.setText("Seleccione un producto para actualizar.");
                return;
            }
            selected.setName(nameField.getText());
            selected.setStockQuantity(Integer.parseInt(stockField.getText()));
            selected.setReorderLevel(Integer.parseInt(reorderLevelField.getText()));
            selected.setSellingPrice(Double.parseDouble(sellingPriceField.getText()));
            inventoryDAO.updateProduct(selected);
            loadProducts();
            statusLabel.setText("Producto actualizado exitosamente.");
        } catch(Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleRemoveProduct() {
        if(idField == null) { // added null check
            mostrarError("Campo ID no inyectado. Verifique el fx:id en FXML.");
            return;
        }
        Product selected = productList.stream().filter(p -> p.getBarcode().equals(idField.getText())).findFirst().orElse(null);
        if (selected == null) {
            statusLabel.setText("Seleccione un producto para eliminar.");
            return;
        }
        inventoryDAO.deleteProduct(selected.getBarcode());
        loadProducts();
        statusLabel.setText("Producto eliminado exitosamente.");
    }
    
    @FXML
    protected void handleSearchProduct() {
        String query = searchField.getText().trim();
        if(query.isEmpty()){
            loadProducts();
        } else {
            productList.setAll(inventoryDAO.searchProducts(query));
        }
    }
    
    // Manejo de paneles: ocultar todos y mostrar solo el deseado
    private void showOnly(VBox paneToShow) {
        agregarPane.setVisible(false);
        actualizarPane.setVisible(false);
        eliminarPane.setVisible(false);
        searchPane.setVisible(false);
        paneToShow.setVisible(true);
    }
    
    @FXML
    private void showAgregar() {
        showOnly(agregarPane);
    }
    
    @FXML
    private void showActualizar() {
        showOnly(actualizarPane);
    }
    
    @FXML
    private void showEliminar() {
        showOnly(eliminarPane);
    }
    
    @FXML
    private void showBuscar() {
        showOnly(searchPane);
    }
    
    // Manejo de búsqueda en el panel de actualización
    @FXML
    private void handleSearchUpdateProducts() {
        updateSearchResults(searchFieldUpdate, updateResultsTable, statusLabelUpdate,
                "Ingrese un criterio de búsqueda.", "No se encontraron productos.");
    }
    
    @FXML
    protected void handleUpdateProductPrice() {
        Product selected = updateResultsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            statusLabelUpdate.setText("Seleccione un producto de la lista.");
            return;
        }
        try {
            double newPrice = Double.parseDouble(newPriceField.getText().trim());
            selected.setSellingPrice(newPrice);
            inventoryDAO.updateProduct(selected);
            loadProducts();
            updateResultsTable.refresh();
            statusLabelUpdate.setText("Precio actualizado exitosamente.");
        } catch(Exception e) {
            statusLabelUpdate.setText("Error: " + e.getMessage());
        }
    }
    
    // Manejo de búsqueda en el panel de eliminación
    @FXML
    private void handleSearchDeleteProducts() {
        String query = searchFieldDelete.getText().trim();
        if(query.isEmpty()){
            statusLabelDelete.setText("Ingrese un criterio de búsqueda.");
            deleteResultsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        if(results.isEmpty()){
            statusLabelDelete.setText("No se encontraron productos.");
        } else {
            statusLabelDelete.setText("");
        }
        deleteResultsTable.setItems(results);
    }
    
    // Nuevo método para eliminar el producto seleccionado de la lista de eliminación
    @FXML
    protected void handleRemoveProductBySelection() {
        Product selected = deleteResultsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            statusLabelDelete.setText("Seleccione un producto de la lista.");
            return;
        }
        try {
            inventoryDAO.deleteProduct(selected.getBarcode());
            loadProducts(); // Se recarga la lista de productos actualizando el panel de agregar
            deleteResultsTable.getItems().remove(selected);
            statusLabelDelete.setText("Producto eliminado exitosamente.");
        } catch(Exception e) {
            statusLabelDelete.setText("Error: " + e.getMessage());
        }
    }
    
    // Método que ejecuta la búsqueda en el nuevo panel
    @FXML
    private void handleBuscarProducto() {
        String query = searchFieldBuscar.getText().trim();
        if(query.isEmpty()){
            statusLabelBuscar.setText("Ingrese un criterio de búsqueda.");
            searchResultsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        if(results.isEmpty()){
            statusLabelBuscar.setText("No se encontraron productos.");
        } else {
            statusLabelBuscar.setText("");
        }
        searchResultsTable.setItems(results);
    }

    // Métodos para "Editar Producto"
    @FXML
    private void handleSearchEditProduct() {
        String query = searchEditField.getText().trim();
        if(query.isEmpty()){
            editProductsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        editProductsTable.setItems(results);
    }
    @FXML
    private void handleUpdateSelectedProduct() {
        Product selected = editProductsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            mostrarError("Seleccione un producto para editar");
            return;
        }
        try {
            selected.setName(editNameField.getText());
            selected.setPurchasePrice(Double.parseDouble(editPurchasePriceField.getText()));
            selected.setSellingPrice(Double.parseDouble(editSellingPriceField.getText()));
            selected.setStockQuantity(Integer.parseInt(editStockField.getText()));
            selected.setReorderLevel(Integer.parseInt(editReorderLevelField.getText()));
            if(inventoryDAO.updateProduct(selected)) {
                mostrarMensaje("Producto actualizado exitosamente");
                loadProducts();
            } else {
                mostrarError("No se pudo actualizar el producto");
            }
        } catch(Exception e) {
            mostrarError("Error al actualizar: " + e.getMessage());
        }
    }
    @FXML
    private void handleCancelEdit() {
        // Limpia campos del panel de edición
        searchEditField.clear();
        editProductsTable.getItems().clear();
        // ...existing code para limpiar formulario de edición...
    }

    // Métodos para "Eliminar Producto"
    @FXML
    private void handleSearchDeleteProduct() {
        String query = searchDeleteField.getText().trim();
        if(query.isEmpty()){
            deleteProductsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        deleteProductsTable.setItems(results);
    }
    @FXML
    private void handleDeleteProduct() {
        Product selected = deleteProductsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            mostrarError("Seleccione un producto para eliminar");
            return;
        }
        if(inventoryDAO.deleteProduct(selected.getBarcode())) {
            mostrarMensaje("Producto eliminado exitosamente");
            loadProducts();
            deleteProductsTable.getItems().remove(selected);
        } else {
            mostrarError("No se pudo eliminar el producto");
        }
    }

    // Método para "Stock Bajo"
    @FXML
    private void handleRefreshLowStock() {
        try {
            // Use the DAO to get low stock products directly from the database
            List<Product> lowStockProducts = inventoryDAO.getLowStockProducts();
            
            // Convert to ObservableList and set to table
            ObservableList<Product> lowStock = FXCollections.observableArrayList(lowStockProducts);
            lowStockTable.setItems(lowStock);
            
            // Update status
            if (lowStock.isEmpty()) {
                statusLabel.setText("No hay productos con stock bajo");
            } else {
                statusLabel.setText("Se encontraron " + lowStock.size() + " productos con stock bajo");
            }
            
            // Debug output
            System.out.println("Cargados " + lowStock.size() + " productos con stock bajo");
            for (Product p : lowStock) {
                System.out.println("Producto bajo stock: " + p.getName() + ", Stock: " + p.getStockQuantity() + 
                                  ", Nivel reorden: " + p.getReorderLevel());
            }
        } catch (Exception e) {
            statusLabel.setText("Error al cargar productos con stock bajo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Métodos para "Movimientos"
    @FXML
    private void handleViewMovements() {
        try {
            // Modificado para usar un método que debe existir en InventoryDAO o crear stub
            ObservableList<InventoryMovement> movimientos = FXCollections.observableArrayList(
                inventoryDAO.getMovementsByProduct(searchMovementsField.getText().trim()));
            movementsTable.setItems(movimientos);
            if(movimientos.isEmpty()){
                mostrarMensaje("No se encontraron movimientos");
            }
        } catch (Exception e) {
            mostrarError("Error al buscar movimientos: " + e.getMessage());
        }
    }
    @FXML
    private void handleRegisterAdjustment() {
        // Mostrar diálogo para registrar ajuste (stub)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registrar Ajuste");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad para registrar ajuste no implementada.");
        alert.showAndWait();
    }
    @FXML
    private void handleExportMovements() {
        // Exportar movimientos a Excel (stub)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Movimientos");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad para exportar movimientos no implementada.");
        alert.showAndWait();
    }

    @FXML
    private void handleCreatePurchaseOrder() {
        // Implementación de la funcionalidad de crear orden de compra
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Crear Orden de Compra");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad para crear orden de compra no implementada.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleClearForm() {
        // Limpia los campos del formulario de agregar producto usando los campos definidos
        if(idField != null) idField.clear();
        if(skuField != null) skuField.clear();
        if(nameField != null) nameField.clear();
        if(categoryCombo != null) categoryCombo.setValue(null);
        if(descriptionArea != null) descriptionArea.clear();
        if(supplierCombo != null) supplierCombo.setValue(null);
        if(purchasePriceField != null) purchasePriceField.clear();
        if(sellingPriceField != null) sellingPriceField.clear();
        if(stockField != null) stockField.clear();
        if(reorderLevelField != null) reorderLevelField.clear();
        if(discountField != null) discountField.clear();
        if(locationField != null) locationField.clear();
        if(expirationDatePicker != null) expirationDatePicker.setValue(null);
        if(activeCheckbox != null) activeCheckbox.setSelected(true);
    }

    // Métodos de utilería para mensajes
    private void mostrarError(String mensaje) {
        statusLabel.setText("Error: " + mensaje);
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.showAndWait();
    }
    private void mostrarMensaje(String mensaje) {
        statusLabel.setText(mensaje);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.showAndWait();
    }
}
