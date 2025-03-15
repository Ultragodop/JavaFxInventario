package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.PriceHistory;
import com.minimercado.javafxinventario.modules.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PriceHistoryReportController {
    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Double> currentPriceColumn;
    
    @FXML private TableView<PriceHistory> priceHistoryTable;
    @FXML private TableColumn<PriceHistory, Date> dateColumn;
    @FXML private TableColumn<PriceHistory, Double> priceColumn;
    @FXML private TableColumn<PriceHistory, Double> changePercentColumn;
    @FXML private TableColumn<PriceHistory, String> userColumn;
    
    @FXML private Label statusLabel;
    
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<PriceHistory> priceHistoryList = FXCollections.observableArrayList();
    private InventoryDAO inventoryDAO;
    
    @FXML
    public void initialize() {
        // Initialize products table columns
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        
        // Initialize price history table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        changePercentColumn.setCellValueFactory(new PropertyValueFactory<>("changePercent"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        // Format date column
        dateColumn.setCellFactory(column -> {
            return new TableCell<PriceHistory, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                
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
        
        // Format price columns
        priceColumn.setCellFactory(column -> {
            return new TableCell<PriceHistory, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
        
        currentPriceColumn.setCellFactory(column -> {
            return new TableCell<Product, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
        
        changePercentColumn.setCellFactory(column -> {
            return new TableCell<PriceHistory, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f%%", item));
                    }
                }
            };
        });
        
        // Set items to tables
        productsTable.setItems(productList);
        priceHistoryTable.setItems(priceHistoryList);
        
        // Add listener for selection
        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadPriceHistory(newSelection);
                }
            }
        );
    }
    
    public void initData(ObservableList<Product> products, InventoryDAO dao) {
        this.inventoryDAO = dao;
        this.productList.setAll(products);
        
        if (!products.isEmpty()) {
            statusLabel.setText("Seleccione un producto para ver su historial de precios");
        } else {
            statusLabel.setText("No hay productos disponibles");
        }
    }
    
    @FXML
    private void handleSearchProducts() {
        String searchQuery = searchField.getText().trim();
        
        if (searchQuery.isEmpty()) {
            // Reset to show all products
            productsTable.setItems(productList);
            statusLabel.setText("Mostrando todos los productos");
        } else {
            // Filter products based on search query
            ObservableList<Product> filteredList = FXCollections.observableArrayList();
            
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(searchQuery.toLowerCase()) || 
                    product.getBarcode().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(searchQuery.toLowerCase()))) {
                    filteredList.add(product);
                }
            }
            
            productsTable.setItems(filteredList);
            statusLabel.setText("Se encontraron " + filteredList.size() + " productos que coinciden con '" + searchQuery + "'");
        }
    }
    
    private void loadPriceHistory(Product product) {
        try {
            // Get price history for selected product
            List<PriceHistory> history = inventoryDAO.getPriceHistory(product.getBarcode());
            priceHistoryList.setAll(history);
            
            if (history.isEmpty()) {
                statusLabel.setText("No hay historial de precios para este producto");
            } else {
                statusLabel.setText("Mostrando historial de precios para: " + product.getName());
            }
        } catch (Exception e) {
            statusLabel.setText("Error al cargar el historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExportToExcel() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Debe seleccionar un producto para exportar su historial");
            return;
        }
        
        if (priceHistoryList.isEmpty()) {
            showAlert("Información", "No hay historial de precios para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Historial de Precios");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("historial_precios_" + selectedProduct.getBarcode() + ".xlsx");
        
        Stage stage = (Stage) searchField.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // Aquí se implementaría la funcionalidad de exportación a Excel
                showAlert("Información", "Funcionalidad de exportación a Excel no implementada.\nSe exportaría a: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Error", "Error al exportar: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handlePrintReport() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Debe seleccionar un producto para imprimir su historial");
            return;
        }
        
        if (priceHistoryList.isEmpty()) {
            showAlert("Información", "No hay historial de precios para imprimir");
            return;
        }
        
        // Aquí se implementaría la funcionalidad de impresión
        showAlert("Información", "Funcionalidad de impresión no implementada");
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
