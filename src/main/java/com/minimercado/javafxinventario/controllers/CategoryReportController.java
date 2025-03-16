package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CategoryReportController {

    public ComboBox filterTypeComboBox;
    public TextField filterField;
    public Button searchButton;
    public Button applyFilterButton; // New button for applying filters
    public TableColumn idColumn;
    public TableColumn descriptionColumn;
    public TableColumn nameColumn;
    public TableColumn createdAtColumn;
    public Button closeButton;
    // FXML injected fields with potential mismatches
    @FXML private TableView<CategoryPerformance> categoryTable;
    @FXML private TableColumn<CategoryPerformance, String> categoryColumn;
    @FXML private TableColumn<CategoryPerformance, Integer> productCountColumn;
    @FXML private TableColumn<CategoryPerformance, Integer> totalStockColumn;
    @FXML private TableColumn<CategoryPerformance, Double> inventoryValueColumn;
    @FXML private TableColumn<CategoryPerformance, Double> avgPriceColumn;
    @FXML private TableColumn<CategoryPerformance, Double> percentageColumn;
    
    @FXML private PieChart categoryValueChart; 
    @FXML private BarChart<String, Number> categoryComparisonChart;
    
    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalInventoryValueLabel;
    
    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private Label statusLabel;

    // Adding potential fields that might exist in the FXML but aren't declared here
    @FXML private Button exportButton;
    @FXML private Button printButton;
    @FXML private Button clearButton;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private CheckBox includeZeroStockCheckbox;
    
    private Map<String, List<Product>> productsByCategory;
    private InventoryDAO inventoryDAO;
    private double totalInventoryValue = 0.0;
    private ObservableList<CategoryPerformance> categoryPerformanceList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // This method will be called by the FXMLLoader when the view is loaded
        System.out.println("CategoryReportController initialized");
        
        // Set up event handlers for any buttons that might exist in the FXML
        if (exportButton != null) {
            exportButton.setOnAction(event -> handleExport(event));
        }
        
        if (printButton != null) {
            printButton.setOnAction(event -> handlePrint(event));
        }
        
        if (clearButton != null) {
            clearButton.setOnAction(event -> handleClear(event));
        }
        
        // Set up any ComboBoxes
        if (categoryFilterCombo != null) {
            categoryFilterCombo.setOnAction(event -> handleCategoryFilterChange());
        }
        
        // Initialize filter type combo box
        if (filterTypeComboBox != null) {
            filterTypeComboBox.setItems(FXCollections.observableArrayList(
                "Nombre", "Descripción", "ID", "Valor"
            ));
            filterTypeComboBox.setValue("Nombre");
        }
        
        // Set up search button handler
        if (searchButton != null) {
            searchButton.setOnAction(event -> handleFilterSearch());
        }
        
        // Set up the new apply filter button handler
        if (applyFilterButton != null) {
            applyFilterButton.setOnAction(event -> handleApplySelectedFilter());
        }
        
        // Set up close button handler
        if (closeButton != null) {
            closeButton.setOnAction(event -> handleClose());
        }
        
        // Initialize products table columns if they exist
        initializeProductColumns();
    }
    
    public void initData(Map<String, List<Product>> productsByCategory, InventoryDAO inventoryDAO) {
        if (productsByCategory == null || inventoryDAO == null) {
            System.err.println("ERROR: Cannot initialize with null data");
            return;
        }
        
        this.productsByCategory = productsByCategory;
        this.inventoryDAO = inventoryDAO;
        
        // Create and set up the table programmatically to avoid null pointer issues
        createAndSetupTableColumns();
        
        calculateMetrics();
        populateTable();
        createCharts();
        updateSummaryLabels();
        
        // Setup category table events for interaction
        setupCategoryTableEvents();
        
        // Initialize product table columns
        initializeProductColumns();
    }
    
    /**
     * Creates and sets up table columns programmatically to avoid NullPointerExceptions
     */
    private void createAndSetupTableColumns() {
        if (categoryTable == null) {
            System.err.println("ERROR: categoryTable is null. Cannot create columns.");
            return;
        }
        
        // Clear any existing columns to avoid duplicates
        categoryTable.getColumns().clear();
        
        // Create columns programmatically
        TableColumn<CategoryPerformance, String> catColumn = new TableColumn<>("Categoría");
        catColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        
        TableColumn<CategoryPerformance, Integer> prodCountColumn = new TableColumn<>("Cantidad de Productos");
        prodCountColumn.setCellValueFactory(data -> data.getValue().productCountProperty().asObject());
        
        TableColumn<CategoryPerformance, Integer> stockColumn = new TableColumn<>("Stock Total");
        stockColumn.setCellValueFactory(data -> data.getValue().totalStockProperty().asObject());
        
        TableColumn<CategoryPerformance, Double> valueColumn = new TableColumn<>("Valor de Inventario");
        valueColumn.setCellValueFactory(data -> data.getValue().inventoryValueProperty().asObject());
        valueColumn.setCellFactory(column -> new TableCell<CategoryPerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                    setText(currencyFormat.format(item));
                }
            }
        });
        
        TableColumn<CategoryPerformance, Double> avgColumn = new TableColumn<>("Precio Promedio");
        avgColumn.setCellValueFactory(data -> data.getValue().avgPriceProperty().asObject());
        avgColumn.setCellFactory(column -> new TableCell<CategoryPerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                    setText(currencyFormat.format(item));
                }
            }
        });
        
        TableColumn<CategoryPerformance, Double> pctColumn = new TableColumn<>("Porcentaje");
        pctColumn.setCellValueFactory(data -> data.getValue().percentageProperty().asObject());
        pctColumn.setCellFactory(column -> new TableCell<CategoryPerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item));
                }
            }
        });
        
        // Add columns to table
        categoryTable.getColumns().addAll(catColumn, prodCountColumn, stockColumn, valueColumn, avgColumn, pctColumn);
        
        // Store references to the columns (in case other methods use them)
        categoryColumn = catColumn;
        productCountColumn = prodCountColumn;
        totalStockColumn = stockColumn;
        inventoryValueColumn = valueColumn;
        avgPriceColumn = avgColumn;
        percentageColumn = pctColumn;
    }
    
    private void calculateMetrics() {
        // Calculate total inventory value first
        totalInventoryValue = productsByCategory.values().stream()
                .flatMap(List::stream)
                .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
                .sum();
        
        // Process each category
        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey().isEmpty() ? "Sin categoría" : entry.getKey();
            List<Product> products = entry.getValue();
            
            int productCount = products.size();
            int totalStock = products.stream().mapToInt(Product::getStockQuantity).sum();
            double categoryValue = products.stream()
                    .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
                    .sum();
            double avgPrice = products.stream()
                    .mapToDouble(Product::getSellingPrice)
                    .average()
                    .orElse(0.0);
            double percentage = (totalInventoryValue > 0) ? (categoryValue / totalInventoryValue * 100) : 0.0;
            
            categoryPerformanceList.add(new CategoryPerformance(
                    category, productCount, totalStock, categoryValue, avgPrice, percentage));
        }
        
        // Sort by inventory value (descending)
        categoryPerformanceList.sort(Comparator.comparing(CategoryPerformance::getInventoryValue).reversed());
    }
    
    private void populateTable() {
        if (categoryTable != null) {
            categoryTable.setItems(categoryPerformanceList);
        } else {
            System.err.println("WARNING: categoryTable is null. Check if fx:id is properly set in FXML.");
        }
    }
    
    private void createCharts() {
        if (categoryValueChart == null || categoryComparisonChart == null) {
            System.err.println("WARNING: One or more charts are null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        // Clear existing chart data
        categoryValueChart.getData().clear();
        categoryComparisonChart.getData().clear();
        
        // Pie chart for inventory value distribution
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // For pie chart readability, limit to top categories and group the rest
        List<CategoryPerformance> topCategories = new ArrayList<>(categoryPerformanceList);
        if (topCategories.size() > 5) {
            double otherValue = 0;
            for (int i = 5; i < topCategories.size(); i++) {
                otherValue += topCategories.get(i).getInventoryValue();
            }
            
            for (int i = 0; i < 5; i++) {
                if (i < topCategories.size()) {
                    CategoryPerformance category = topCategories.get(i);
                    pieChartData.add(new PieChart.Data(category.getCategory(), category.getInventoryValue()));
                }
            }
            
            if (otherValue > 0) {
                pieChartData.add(new PieChart.Data("Otros", otherValue));
            }
        } else {
            for (CategoryPerformance category : topCategories) {
                pieChartData.add(new PieChart.Data(category.getCategory(), category.getInventoryValue()));
            }
        }
        
        categoryValueChart.setData(pieChartData);
        categoryValueChart.setTitle("Valor de Inventario por Categoría");
        
        // Bar chart comparison (product count vs inventory value)
        XYChart.Series<String, Number> productCountSeries = new XYChart.Series<>();
        productCountSeries.setName("Cantidad de Productos");
        
        XYChart.Series<String, Number> inventoryValueSeries = new XYChart.Series<>();
        inventoryValueSeries.setName("Valor de Inventario (x100)");
        
        // Limit to top 8 categories for readability
        List<CategoryPerformance> barChartCategories = categoryPerformanceList.stream()
            .limit(8)
            .collect(Collectors.toList());
            
        for (CategoryPerformance category : barChartCategories) {
            productCountSeries.getData().add(
                new XYChart.Data<>(category.getCategory(), category.getProductCount()));
                
            // Scale down inventory value for better visualization
            inventoryValueSeries.getData().add(
                new XYChart.Data<>(category.getCategory(), category.getInventoryValue() / 100));
        }
        
        categoryComparisonChart.getData().addAll(productCountSeries, inventoryValueSeries);
        categoryComparisonChart.setTitle("Comparación de Categorías");
    }
    
    private void updateSummaryLabels() {
        if (totalCategoriesLabel != null) {
            totalCategoriesLabel.setText(String.valueOf(productsByCategory.size()));
        } else {
            System.err.println("WARNING: totalCategoriesLabel is null. Check if fx:id is properly set in FXML.");
        }
        
        int totalProducts = productsByCategory.values().stream()
                .mapToInt(List::size)
                .sum();
                
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(totalProducts));
        } else {
            System.err.println("WARNING: totalProductsLabel is null. Check if fx:id is properly set in FXML.");
        }
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        if (totalInventoryValueLabel != null) {
            totalInventoryValueLabel.setText(currencyFormat.format(totalInventoryValue));
        } else {
            System.err.println("WARNING: totalInventoryValueLabel is null. Check if fx:id is properly set in FXML.");
        }
    }
    
    @FXML
    private void handleExportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Reporte");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("reporte_categorias.xlsx");
        
        Window owner = null;
        if (categoryTable != null) owner = categoryTable.getScene().getWindow();
        else if (categoryValueChart != null) owner = categoryValueChart.getScene().getWindow();
        else if (categoryComparisonChart != null) owner = categoryComparisonChart.getScene().getWindow();
        
        if (owner == null) {
            System.err.println("WARNING: Cannot find a valid window for file dialog.");
            return;
        }
        
        File file = fileChooser.showSaveDialog((Stage)owner);
        
        if (file != null) {
            // Mostrar mensaje informativo - la exportación real requeriría una librería como Apache POI
            showMessage("Exportación", "La exportación a Excel será implementada en una versión futura.");
        }
    }
    
    @FXML
    private void handleClose() {
        if (categoryTable != null && categoryTable.getScene() != null && categoryTable.getScene().getWindow() != null) {
            Stage stage = (Stage) categoryTable.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("WARNING: Cannot find stage to close.");
        }
    }
    
    @FXML
    private void handleSearch() {
        // Add null check for searchField
        if (searchField == null) {
            System.err.println("WARNING: searchField is null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            // If search is empty, show all products
            ObservableList<Product> allProducts = FXCollections.observableArrayList();
            for (List<Product> products : productsByCategory.values()) {
                allProducts.addAll(products);
            }
            
            // Check if productsTable is not null before setting items
            if (productsTable != null) {
                productsTable.setItems(allProducts);
                if (statusLabel != null) {
                    statusLabel.setText("Mostrando todos los productos");
                }
            } else {
                System.err.println("WARNING: productsTable is null. Check if fx:id is properly set in FXML.");
            }
        } else {
            // Filter products by search query
            List<Product> searchResults = inventoryDAO.searchProducts(query);
            
            // Check if productsTable is not null before setting items
            if (productsTable != null) {
                productsTable.setItems(FXCollections.observableArrayList(searchResults));
                if (statusLabel != null) {
                    statusLabel.setText("Se encontraron " + searchResults.size() + " productos");
                }
            } else {
                System.err.println("WARNING: productsTable is null. Check if fx:id is properly set in FXML.");
            }
        }
    }

    @FXML
    public void handleClear(ActionEvent actionEvent) {
        // Add null check for searchField
        if (searchField != null) {
            searchField.clear();
            handleSearch(); // Call handleSearch to refresh the view
        } else {
            System.err.println("WARNING: searchField is null. Check if fx:id is properly set in FXML.");
        }
    }

    @FXML
    public void handleExport(ActionEvent actionEvent) {
        handleExportReport();  // Reuse existing export method
    }

    @FXML
    public void handlePrint(ActionEvent actionEvent) {
        showMessage("Imprimir", "La funcionalidad de impresión será implementada en una versión futura.");
    }
    
    @FXML
    private void handleCategoryFilterChange() {
        if (categoryFilterCombo == null) {
            System.err.println("WARNING: categoryFilterCombo is null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        String selectedCategory = categoryFilterCombo.getValue();
        if (selectedCategory == null || selectedCategory.equals("Todas")) {
            // Show all categories
            populateTable();
        } else {
            // Filter for selected category
            ObservableList<CategoryPerformance> filtered = categoryPerformanceList.filtered(
                category -> category.getCategory().equals(selectedCategory)
            );
            
            if (categoryTable != null) {
                categoryTable.setItems(filtered);
            }
        }
    }
    
    @FXML
    private void handleDateRangeChange() {
        // Handle date range change if date pickers are used in the FXML
        if (fromDatePicker == null || toDatePicker == null) {
            System.err.println("WARNING: Date pickers are null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        // Date range filter implementation would go here
        showMessage("Filtro de fechas", "El filtro por rango de fechas será implementado en una versión futura.");
    }
    
    private void showMessage(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Helper class to hold category performance data
    public static class CategoryPerformance {
        private final SimpleStringProperty category;
        private final SimpleIntegerProperty productCount;
        private final SimpleIntegerProperty totalStock;
        private final SimpleDoubleProperty inventoryValue;
        private final SimpleDoubleProperty avgPrice;
        private final SimpleDoubleProperty percentage;
        
        public CategoryPerformance(String category, int productCount, int totalStock, 
                                   double inventoryValue, double avgPrice, double percentage) {
            this.category = new SimpleStringProperty(category);
            this.productCount = new SimpleIntegerProperty(productCount);
            this.totalStock = new SimpleIntegerProperty(totalStock);
            this.inventoryValue = new SimpleDoubleProperty(inventoryValue);
            this.avgPrice = new SimpleDoubleProperty(avgPrice);
            this.percentage = new SimpleDoubleProperty(percentage);
        }
        
        public String getCategory() {
            return category.get();
        }
        
        public SimpleStringProperty categoryProperty() {
            return category;
        }
        
        public int getProductCount() {
            return productCount.get();
        }
        
        public SimpleIntegerProperty productCountProperty() {
            return productCount;
        }
        
        public int getTotalStock() {
            return totalStock.get();
        }
        
        public SimpleIntegerProperty totalStockProperty() {
            return totalStock;
        }
        
        public double getInventoryValue() {
            return inventoryValue.get();
        }
        
        public SimpleDoubleProperty inventoryValueProperty() {
            return inventoryValue;
        }
        
        public double getAvgPrice() {
            return avgPrice.get();
        }
        
        public SimpleDoubleProperty avgPriceProperty() {
            return avgPrice;
        }
        
        public double getPercentage() {
            return percentage.get();
        }
        
        public SimpleDoubleProperty percentageProperty() {
            return percentage;
        }
    }
    
    /**
     * Initialize columns for the product details table
     */
    private void initializeProductColumns() {
        if (productsTable != null) {
            // Clear existing columns to avoid duplicates
            if (productsTable.getColumns() != null) {
                productsTable.getColumns().clear();
            }
            
            TableColumn<Product, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getBarcode()));
            
            TableColumn<Product, String> nameCol = new TableColumn<>("Nombre");
            nameCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getName()));
            nameCol.setPrefWidth(200);
            
            TableColumn<Product, String> descCol = new TableColumn<>("Descripción");
            descCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getDescription() != null ? 
                    data.getValue().getDescription() : ""));
            descCol.setPrefWidth(200);
            
            TableColumn<Product, Double> priceCol = new TableColumn<>("Precio");
            priceCol.setCellValueFactory(data -> 
                new SimpleDoubleProperty(data.getValue().getSellingPrice()).asObject());
            priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", price));
                    }
                }
            });
            
            TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
            stockCol.setCellValueFactory(data -> 
                new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());
                
            TableColumn<Product, String> categoryCol = new TableColumn<>("Categoría");
            categoryCol.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getCategory()));
            
            TableColumn<Product, Double> valueCol = new TableColumn<>("Valor Total");
            valueCol.setCellValueFactory(data -> {
                double value = data.getValue().getStockQuantity() * data.getValue().getSellingPrice();
                return new SimpleDoubleProperty(value).asObject();
            });
            valueCol.setCellFactory(col -> new TableCell<Product, Double>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    super.updateItem(value, empty);
                    if (empty || value == null) {
                        setText(null);
                    } else {
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                        setText(currencyFormat.format(value));
                    }
                }
            });
            
            // Add columns to table
            productsTable.getColumns().addAll(
                idCol, nameCol, descCol, priceCol, stockCol, categoryCol, valueCol
            );
            
            // Store references if needed
            idColumn = idCol;
            nameColumn = nameCol;
            descriptionColumn = descCol;
        }
    }
    
    /**
     * Handle search button click for the filter section
     */
    @FXML
    private void handleFilterSearch() {
        if (filterField == null || filterTypeComboBox == null) {
            System.err.println("WARNING: Filter components are null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        String filterText = filterField.getText().trim();
        
        // Always show complete dataset if filter is empty
        if (filterText.isEmpty()) {
            if (categoryTable != null) {
                categoryTable.setItems(categoryPerformanceList);  // Reset to full list
                if (statusLabel != null) {
                    statusLabel.setText("Mostrando todas las categorías (" + categoryPerformanceList.size() + ")");
                }
            }
            return;
        }
        
        // Get filter type (with null check)
        String filterType = filterTypeComboBox.getValue() != null ? 
                            (String) filterTypeComboBox.getValue() : "Nombre";
        
        // Create filtered list with debug output
        System.out.println("Applying filter: " + filterType + " with value: " + filterText);
        
        // Filter category list based on the filter type and text - completely rewritten for reliability
        ObservableList<CategoryPerformance> filteredCategories = FXCollections.observableArrayList();
        
        for (CategoryPerformance category : categoryPerformanceList) {
            boolean matches = false;
            
            switch (filterType) {
                case "Nombre":
                    if (category.getCategory().toLowerCase().contains(filterText.toLowerCase())) {
                        matches = true;
                        System.out.println("Match found (Nombre): " + category.getCategory());
                    }
                    break;
                    
                case "Descripción":
                    // Assuming there's no description field, just use category name as fallback
                    if (category.getCategory().toLowerCase().contains(filterText.toLowerCase())) {
                        matches = true;
                        System.out.println("Match found (Descripción): " + category.getCategory());
                    }
                    break;
                    
                case "ID":
                    // Try to match products in the category by ID
                    List<Product> products = productsByCategory.get(category.getCategory());
                    if (products != null) {
                        for (Product p : products) {
                            if (p.getBarcode() != null && p.getBarcode().contains(filterText)) {
                                matches = true;
                                System.out.println("Match found (ID) in category: " + category.getCategory());
                                break;
                            }
                        }
                    }
                    break;
                    
                case "Valor":
                    try {
                        double filterValue = Double.parseDouble(filterText);
                        if (category.getInventoryValue() >= filterValue) {
                            matches = true;
                            System.out.println("Match found (Valor): " + category.getCategory() + 
                                               " value: " + category.getInventoryValue());
                        }
                    } catch (NumberFormatException e) {
                        // If we can't parse the number, no match
                    }
                    break;
                    
                default:
                    // Default to name search
                    if (category.getCategory().toLowerCase().contains(filterText.toLowerCase())) {
                        matches = true;
                    }
                    break;
            }
            
            if (matches) {
                filteredCategories.add(category);
            }
        }
        
        // Update the table with the filtered list
        if (categoryTable != null) {
            // First check if we got any results
            if (filteredCategories.isEmpty()) {
                // We could either show an empty table or keep the current items
                categoryTable.setItems(FXCollections.observableArrayList()); // Show empty table
                if (statusLabel != null) {
                    statusLabel.setText("No se encontraron categorías con el filtro aplicado: " + filterType + " = " + filterText);
                }
            } else {
                categoryTable.setItems(filteredCategories);
                if (statusLabel != null) {
                    statusLabel.setText(String.format("Se encontraron %d categorías con el filtro: %s = '%s'", 
                                       filteredCategories.size(), filterType, filterText));
                }
            }
        }
    }

    /**
     * Handler for the apply filter button - applies the selected filter from the combo box
     */
    @FXML
    private void handleApplySelectedFilter() {
        if (filterTypeComboBox == null) {
            System.err.println("WARNING: filterTypeComboBox is null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        String selectedFilter = (String) filterTypeComboBox.getValue();
        if (selectedFilter == null) {
            // Just show a status message instead of a popup
            if (statusLabel != null) {
                statusLabel.setText("Por favor, seleccione un tipo de filtro");
            }
            return;
        }
        
        // Apply the filter using the improved handleFilterSearch method
        handleFilterSearch();
    }
    
    /**
     * Shows products from the selected category in the products table
     */
    @FXML
    private void handleShowCategoryProducts() {
        if (categoryTable == null || productsTable == null) {
            System.err.println("WARNING: Tables are null. Check if fx:id is properly set in FXML.");
            return;
        }
        
        CategoryPerformance selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            if (statusLabel != null) {
                statusLabel.setText("Seleccione una categoría para ver sus productos");
            }
            return;
        }
        
        String categoryName = selectedCategory.getCategory();
        List<Product> products = productsByCategory.get(categoryName);
        
        if (products != null && !products.isEmpty()) {
            productsTable.setItems(FXCollections.observableArrayList(products));
            if (statusLabel != null) {
                statusLabel.setText(String.format("Mostrando %d productos de la categoría '%s'", 
                                   products.size(), categoryName));
            }
        } else {
            productsTable.setItems(FXCollections.observableArrayList());
            if (statusLabel != null) {
                statusLabel.setText("No hay productos en esta categoría");
            }
        }
    }
    
    /**
     * Double-click handler for category table
     */
    private void setupCategoryTableEvents() {
        if (categoryTable == null) return;
        
        // Add row double-click handler
        categoryTable.setRowFactory(tv -> {
            TableRow<CategoryPerformance> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleShowCategoryProducts();
                }
            });
            return row;
        });
    }
}
