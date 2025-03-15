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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.CheckBox;
import java.util.ArrayList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import java.io.IOException;

public class InventoryViewController {
    @FXML private HBox searchBox;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> idColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> thresholdColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, java.util.Date> expirationDateColumn; // Nueva columna de fecha de vencimiento
    
    // New fields for filter
    @FXML private ComboBox<String> filterCategoryCombo;
    
    @FXML private TextField idField;
    @FXML private TextField skuField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> supplierCombo;
    @FXML private TextField purchasePriceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField stockField;
    @FXML private TextField reorderLevelField;
    @FXML private TextField discountField;
    @FXML private TextField locationField;
    @FXML private DatePicker expirationDatePicker;
    @FXML private CheckBox activeCheckbox;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label totalProductsLabel;
    
    // New fields for bulk operations
    @FXML private ComboBox<String> bulkCategoryCombo;
    @FXML private ComboBox<String> adjustmentTypeCombo;
    @FXML private TextField adjustmentValueField;
    @FXML private TableView<Product> bulkUpdatePreviewTable;
    // Eliminar las anotaciones @FXML de las columnas que creamos programáticamente
    private TableColumn<Product, String> bulkBarcodeColumn;
    private TableColumn<Product, String> bulkNameColumn;
    private TableColumn<Product, Double> bulkCurrentPriceColumn;
    private TableColumn<Product, Double> bulkNewPriceColumn;
    @FXML private Label importStatusLabel;
    
    // New fields for status bar
    @FXML private Label inventorySummaryLabel;
    @FXML private Label lastUpdatedLabel;
    
    // New fields for reports tab
    @FXML private Label inventoryValueLabel;
    @FXML private Label criticalStockCountLabel;
    @FXML private Label noMovementCountLabel;
    
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
    
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private InventoryModule inventoryModule = InventoryModule.getInstance();
    
    // Nuevos campos para "Editar Producto"
    @FXML private TextField searchEditField;
    @FXML private TableView<Product> editProductsTable;
    @FXML private TableColumn<Product, String> editBarcodeColumn;
    @FXML private TableColumn<Product, String> editNameColumn;
    @FXML private TextField editBarcodeField; // Agregar campo que faltaba
    @FXML private TextField editNameField;
    @FXML private TextField editPurchasePriceField;
    @FXML private TextField editSellingPriceField;
    @FXML private TextField editStockField;
    @FXML private TextField editReorderLevelField;
    @FXML private Button addProductFromEditButton; // New button for adding product from edit section
    
    // Variable para almacenar el producto seleccionado para edición
    private Product selectedProductForEdit;

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
        // Initialize main product table columns
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        barcodeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBarcode()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        stockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStockQuantity()).asObject());
        thresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReorderLevel()).asObject());
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asObject());
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        supplierColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplier()));
        
        // Verificar que expirationDateColumn no sea null antes de configurarla
        if (expirationDateColumn != null) {
            expirationDateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getExpirationDate()));
                
            // Formatear la columna de fecha para mostrarla legiblemente
            expirationDateColumn.setCellFactory(column -> {
                return new TableCell<Product, Date>() {
                    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(format.format(item));
                        }
                    }
                };
            });
        } else {
            System.err.println("WARNING: expirationDateColumn is null. Check if fx:id is properly set in FXML.");
        }
        
        productsTable.setItems(productList);
        
        // Initialize filter category combo
        if (filterCategoryCombo != null) {
            List<String> categories = inventoryDAO.getDistinctCategories();
            filterCategoryCombo.setItems(FXCollections.observableArrayList(categories));
        }
        
        // Initialize bulk operations category combo
        if (bulkCategoryCombo != null) {
            List<String> categories = inventoryDAO.getDistinctCategories();
            bulkCategoryCombo.setItems(FXCollections.observableArrayList(categories));
        }
        
        // Initialize adjustment type combo
        if (adjustmentTypeCombo != null) {
            adjustmentTypeCombo.setItems(FXCollections.observableArrayList("Porcentaje (%)", "Monto Fijo"));
        }
        
        // Load products
        loadProducts();
        
        // Update report values
        updateReportValues();

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
        // Inicializar columnas y tablas de nuevos paneles
        if(editProductsTable != null) {
            editBarcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            editNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            
            // Mejorar el manejo de eventos de selección
            editProductsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    fillEditFields(newSelection);
                }
            });
            
            // Add double-click event handler
            editProductsTable.setRowFactory(tv -> {
                TableRow<Product> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        fillEditFields(row.getItem());
                    }
                });
                return row;
            });
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

        // Set last updated timestamp
        updateLastUpdatedTimestamp();
        
        // Initialize bulk update preview table columns programmatically
        if (bulkUpdatePreviewTable != null) {
            initializeBulkUpdatePreviewTable();
        }
    }
    
    private void loadProducts() {
        // Simplificar para cargar todos los productos directamente
        List<Product> products = inventoryDAO.getAllProducts();
        productList.setAll(products);
        
        // Update total products count
        if (totalProductsLabel != null) {
            totalProductsLabel.setText("Total de productos: " + products.size());
        }
        
        // Update inventory summary value
        updateInventorySummary();
    }
    
    private void updateInventorySummary() {
        if (inventorySummaryLabel != null) {
            double totalValue = productList.stream()
                .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
                .sum();
            inventorySummaryLabel.setText(String.format("Valor total: $%.2f", totalValue));
        }
    }
    
    private void updateLastUpdatedTimestamp() {
        if (lastUpdatedLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lastUpdatedLabel.setText("Última actualización: " + java.time.LocalDateTime.now().format(formatter));
        }
    }
    
    private void updateReportValues() {
        try {
            if (inventoryValueLabel != null) {
                double totalValue = productList.stream()
                    .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
                    .sum();
                inventoryValueLabel.setText(String.format("$%.2f", totalValue));
            }
            
            if (criticalStockCountLabel != null) {
                long criticalCount = productList.stream()
                    .filter(p -> p.getStockQuantity() <= p.getReorderLevel())
                    .count();
                criticalStockCountLabel.setText(String.valueOf(criticalCount));
            }
            
            if (noMovementCountLabel != null) {
                // This would require additional logic to track products without movement
                // For now we'll set a placeholder value
                noMovementCountLabel.setText("0");
            }
        } catch (Exception e) {
            System.err.println("Error updating report values: " + e.getMessage());
        }
    }
    
    // New method for scanning barcode
    @FXML
    private void handleScanBarcode() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Escanear Código de Barras");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad de escaneo de código de barras no implementada.");
        alert.showAndWait();
    }
    
    // New methods for product import/export
    @FXML
    private void handleImportProducts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Productos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        Stage stage = (Stage) productsTable.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Importación");
            alert.setHeaderText(null);
            alert.setContentText("Funcionalidad de importación no implementada.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleExportProducts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Productos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        Stage stage = (Stage) productsTable.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        
        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación");
            alert.setHeaderText(null);
            alert.setContentText("Funcionalidad de exportación no implementada.");
            alert.showAndWait();
        }
    }
    
    // Bulk update operations
    @FXML
    private void handlePreviewBulkUpdate() {
        try {
            String category = bulkCategoryCombo.getValue();
            String adjustmentType = adjustmentTypeCombo.getValue();
            String adjustmentValue = adjustmentValueField.getText();
            
            if (category == null || adjustmentType == null || adjustmentValue == null || adjustmentValue.isEmpty()) {
                mostrarError("Todos los campos son requeridos para la actualización masiva");
                return;
            }
            
            // Get products of selected category
            List<Product> productsToUpdate = inventoryDAO.getAllProducts().stream()
                .filter(p -> category.equals(p.getCategory()))
                .collect(Collectors.toList());
                
            if (productsToUpdate.isEmpty()) {
                mostrarError("No hay productos en la categoría seleccionada");
                return;
            }
            
            // Show preview
            bulkUpdatePreviewTable.setItems(FXCollections.observableArrayList(productsToUpdate));
            statusLabel.setText("Previsualización lista. Revise los cambios antes de aplicar.");
            
        } catch (Exception e) {
            mostrarError("Error al previsualizar cambios: " + e.getMessage());
        }
    }
    
    private double calculateNewPrice(Product product) {
        try {
            double currentPrice = product.getSellingPrice();
            String adjustmentType = adjustmentTypeCombo.getValue();
            double value = Double.parseDouble(adjustmentValueField.getText());
            
            if ("Porcentaje (%)".equals(adjustmentType)) {
                return currentPrice * (1 + (value / 100));
            } else { // "Monto Fijo"
                return currentPrice + value;
            }
        } catch (Exception e) {
            return product.getSellingPrice();
        }
    }
    
    @FXML
    private void handleApplyBulkUpdate() {
        try {
            ObservableList<Product> productsToUpdate = bulkUpdatePreviewTable.getItems();
            
            if (productsToUpdate == null || productsToUpdate.isEmpty()) {
                mostrarError("No hay productos para actualizar");
                return;
            }
            
            // Apply changes
            for (Product product : productsToUpdate) {
                double newPrice = calculateNewPrice(product);
                product.setSellingPrice(newPrice);
                inventoryDAO.updateProduct(product);
            }
            
            mostrarMensaje("Precios actualizados exitosamente para " + productsToUpdate.size() + " productos");
            loadProducts();
            bulkUpdatePreviewTable.getItems().clear();
            
        } catch (Exception e) {
            mostrarError("Error al aplicar cambios: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelBulkUpdate() {
        bulkCategoryCombo.setValue(null);
        adjustmentTypeCombo.setValue(null);
        adjustmentValueField.clear();
        bulkUpdatePreviewTable.getItems().clear();
        statusLabel.setText("Operación cancelada");
    }
    
    // Import/Export template operations
    @FXML
    private void handleDownloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plantilla");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("plantilla_inventario.xlsx");
        
        Stage stage = (Stage) importStatusLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            // Implement template generation logic here
            importStatusLabel.setText("Plantilla descargada en: " + file.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleImportFromExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar desde Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        
        Stage stage = (Stage) importStatusLabel.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            // Implement import logic here
            importStatusLabel.setText("Importando desde: " + file.getAbsolutePath());
            
            // Show placeholder message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Importar Inventario");
            alert.setHeaderText(null);
            alert.setContentText("Funcionalidad de importación no implementada.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleExportFullInventory() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Inventario Completo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("inventario_completo.xlsx");
        
        Stage stage = (Stage) importStatusLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            // Implement export logic here
            importStatusLabel.setText("Exportando a: " + file.getAbsolutePath());
            
            // Show placeholder message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportar Inventario");
            alert.setHeaderText(null);
            alert.setContentText("Funcionalidad de exportación no implementada.");
            alert.showAndWait();
        }
    }
    
    // Report generation methods
    @FXML
    private void handleViewInventoryValueDetails() {
        try {
            // Cargar el archivo FXML de la vista detallada
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/inventory-value-details.fxml"));
            Parent detailsRoot = loader.load();
            
            // Obtener el controlador y pasar los datos necesarios
            InventoryValueDetailsController controller = loader.getController();
            
            // Pasar la lista completa de productos y datos de resumen
            controller.initData(
                productList, 
                calculateTotalInventoryValue(), 
                productList.size(), 
                inventoryDAO.getDistinctCategories()
            );
            
            // Crear y configurar una nueva ventana para la vista detallada
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Detalles del Valor de Inventario");
            detailsStage.setScene(new Scene(detailsRoot));
            detailsStage.setWidth(1000);
            detailsStage.setHeight(700);
            detailsStage.initModality(Modality.WINDOW_MODAL);
            detailsStage.initOwner(inventoryValueLabel.getScene().getWindow());
            
            // Mostrar la ventana
            detailsStage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir la vista detallada: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método auxiliar para calcular el valor total del inventario
    private double calculateTotalInventoryValue() {
        return productList.stream()
            .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
            .sum();
    }
    
    @FXML
    private void handleViewCriticalStockDetails() {
        showReportPlaceholder("Detalles de Stock Crítico");
    }
    
    @FXML
    private void handleViewNoMovementDetails() {
        try {
            // Cargar el archivo FXML de la vista detallada
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/no-movement-details.fxml"));
            Parent detailsRoot = loader.load();
            
            // Obtener el controlador y pasar los datos necesarios
            NoMovementDetailsController controller = loader.getController();
            
            // Obtener productos sin movimiento (aquí podrías filtrar los productos que no han tenido movimiento)
            List<Product> noMovementProducts = inventoryDAO.getProductsWithoutMovement();
            
            // Pasar los datos al controlador
            controller.initData(noMovementProducts);
            
            // Crear y configurar una nueva ventana para la vista detallada
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Detalles de Productos Sin Movimiento");
            detailsStage.setScene(new Scene(detailsRoot));
            detailsStage.setWidth(1000);
            detailsStage.setHeight(700);
            detailsStage.initModality(Modality.WINDOW_MODAL);
            detailsStage.initOwner(noMovementCountLabel.getScene().getWindow());
            
            // Mostrar la ventana
            detailsStage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir la vista detallada: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGenerateCategoryReport() {
        showReportPlaceholder("Reporte de Rendimiento por Categoría");
    }
    
    @FXML
    private void handleGeneratePriceHistoryReport() {
        try {
            // Cargar el archivo FXML de la vista de historial de precios
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/price-history-report.fxml"));
            Parent reportRoot = loader.load();
            
            // Obtener el controlador y pasar los datos necesarios
            PriceHistoryReportController controller = loader.getController();
            
            // Pasar la lista de productos al controlador
            controller.initData(productList, inventoryDAO);
            
            // Crear y configurar una nueva ventana para la vista del reporte
            Stage reportStage = new Stage();
            reportStage.setTitle("Historial de Precios");
            reportStage.setScene(new Scene(reportRoot));
            reportStage.setWidth(1000);
            reportStage.setHeight(700);
            reportStage.initModality(Modality.WINDOW_MODAL);
            reportStage.initOwner(inventoryValueLabel.getScene().getWindow());
            
            // Mostrar la ventana
            reportStage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir el reporte de historial de precios: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showReportPlaceholder(String reportName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reporte");
        alert.setHeaderText(reportName);
        alert.setContentText("La generación de reportes no está implementada aún.");
        alert.showAndWait();
    }
    
    // Refactorizar método de búsqueda para trabajar sin paginación
    @FXML
    protected void handleSearchProduct() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            // Reset to show all products
            loadProducts();
        } else {
            // Filter products based on search query
            List<Product> searchResults = inventoryDAO.searchProducts(query);
            productList.setAll(searchResults);
            
            // Update status message
            statusLabel.setText(String.format("Se encontraron %d productos que coinciden con '%s'", 
                    searchResults.size(), query));
        }
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
            statusLabel.setText("Ingrese un criterio de búsqueda.");
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        editProductsTable.setItems(results);
        
        if (results.isEmpty()) {
            statusLabel.setText("No se encontraron productos que coincidan con: " + query);
        } else {
            statusLabel.setText("Se encontraron " + results.size() + " productos. Seleccione uno para editar.");
        }
    }
    @FXML
    private void handleUpdateSelectedProduct() {
        if(selectedProductForEdit == null) {
            mostrarError("Primero debe seleccionar un producto de la tabla para editar");
            return;
        }
        
        try {
            selectedProductForEdit.setName(editNameField.getText());
            selectedProductForEdit.setPurchasePrice(Double.parseDouble(editPurchasePriceField.getText()));
            selectedProductForEdit.setSellingPrice(Double.parseDouble(editSellingPriceField.getText()));
            selectedProductForEdit.setStockQuantity(Integer.parseInt(editStockField.getText()));
            selectedProductForEdit.setReorderLevel(Integer.parseInt(editReorderLevelField.getText()));
            
            if(inventoryDAO.updateProduct(selectedProductForEdit)) {
                mostrarMensaje("Producto actualizado exitosamente");
                loadProducts();
                // Actualizar la tabla de edición
                editProductsTable.refresh();
            } else {
                mostrarError("No se pudo actualizar el producto");
            }
        } catch(Exception e) {
            mostrarError("Error al actualizar: " + e.getMessage());
        }
    }
    @FXML
    private void handleAddProductFromEdit() {
        try {
            // Validate required fields
            if(editNameField.getText().trim().isEmpty() ||
               editPurchasePriceField.getText().trim().isEmpty() ||
               editSellingPriceField.getText().trim().isEmpty() ||
               editStockField.getText().trim().isEmpty() ||
               editReorderLevelField.getText().trim().isEmpty()) {
                mostrarError("Todos los campos son obligatorios");
                return;
            }
            
            // Create a new product with the data entered in edit fields
            Product newProduct = new Product();
            newProduct.setBarcode(generateRandomBarcode()); // Generate random barcode for new product
            newProduct.setName(editNameField.getText().trim());
            
            try {
                newProduct.setPurchasePrice(Double.parseDouble(editPurchasePriceField.getText().trim()));
                newProduct.setSellingPrice(Double.parseDouble(editSellingPriceField.getText().trim()));
                newProduct.setStockQuantity(Integer.parseInt(editStockField.getText().trim()));
                newProduct.setReorderLevel(Integer.parseInt(editReorderLevelField.getText().trim()));
            } catch(NumberFormatException e) {
                mostrarError("Formato inválido en campos numéricos: " + e.getMessage());
                return;
            }
            
            // Add the product to database
            if(inventoryDAO.addProduct(newProduct)) {
                mostrarMensaje("Nuevo producto agregado exitosamente");
                loadProducts(); // Reload products
                clearEditFields(); // Clear the edit fields
            } else {
                mostrarError("No se pudo agregar el nuevo producto");
            }
            
        } catch(Exception e) {
            mostrarError("Error al agregar nuevo producto: " + e.getMessage());
        }
    }
    
    /**
     * Generates a random barcode for new products
     */
    private String generateRandomBarcode() {
        // Generate 13-digit random barcode
        StringBuilder barcode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 13; i++) {
            barcode.append(random.nextInt(10));
        }
        return barcode.toString();
    }
    
    /**
     * Clears all edit fields
     */
    private void clearEditFields() {
        editNameField.clear();
        editPurchasePriceField.clear();
        editSellingPriceField.clear();
        editStockField.clear();
        editReorderLevelField.clear();
    }
    
    @FXML
    private void handleCancelEdit() {
        // Limpia campos del panel de edición
        searchEditField.clear();
        editProductsTable.getItems().clear();
        if (editBarcodeField != null) editBarcodeField.clear();
        editNameField.clear();
        editPurchasePriceField.clear();
        editSellingPriceField.clear();
        editStockField.clear();
        editReorderLevelField.clear();
        selectedProductForEdit = null;
        statusLabel.setText("Edición cancelada");
    }

    /**
     * Método centralizado para llenar los campos de edición
     * y almacenar el producto seleccionado
     */
    private void fillEditFields(Product product) {
        if (product == null) return;
        
        selectedProductForEdit = product;
        
        // Populate all edit fields
        if (editBarcodeField != null) {
            editBarcodeField.setText(product.getBarcode());
        }
        editNameField.setText(product.getName());
        editPurchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
        editSellingPriceField.setText(String.valueOf(product.getSellingPrice()));
        editStockField.setText(String.valueOf(product.getStockQuantity()));
        editReorderLevelField.setText(String.valueOf(product.getReorderLevel()));
        
        // Mostrar mensaje informativo
        statusLabel.setText("Producto seleccionado para edición: " + product.getName());
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

    /**
     * Método específico para inicializar la tabla de previsualización de actualización masiva
     * Separado para mejorar la claridad y evitar problemas de inicialización
     */
    private void initializeBulkUpdatePreviewTable() {
        // Asegurarnos de que la tabla esté limpia
        bulkUpdatePreviewTable.getColumns().clear();
        
        // Crear columnas
        TableColumn<Product, String> codeCol = new TableColumn<>("Código");
        codeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBarcode()));
        codeCol.setPrefWidth(120);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, Double> currentPriceCol = new TableColumn<>("Precio Actual");
        currentPriceCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asObject());
        currentPriceCol.setPrefWidth(120);
        
        TableColumn<Product, Double> newPriceCol = new TableColumn<>("Nuevo Precio");
        newPriceCol.setCellValueFactory(cellData -> {
            try {
                double currentPrice = cellData.getValue().getSellingPrice();
                if (adjustmentTypeCombo.getValue() == null || adjustmentValueField == null || adjustmentValueField.getText().isEmpty()) {
                    return new javafx.beans.property.SimpleDoubleProperty(currentPrice).asObject();
                }
                return new javafx.beans.property.SimpleDoubleProperty(calculateNewPrice(cellData.getValue())).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asObject();
            }
        });
        newPriceCol.setPrefWidth(120);
        
        // Agregar columnas a la tabla
        bulkUpdatePreviewTable.getColumns().addAll(codeCol, nameCol, currentPriceCol, newPriceCol);
        
        // Mantener referencia de columnas
        bulkBarcodeColumn = codeCol;
        bulkNameColumn = nameCol;
        bulkCurrentPriceColumn = currentPriceCol;
        bulkNewPriceColumn = newPriceCol;
    }

    @FXML
    private void handleFilterByCategory() {
        try {
            // Cargar la ventana de filtros avanzados
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/advanced-filter.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador y pasar datos necesarios
            AdvancedFilterController controller = loader.getController();
            controller.initData(inventoryDAO, productList);
            
            // Configurar la ventana modal
            Stage filterStage = new Stage();
            filterStage.setTitle("Filtros Avanzados");
            filterStage.setScene(new Scene(root));
            filterStage.initModality(Modality.WINDOW_MODAL);
            filterStage.initOwner(productsTable.getScene().getWindow());
            
            // Configurar el callback para cuando se apliquen los filtros
            controller.setFilterCallback(filteredResults -> {
                // Actualizar la tabla con los resultados filtrados
                productList.setAll(filteredResults);
                
                // Actualizar mensaje de estado
                statusLabel.setText(String.format("Mostrando %d productos con los filtros aplicados", 
                        filteredResults.size()));
            });
            
            // Mostrar la ventana y esperar hasta que se cierre
            filterStage.showAndWait();
            
        } catch (IOException e) {
            mostrarError("Error al abrir la ventana de filtros: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateExpirationReport() {
        try {
            // Cargar el archivo FXML de la vista de reporte de vencimientos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/expiration-report.fxml"));
            Parent reportRoot = loader.load();
            
            // Obtener el controlador y pasar los datos necesarios
            ExpirationReportController controller = loader.getController();
            
            // Filtrar productos con fecha de vencimiento
            List<Product> productsWithExpiration = productList.stream()
                .filter(p -> p.getExpirationDate() != null)
                .collect(Collectors.toList());
            
            // Pasar la lista de productos al controlador
            controller.initData(productsWithExpiration);
            
            // Crear y configurar una nueva ventana para la vista del reporte
            Stage reportStage = new Stage();
            reportStage.setTitle("Reporte de Vencimientos");
            reportStage.setScene(new Scene(reportRoot));
            reportStage.setWidth(1000);
            reportStage.setHeight(700);
            reportStage.initModality(Modality.WINDOW_MODAL);
            reportStage.initOwner(inventoryValueLabel.getScene().getWindow());
            
            // Mostrar la ventana
            reportStage.show();
        } catch (IOException e) {
            mostrarError("Error al abrir el reporte de vencimientos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
