package com.minimercado.javafxinventario.controllers;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExpirationReportController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> expirationFilterCombo;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Date> expirationDateColumn;
    @FXML private TableColumn<Product, Integer> daysRemainingColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private Label statusLabel;
    
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Initialize table columns
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        expirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Custom cell factory for days remaining column
        daysRemainingColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product.getExpirationDate() == null) {
                return new javafx.beans.property.SimpleObjectProperty<Integer>(0);
            }
            
            // Calculate days between today and expiration date
            LocalDate today = LocalDate.now();
            LocalDate expiration = product.getExpirationDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expiration);
            return new javafx.beans.property.SimpleObjectProperty<Integer>((int)daysRemaining);
        });
        
        // Format date column
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
        
        // Color rows based on expiration date
        productTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                
                if (product == null || empty) {
                    setStyle("");
                    return;
                }
                
                // If expiration date is null, no special style
                if (product.getExpirationDate() == null) {
                    setStyle("");
                    return;
                }
                
                // Calculate days remaining
                LocalDate today = LocalDate.now();
                LocalDate expiration = product.getExpirationDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expiration);
                
                // Apply styles based on days remaining
                if (daysRemaining < 0) {
                    // Expired
                    setStyle("-fx-background-color: #ffcccc;"); // Light red
                } else if (daysRemaining <= 30) {
                    // Expiring soon (within 30 days)
                    setStyle("-fx-background-color: #ffffcc;"); // Light yellow
                } else {
                    // Not expiring soon
                    setStyle("");
                }
            }
        });
        
        // Set up filter combo
        expirationFilterCombo.setItems(FXCollections.observableArrayList(
            "Todos", "Vencidos", "Vence en 30 días", "Vence en 90 días"
        ));
        expirationFilterCombo.getSelectionModel().select(0);
        
        // Add listeners
        expirationFilterCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filterProducts();
                }
            }
        );
        
        searchField.textProperty().addListener(
            (obs, oldVal, newVal) -> filterProducts()
        );
        
        // Set table items
        productTable.setItems(filteredProducts);
    }
    
    public void initData(List<Product> products) {
        this.allProducts.setAll(products);
        filterProducts();
        
        updateStatusLabel();
    }
    
    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filterOption = expirationFilterCombo.getValue();
        
        // Instead of modifying filterOption, create a new final variable for use in the lambda
        final String finalFilterOption = (filterOption == null) ? "Todos" : filterOption;
        
        // Create a filtered list based on search text and expiration filter
        List<Product> filtered = allProducts.stream()
            .filter(product -> {
                // Filter by search text
                if (!searchText.isEmpty()) {
                    boolean matchesSearch = 
                        product.getBarcode().toLowerCase().contains(searchText) ||
                        product.getName().toLowerCase().contains(searchText);
                    
                    if (!matchesSearch) {
                        return false;
                    }
                }
                
                // Filter by expiration
                if (product.getExpirationDate() == null) {
                    return false; // Skip products without expiration date
                }
                
                if ("Todos".equals(finalFilterOption)) {
                    return true;
                }
                
                // Calculate days remaining
                LocalDate today = LocalDate.now();
                LocalDate expiration = product.getExpirationDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expiration);
                
                // Apply filter based on days remaining
                switch (finalFilterOption) {
                    case "Vencidos":
                        return daysRemaining < 0;
                    case "Vence en 30 días":
                        return daysRemaining >= 0 && daysRemaining <= 30;
                    case "Vence en 90 días":
                        return daysRemaining >= 0 && daysRemaining <= 90;
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
        
        // Update the filtered list
        filteredProducts.setAll(filtered);
        
        // Update status label
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        int expiredCount = 0;
        int soonToExpireCount = 0;
        
        for (Product product : allProducts) {
            if (product.getExpirationDate() == null) {
                continue;
            }
            
            LocalDate today = LocalDate.now();
            LocalDate expiration = product.getExpirationDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expiration);
            
            if (daysRemaining < 0) {
                expiredCount++;
            } else if (daysRemaining <= 30) {
                soonToExpireCount++;
            }
        }
        
        statusLabel.setText(String.format("Mostrando %d productos de %d totales. Vencidos: %d, Por vencer (30 días): %d",
            filteredProducts.size(), allProducts.size(), expiredCount, soonToExpireCount));
    }
    
    @FXML
    private void handleExportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Reporte de Vencimientos");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("reporte_vencimientos.xlsx");
        
        Stage stage = (Stage) searchField.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            // Here you would implement the actual export logic
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación");
            alert.setHeaderText(null);
            alert.setContentText("Funcionalidad de exportación no implementada.\nSe exportaría a: " + file.getAbsolutePath());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handlePrintReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Imprimir");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad de impresión no implementada.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }
}
