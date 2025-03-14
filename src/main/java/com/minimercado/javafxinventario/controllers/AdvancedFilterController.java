package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AdvancedFilterController {

    @FXML private ComboBox<String> categoryCombo;
    // Modificar para usar dos sliders separados
    @FXML private Slider minPriceSlider;
    @FXML private Slider maxPriceSlider;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private TextField minStockField;
    @FXML private TextField maxStockField;
    @FXML private ComboBox<String> expiryDateCombo;
    @FXML private ComboBox<String> supplierCombo;
    @FXML private CheckBox promotionCheckBox;
    
    @FXML private Button applyButton;
    @FXML private Button clearButton;
    @FXML private Button cancelButton;
    
    private InventoryDAO inventoryDAO;
    private ObservableList<Product> allProducts;
    private Consumer<List<Product>> filterCallback;
    private double globalMinPrice = 0;
    private double globalMaxPrice = 100000;
    
    @FXML
    public void initialize() {
        // Inicializar opciones para el combobox de vencimiento
        expiryDateCombo.setItems(FXCollections.observableArrayList(
            "Todos", "Próximos 7 días", "Próximos 30 días", "Vencido", "Sin vencimiento"
        ));
        expiryDateCombo.setValue("Todos");
        
        // Configurar listeners para actualizar campos de texto según los sliders
        minPriceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            if (value > maxPriceSlider.getValue()) {
                minPriceSlider.setValue(maxPriceSlider.getValue());
                value = maxPriceSlider.getValue();
            }
            minPriceField.setText(String.format("%.2f", value));
        });
        
        maxPriceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            if (value < minPriceSlider.getValue()) {
                maxPriceSlider.setValue(minPriceSlider.getValue());
                value = minPriceSlider.getValue();
            }
            maxPriceField.setText(String.format("%.2f", value));
        });
        
        // Listener para los campos de texto que actualizan los sliders
        minPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double value = Double.parseDouble(newVal);
                if (value <= maxPriceSlider.getValue()) {
                    minPriceSlider.setValue(value);
                }
            } catch (NumberFormatException e) {
                // Ignorar entrada no numérica
            }
        });
        
        maxPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double value = Double.parseDouble(newVal);
                if (value >= minPriceSlider.getValue()) {
                    maxPriceSlider.setValue(value);
                }
            } catch (NumberFormatException e) {
                // Ignorar entrada no numérica
            }
        });
        
        // Configurar botones
        applyButton.setOnAction(e -> handleApplyFilter());
        clearButton.setOnAction(e -> handleClearFilter());
        cancelButton.setOnAction(e -> handleCancel());
    }
    
    public void initData(InventoryDAO dao, ObservableList<Product> products) {
        this.inventoryDAO = dao;
        this.allProducts = products;
        
        // Cargar categorías
        loadCategories();
        
        // Cargar proveedores
        loadSuppliers();
        
        // Configurar rango de precios
        setupPriceRange();
    }
    
    public void setFilterCallback(Consumer<List<Product>> callback) {
        this.filterCallback = callback;
    }
    
    private void loadCategories() {
        List<String> categories = inventoryDAO.getDistinctCategories();
        categories.add(0, "Todas las categorías");
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setValue("Todas las categorías");
    }
    
    private void loadSuppliers() {
        List<String> supplierNames = new ArrayList<>();
        supplierNames.add("Todos los proveedores");
        
        List<Supplier> suppliers = inventoryDAO.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierNames.add(supplier.getName());
        }
        
        supplierCombo.setItems(FXCollections.observableArrayList(supplierNames));
        supplierCombo.setValue("Todos los proveedores");
    }
    
    private void setupPriceRange() {
        // Encontrar precios mínimos y máximos en los productos
        globalMinPrice = 0;
        globalMaxPrice = 100000; // Valor por defecto alto
        
        if (!allProducts.isEmpty()) {
            globalMinPrice = allProducts.stream()
                .mapToDouble(Product::getSellingPrice)
                .min()
                .orElse(0);
            
            globalMaxPrice = allProducts.stream()
                .mapToDouble(Product::getSellingPrice)
                .max()
                .orElse(100000);
            
            // Agregar un margen
            globalMaxPrice = globalMaxPrice * 1.1;
        }
        
        // Configurar los sliders
        minPriceSlider.setMin(globalMinPrice);
        minPriceSlider.setMax(globalMaxPrice);
        minPriceSlider.setValue(globalMinPrice);
        
        maxPriceSlider.setMin(globalMinPrice);
        maxPriceSlider.setMax(globalMaxPrice);
        maxPriceSlider.setValue(globalMaxPrice);
        
        // Actualizar los campos de texto
        minPriceField.setText(String.format("%.2f", globalMinPrice));
        maxPriceField.setText(String.format("%.2f", globalMaxPrice));
    }
    
    @FXML
    private void handleApplyFilter() {
        try {
            // Obtener todos los criterios de filtro
            String category = categoryCombo.getValue();
            double minPrice = Double.parseDouble(minPriceField.getText());
            double maxPrice = Double.parseDouble(maxPriceField.getText());
            
            int minStock = minStockField.getText().isEmpty() ? 0 : Integer.parseInt(minStockField.getText());
            int maxStock = maxStockField.getText().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(maxStockField.getText());
            
            String expiryOption = expiryDateCombo.getValue();
            String supplier = supplierCombo.getValue();
            boolean promotion = promotionCheckBox.isSelected();
            
            // Filtrar productos
            List<Product> filteredProducts = allProducts.stream()
                .filter(p -> categoryFilter(p, category))
                .filter(p -> priceFilter(p, minPrice, maxPrice))
                .filter(p -> stockFilter(p, minStock, maxStock))
                .filter(p -> expiryFilter(p, expiryOption))
                .filter(p -> supplierFilter(p, supplier))
                .filter(p -> promotionFilter(p, promotion))
                .collect(Collectors.toList());
            
            // Invocar callback con los resultados
            if (filterCallback != null) {
                filterCallback.accept(filteredProducts);
            }
            
            // Cerrar la ventana
            closeWindow();
        } catch (Exception e) {
            showError("Error al aplicar filtros: " + e.getMessage());
        }
    }
    
    private boolean categoryFilter(Product p, String category) {
        return "Todas las categorías".equals(category) || category.equals(p.getCategory());
    }
    
    private boolean priceFilter(Product p, double minPrice, double maxPrice) {
        double price = p.getSellingPrice();
        return price >= minPrice && price <= maxPrice;
    }
    
    private boolean stockFilter(Product p, int minStock, int maxStock) {
        int stock = p.getStockQuantity();
        return stock >= minStock && stock <= maxStock;
    }
    
    private boolean expiryFilter(Product p, String expiryOption) {
        if ("Todos".equals(expiryOption)) {
            return true;
        }
        
        Date expiryDate = p.getExpirationDate();
        if (expiryDate == null) {
            return "Sin vencimiento".equals(expiryOption);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate expiry = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        switch(expiryOption) {
            case "Próximos 7 días":
                return !expiry.isBefore(today) && expiry.isBefore(today.plusDays(7));
            case "Próximos 30 días":
                return !expiry.isBefore(today) && expiry.isBefore(today.plusDays(30));
            case "Vencido":
                return expiry.isBefore(today);
            default:
                return true;
        }
    }
    
    private boolean supplierFilter(Product p, String supplier) {
        return "Todos los proveedores".equals(supplier) || supplier.equals(p.getSupplier());
    }
    
    private boolean promotionFilter(Product p, boolean promotion) {
        if (!promotion) {
            return true; // Si no se solicita filtrar por promoción, mostrar todos
        }
        
        // Aquí se debe implementar lógica según cómo se determine si un producto está en promoción
        // Por ejemplo, podría basarse en si tiene descuento:
        return p.getDiscount() > 0;
    }
    
    @FXML
    private void handleClearFilter() {
        // Resetear todos los campos a sus valores por defecto
        categoryCombo.setValue("Todas las categorías");
        
        // Resetear slider y campos de precio
        minPriceSlider.setValue(globalMinPrice);
        maxPriceSlider.setValue(globalMaxPrice);
        minPriceField.setText(String.format("%.2f", globalMinPrice));
        maxPriceField.setText(String.format("%.2f", globalMaxPrice));
        
        minStockField.clear();
        maxStockField.clear();
        expiryDateCombo.setValue("Todos");
        supplierCombo.setValue("Todos los proveedores");
        promotionCheckBox.setSelected(false);
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
