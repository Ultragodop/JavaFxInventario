package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryValueDetailsController {

    @FXML private BorderPane rootPane;
    @FXML private Label totalValueLabel;
    @FXML private Label totalProductsLabel;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private DatePicker expirationFilterDatePicker;
    
    // Resumen por categorías
    @FXML private TableView<CategoryValue> categoryTable;
    @FXML private TableColumn<CategoryValue, String> categoryNameColumn;
    @FXML private TableColumn<CategoryValue, Integer> categoryCountColumn;
    @FXML private TableColumn<CategoryValue, Double> categoryValueColumn;
    
    // Listado detallado
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Double> totalValueColumn;
    @FXML private TableColumn<Product, Date> expirationColumn;
    
    // Alertas
    @FXML private TableView<Product> alertsTable;
    @FXML private TableColumn<Product, String> alertCodeColumn;
    @FXML private TableColumn<Product, String> alertNameColumn;
    @FXML private TableColumn<Product, String> alertTypeColumn;
    @FXML private TableColumn<Product, String> alertDescriptionColumn;
    
    // Gráficos
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> topProductsBarChart;
    
    // Botones de acción
    @FXML private Button exportButton;
    @FXML private Button printButton;
    
    private ObservableList<Product> allProducts;
    private FilteredList<Product> filteredProducts;
    private ObservableList<CategoryValue> categoryValues = FXCollections.observableArrayList();
    private ObservableList<Product> alertProducts = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inicializa las columnas del resumen de categorías
        categoryNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        categoryCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCount()).asObject());
        categoryValueColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getValue()).asObject());
        
        // Inicializa las columnas de la tabla de productos
        codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBarcode()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        stockColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStockQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSellingPrice()).asObject());
        totalValueColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getStockQuantity() * cellData.getValue().getSellingPrice()).asObject());
        expirationColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getExpirationDate()));
        
        // Formatea la columna de fecha de vencimiento
        expirationColumn.setCellFactory(column -> new TableCell<Product, Date>() {
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
        });
        
        // Inicializa las columnas de alertas
        alertCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBarcode()));
        alertNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        alertTypeColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product.getStockQuantity() <= product.getReorderLevel()) {
                return new SimpleStringProperty("Stock Bajo");
            } else if (product.getExpirationDate() != null && 
                      product.getExpirationDate().before(
                          Date.from(LocalDate.now().plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                return new SimpleStringProperty("Próximo a vencer");
            } else if (product.getStockQuantity() > product.getReorderLevel() * 3) {
                return new SimpleStringProperty("Exceso de stock");
            }
            return new SimpleStringProperty("Normal");
        });
        alertDescriptionColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product.getStockQuantity() <= product.getReorderLevel()) {
                return new SimpleStringProperty("Stock: " + product.getStockQuantity() + 
                                              ", Nivel mínimo: " + product.getReorderLevel());
            } else if (product.getExpirationDate() != null && 
                      product.getExpirationDate().before(
                          Date.from(LocalDate.now().plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                return new SimpleStringProperty("Vence el: " + format.format(product.getExpirationDate()));
            } else if (product.getStockQuantity() > product.getReorderLevel() * 3) {
                return new SimpleStringProperty("Stock: " + product.getStockQuantity() + 
                                              ", Excede nivel óptimo");
            }
            return new SimpleStringProperty("");
        });
        
        // Configurar filtro de categoría
        filterCategoryCombo.getItems().add("Todas las categorías");
        filterCategoryCombo.setValue("Todas las categorías");
        filterCategoryCombo.setOnAction(event -> applyFilters());
        
        // Configurar filtro de fecha de vencimiento
        expirationFilterDatePicker.setValue(null);
        expirationFilterDatePicker.setOnAction(event -> applyFilters());
        
        // Configurar botones de acción
        exportButton.setOnAction(event -> handleExport());
        printButton.setOnAction(event -> handlePrint());
    }
    
    public void initData(ObservableList<Product> products, double totalValue, int totalProductCount, List<String> categories) {
        // Guardar datos y configurar lista filtrada
        this.allProducts = products;
        this.filteredProducts = new FilteredList<>(allProducts, p -> true);
        productsTable.setItems(filteredProducts);
        
        // Actualizar etiquetas de resumen
        totalValueLabel.setText(String.format("$%.2f", totalValue));
        totalProductsLabel.setText(String.valueOf(totalProductCount));
        
        // Cargar categorías en el combo
        filterCategoryCombo.getItems().setAll("Todas las categorías");
        filterCategoryCombo.getItems().addAll(categories);
        
        // Calcular valores por categoría
        calculateCategoryValues();
        
        // Identificar productos en alerta
        identifyAlertProducts();
        
        // Generar gráficos
        generateCharts();
    }
    
    private void calculateCategoryValues() {
        Map<String, List<Product>> productsByCategory = allProducts.stream()
            .collect(Collectors.groupingBy(Product::getCategory));
            
        categoryValues.clear();
        
        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Product> products = entry.getValue();
            
            int count = products.size();
            double value = products.stream()
                .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
                .sum();
                
            categoryValues.add(new CategoryValue(category, count, value));
        }
        
        // Ordenar por valor (descendente)
        categoryValues.sort(Comparator.comparing(CategoryValue::getValue).reversed());
        
        categoryTable.setItems(categoryValues);
    }
    
    private void identifyAlertProducts() {
        alertProducts.clear();
        
        // Productos con stock bajo
        List<Product> lowStockProducts = allProducts.stream()
            .filter(p -> p.getStockQuantity() <= p.getReorderLevel())
            .collect(Collectors.toList());
        
        // Productos próximos a vencer (15 días)
        List<Product> expiringProducts = allProducts.stream()
            .filter(p -> p.getExpirationDate() != null && 
                   p.getExpirationDate().before(
                       Date.from(LocalDate.now().plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant())))
            .collect(Collectors.toList());
        
        // Productos en exceso (3 veces el nivel de reorden)
        List<Product> excessProducts = allProducts.stream()
            .filter(p -> p.getStockQuantity() > p.getReorderLevel() * 3)
            .collect(Collectors.toList());
        
        // Combinar todas las alertas (pueden haber duplicados si un producto tiene más de un tipo de alerta)
        Set<Product> uniqueAlerts = new HashSet<>();
        uniqueAlerts.addAll(lowStockProducts);
        uniqueAlerts.addAll(expiringProducts);
        uniqueAlerts.addAll(excessProducts);
        
        alertProducts.addAll(uniqueAlerts);
        alertsTable.setItems(alertProducts);
    }
    
    private void generateCharts() {
        // Generar gráfico circular de categorías
        categoryPieChart.getData().clear();
        for (CategoryValue cv : categoryValues) {
            PieChart.Data slice = new PieChart.Data(cv.getCategory(), cv.getValue());
            categoryPieChart.getData().add(slice);
        }
        
        // Generar gráfico de barras de los productos con mayor valor
        topProductsBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valor en Inventario");
        
        // Top 10 productos por valor
        List<Product> topProducts = allProducts.stream()
            .sorted(Comparator.comparing(p -> -(p.getStockQuantity() * p.getSellingPrice())))
            .limit(10)
            .collect(Collectors.toList());
            
        for (Product p : topProducts) {
            double value = p.getStockQuantity() * p.getSellingPrice();
            series.getData().add(new XYChart.Data<>(p.getName(), value));
        }
        
        topProductsBarChart.getData().add(series);
    }
    
    private void applyFilters() {
        String selectedCategory = filterCategoryCombo.getValue();
        LocalDate selectedDate = expirationFilterDatePicker.getValue();
        
        filteredProducts.setPredicate(product -> {
            // Filtrar por categoría si no es "Todas las categorías"
            boolean matchesCategory = "Todas las categorías".equals(selectedCategory) || 
                                     selectedCategory.equals(product.getCategory());
            
            // Filtrar por fecha de vencimiento si está seleccionada
            boolean matchesExpiration = selectedDate == null || 
                                      (product.getExpirationDate() != null && 
                                       !product.getExpirationDate().before(
                                           Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            
            return matchesCategory && matchesExpiration;
        });
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Reporte de Inventario");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("reporte_inventario.xlsx");
        
        Stage stage = (Stage) rootPane.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        
        if (selectedFile != null) {
            // Aquí iría la lógica de exportación a Excel o PDF
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación");
            alert.setHeaderText(null);
            alert.setContentText("La funcionalidad de exportación no está implementada completamente. Se ha seleccionado el archivo: " + selectedFile.getName());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handlePrint() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Imprimir");
        alert.setHeaderText(null);
        alert.setContentText("La funcionalidad de impresión no está implementada.");
        alert.showAndWait();
    }
    
    // Clase interna para representar el valor por categoría
    public static class CategoryValue {
        private final String category;
        private final int count;
        private final double value;
        
        public CategoryValue(String category, int count, double value) {
            this.category = category;
            this.count = count;
            this.value = value;
        }
        
        public String getCategory() { return category; }
        public int getCount() { return count; }
        public double getValue() { return value; }
    }
}

