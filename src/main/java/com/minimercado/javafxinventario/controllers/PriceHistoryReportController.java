package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.PriceChange;
import com.minimercado.javafxinventario.modules.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Price History Report screen
 */
public class PriceHistoryReportController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> currentPriceColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    @FXML private TableView<PriceChange> priceHistoryTable;
    @FXML private TableColumn<PriceChange, Date> dateColumn;
    @FXML private TableColumn<PriceChange, Double> priceColumn;
    @FXML private TableColumn<PriceChange, Double> changePercentColumn;
    @FXML private TableColumn<PriceChange, String> userColumn;
    
    @FXML private Label productNameLabel;
    @FXML private Label totalChangesLabel;
    @FXML private Label averageChangeLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateRangeLabel;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private LineChart<Number, Number> priceChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<PriceChange> priceChangesList = FXCollections.observableArrayList();
    private InventoryDAO inventoryDAO;
    
    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        barcodeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        currentPriceColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getSellingPrice()).asObject());
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        changePercentColumn.setCellValueFactory(new PropertyValueFactory<>("changePercent"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        // Format the date column
        dateColumn.setCellFactory(column -> {
            TableCell<PriceChange, Date> cell = new TableCell<PriceChange, Date>() {
                private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(format.format(item));
                    }
                }
            };
            return cell;
        });
        
        // Format price columns with currency
        currentPriceColumn.setCellFactory(column -> {
            return new TableCell<Product, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
        
        priceColumn.setCellFactory(column -> {
            return new TableCell<PriceChange, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
        
        changePercentColumn.setCellFactory(column -> {
            return new TableCell<PriceChange, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f%%", item));
                        // Color coding for positive/negative changes
                        if(item > 0) {
                            setStyle("-fx-text-fill: green;");
                        } else if(item < 0) {
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
        });
        
        // Set up the table selection listeners
        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadPriceHistory(newVal);
                }
            }
        );
        
        // Initialize date pickers with default values (last 30 days)
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        startDatePicker.setValue(thirtyDaysAgo);
        endDatePicker.setValue(today);
        updateDateRangeLabel(thirtyDaysAgo, today);
        
        // Set items to tables
        productsTable.setItems(productList);
        priceHistoryTable.setItems(priceChangesList);
    }
    
    /**
     * Initialize data in the controller
     * @param products List of products to display
     * @param dao Data access object for fetching price history
     */
    public void initData(List<Product> products, InventoryDAO dao) {
        this.inventoryDAO = dao;
        
        if (products != null) {
            productList.setAll(products);
        }
        
        // If products were provided, automatically show them
        if (!productList.isEmpty()) {
            statusLabel.setText(String.format("Mostrando %d productos", productList.size()));
        } else {
            statusLabel.setText("No hay productos para mostrar");
        }
    }
    
    /**
     * Load price history for the selected product
     * @param product Selected product
     */
    private void loadPriceHistory(Product product) {
        if (product == null || inventoryDAO == null) return;
        
        try {
            // Update product name label
            productNameLabel.setText(product.getName() + " (" + product.getBarcode() + ")");
            
            // Get date range from pickers
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Convert LocalDate to Date
            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            // Get price history from DAO
            List<PriceChange> priceHistory = inventoryDAO.getPriceHistory(product.getBarcode(), start, end);
            priceChangesList.setAll(priceHistory);
            
            // Update summary statistics
            updatePriceStatistics(priceHistory);
            
            // Create price chart
            createPriceChart(product, priceHistory);
            
        } catch (Exception e) {
            statusLabel.setText("Error al cargar historial de precios: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update price statistics labels
     * @param priceHistory List of price changes
     */
    private void updatePriceStatistics(List<PriceChange> priceHistory) {
        totalChangesLabel.setText(String.valueOf(priceHistory.size()));
        
        if (!priceHistory.isEmpty()) {
            double avgChange = priceHistory.stream()
                .mapToDouble(PriceChange::getChangePercent)
                .average()
                .orElse(0);
            
            averageChangeLabel.setText(String.format("%.2f%%", avgChange));
        } else {
            averageChangeLabel.setText("0.00%");
        }
    }
    
    /**
     * Create price chart from history data
     * @param product The product
     * @param priceHistory List of price changes
     */
    private void createPriceChart(Product product, List<PriceChange> priceHistory) {
        priceChart.getData().clear();
        
        // If no price history, don't create chart
        if (priceHistory.isEmpty()) return;
        
        // Create series for the chart
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Precio de " + product.getName());
        
        // Sort price changes by date
        List<PriceChange> sortedChanges = new ArrayList<>(priceHistory);
        sortedChanges.sort(Comparator.comparing(PriceChange::getDate));
        
        // Add data points - convert date to millis for X axis
        for (PriceChange change : sortedChanges) {
            series.getData().add(new XYChart.Data<>(
                change.getDate().getTime(), 
                change.getPrice()
            ));
        }
        
        priceChart.getData().add(series);
    }
    
    /**
     * Handle update date range button click
     */
    @FXML
    public void handleUpdateDateRange() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Validate dates
            if (startDate == null || endDate == null) {
                statusLabel.setText("Por favor seleccione fechas válidas");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                statusLabel.setText("La fecha inicial no puede ser posterior a la fecha final");
                return;
            }
            
            updateDateRangeLabel(startDate, endDate);
            loadPriceHistory(selectedProduct);
        } else {
            statusLabel.setText("Seleccione un producto primero");
        }
    }
    
    /**
     * Update the date range label
     * @param startDate Start date
     * @param endDate End date
     */
    private void updateDateRangeLabel(LocalDate startDate, LocalDate endDate) {
        dateRangeLabel.setText(String.format(
            "Período: %s al %s", 
            startDate.toString(),
            endDate.toString()
        ));
    }
    
    /**
     * Handle search button click - this was missing and causing the error
     */
    @FXML
    public void handleSearchProducts() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            statusLabel.setText("Por favor ingrese un término de búsqueda");
            return;
        }
        
        // Filter products based on search term
        List<Product> filteredProducts = productList.stream()
            .filter(p -> p.getBarcode().contains(searchTerm) || 
                         p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                         p.getCategory().toLowerCase().contains(searchTerm.toLowerCase()))
            .collect(Collectors.toList());
        
        // Update table with filtered results
        productsTable.setItems(FXCollections.observableArrayList(filteredProducts));
        
        if (filteredProducts.isEmpty()) {
            statusLabel.setText("No se encontraron productos que coincidan con '" + searchTerm + "'");
        } else {
            statusLabel.setText("Se encontraron " + filteredProducts.size() + " productos");
        }
    }
    
    /**
     * Handle reset button click
     */
    @FXML
    public void handleResetSearch() {
        searchField.clear();
        productsTable.setItems(productList);
        statusLabel.setText("Búsqueda reiniciada. Mostrando " + productList.size() + " productos");
    }
    
    /**
     * Handle export to CSV button click
     */
    @FXML
    public void handleExportToCsv() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null || priceChangesList.isEmpty()) {
            statusLabel.setText("No hay datos para exportar");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar historial de precios");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("historial_precios_" + selectedProduct.getBarcode() + ".csv");
        
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Fecha,Precio,Variación %,Usuario\n");
                
                // Write data
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                for (PriceChange change : priceChangesList) {
                    writer.write(String.format("%s,%.2f,%.2f,%s\n",
                        dateFormat.format(change.getDate()),
                        change.getPrice(),
                        change.getChangePercent(),
                        change.getUser() != null ? change.getUser() : ""
                    ));
                }
                
                statusLabel.setText("Datos exportados correctamente a " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Error al exportar: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle close button click
     */
    @FXML
    public void handleClose() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
