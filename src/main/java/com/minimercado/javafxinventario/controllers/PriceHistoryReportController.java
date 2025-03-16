package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.PriceHistory;
import com.minimercado.javafxinventario.modules.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PriceHistoryReportController {

    // TableView and column declarations to match FXML
    @FXML private TableView<PriceHistory> priceHistoryTable;
    @FXML private TableColumn<PriceHistory, String> productIdColumn;
    @FXML private TableColumn<PriceHistory, String> productNameColumn;
    @FXML private TableColumn<PriceHistory, Double> previousPriceColumn;
    @FXML private TableColumn<PriceHistory, Double> currentPriceColumn;
    @FXML private TableColumn<PriceHistory, Date> changeDateColumn;
    @FXML private TableColumn<PriceHistory, Double> percentageChangeColumn;
    @FXML private TableColumn<PriceHistory, String> userColumn;

    // Other UI elements
    @FXML private ComboBox<String> periodCombo;
    @FXML private Label statusLabel;

    // Data and helpers
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<PriceHistory> priceHistoryList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    @FXML
    public void initialize() {
        // Initialize the table columns with proper cell value factories
        productIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProductId()));
            
        productNameColumn.setCellValueFactory(cellData -> {
            // In a real implementation, you would lookup the product name by ID
            // For now, we'll just use a placeholder or the ID itself
            String productId = cellData.getValue().getProductId();
            String productName = getProductNameById(productId);
            return new SimpleStringProperty(productName);
        });
        
        previousPriceColumn.setCellValueFactory(cellData -> {
            // This would be based on how PriceHistory tracks the previous price
            // For simplicity, we'll use a calculation based on current price and change percentage
            double currentPrice = cellData.getValue().getPrice();
            double changePercent = cellData.getValue().getChangePercent();
            double previousPrice = currentPrice / (1 + (changePercent / 100));
            return new SimpleDoubleProperty(previousPrice).asObject();
        });
        
        currentPriceColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
            
        changeDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDate()));
            
        percentageChangeColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getChangePercent()).asObject());
            
        userColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUser()));
            
        // Format currency columns
        previousPriceColumn.setCellFactory(column -> new TableCell<PriceHistory, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        currentPriceColumn.setCellFactory(column -> new TableCell<PriceHistory, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        // Format percentage column
        percentageChangeColumn.setCellFactory(column -> new TableCell<PriceHistory, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Format date column
        changeDateColumn.setCellFactory(column -> new TableCell<PriceHistory, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
        
        // Initialize period combo with options
        periodCombo.setItems(FXCollections.observableArrayList(
            "Últimos 7 días", 
            "Últimos 30 días", 
            "Últimos 90 días", 
            "Último año", 
            "Todo"
        ));
        periodCombo.setValue("Últimos 30 días");
        
        // Set items to the table
        priceHistoryTable.setItems(priceHistoryList);
        
        // Initial load
        handleGenerateReport();
    }
    
    private String getProductNameById(String productId) {
        // This would typically query the database for the product name
        // For now we'll return a placeholder
        return "Product " + productId;
    }
    
    @FXML
    private void handleGenerateReport() {
        try {
            // Clear existing data
            priceHistoryList.clear();
            
            // Get selected period
            String selectedPeriod = periodCombo.getValue();
            Date startDate = calculateStartDate(selectedPeriod);
            
            // Load price history data
            // Fix the method call to match the required signature
            // Instead of passing date range, we'll get all price history
            // and filter by date in memory
            List<PriceHistory> allHistory = new ArrayList<>();
            
            // Get all products first to iterate through their IDs
            List<Product> products = inventoryDAO.getAllProducts();
            for (Product product : products) {
                // Get price history for each product
                List<PriceHistory> productHistory = inventoryDAO.getPriceHistory(product.getBarcode());
                // Add to our master list
                allHistory.addAll(productHistory);
            }
            
            // Filter by date range
            List<PriceHistory> filteredHistory = allHistory.stream()
                .filter(ph -> {
                    // Only include entries within our date range
                    return ph.getDate() != null && 
                           !ph.getDate().before(startDate) && 
                           !ph.getDate().after(new Date());
                })
                .collect(Collectors.toList());
            
            priceHistoryList.addAll(filteredHistory);
            
            // Update status
            statusLabel.setText("Se encontraron " + filteredHistory.size() + " cambios de precio");
            
        } catch (Exception e) {
            statusLabel.setText("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Date calculateStartDate(String period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        
        switch(period) {
            case "Últimos 7 días":
                startDate = now.minusDays(7);
                break;
            case "Últimos 30 días":
                startDate = now.minusDays(30);
                break;
            case "Últimos 90 días":
                startDate = now.minusDays(90);
                break;
            case "Último año":
                startDate = now.minusYears(1);
                break;
            case "Todo":
            default:
                startDate = now.minusYears(10); // Far back in the past
                break;
        }
        
        return Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    @FXML
    private void handleExportToExcel() {
        if (priceHistoryList.isEmpty()) {
            statusLabel.setText("No hay datos para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName("historial_precios.xlsx");
        
        Stage stage = (Stage) priceHistoryTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Historial de Precios");
                
                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID Producto");
                headerRow.createCell(1).setCellValue("Nombre Producto");
                headerRow.createCell(2).setCellValue("Precio Anterior");
                headerRow.createCell(3).setCellValue("Precio Actual");
                headerRow.createCell(4).setCellValue("Fecha de Cambio");
                headerRow.createCell(5).setCellValue("% Cambio");
                headerRow.createCell(6).setCellValue("Usuario");
                
                // Fill data rows
                for (int i = 0; i < priceHistoryList.size(); i++) {
                    PriceHistory history = priceHistoryList.get(i);
                    Row row = sheet.createRow(i + 1);
                    
                    row.createCell(0).setCellValue(history.getProductId());
                    row.createCell(1).setCellValue(getProductNameById(history.getProductId()));
                    
                    double currentPrice = history.getPrice();
                    double changePercent = history.getChangePercent();
                    double previousPrice = currentPrice / (1 + (changePercent / 100));
                    
                    row.createCell(2).setCellValue(previousPrice);
                    row.createCell(3).setCellValue(currentPrice);
                    row.createCell(4).setCellValue(dateFormat.format(history.getDate()));
                    row.createCell(5).setCellValue(history.getChangePercent());
                    row.createCell(6).setCellValue(history.getUser());
                }
                
                // Auto-size columns
                for (int i = 0; i < 7; i++) {
                    sheet.autoSizeColumn(i);
                }
                
                // Write to file
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
                
                statusLabel.setText("Datos exportados exitosamente a: " + file.getName());
                
            } catch (Exception e) {
                statusLabel.setText("Error al exportar datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) priceHistoryTable.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Initialize data for the report
     * @param products List of products to display price history for
     * @param inventoryDAO DAO for accessing inventory data
     */
    public void initData(ObservableList<Product> products, InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
        
        // If a specific set of products is provided, we could filter the price history
        // to only show history for those products
        
        // For now, just trigger the report generation
        handleGenerateReport();
    }
}
