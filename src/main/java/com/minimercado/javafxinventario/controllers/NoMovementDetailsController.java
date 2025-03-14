package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.InventoryMovement;
import javafx.beans.property.SimpleStringProperty;
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
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NoMovementDetailsController {

    @FXML private TableView<Product> noMovementTable;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> lastEntryDateColumn;
    @FXML private TableColumn<Product, String> lastSaleDateColumn;
    @FXML private TableColumn<Product, String> daysSinceLastSaleColumn;
    @FXML private TableColumn<Product, String> daysInInventoryColumn;
    @FXML private TableColumn<Product, Double> unitCostColumn;
    @FXML private TableColumn<Product, String> totalValueColumn;
    @FXML private TableColumn<Product, Double> salePriceColumn;
    @FXML private TableColumn<Product, String> expirationDateColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    
    @FXML private ComboBox<String> actionFilterComboBox;
    @FXML private TextField searchField;
    @FXML private Label totalProductsLabel;
    @FXML private Label totalValueLabel;
    @FXML private Button applyActionButton;
    @FXML private ComboBox<String> actionComboBox;
    @FXML private TextArea notesTextArea;
    
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Product> noMovementProducts = FXCollections.observableArrayList();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Initialize table columns
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Custom cell factories for dates and calculated fields
        lastEntryDateColumn.setCellValueFactory(cellData -> {
            Date lastEntry = inventoryDAO.getLastEntryDate(cellData.getValue().getBarcode());
            return new SimpleStringProperty(lastEntry != null ? dateFormat.format(lastEntry) : "N/A");
        });
        
        lastSaleDateColumn.setCellValueFactory(cellData -> {
            Date lastSale = inventoryDAO.getLastSaleDate(cellData.getValue().getBarcode());
            return new SimpleStringProperty(lastSale != null ? dateFormat.format(lastSale) : "Nunca");
        });
        
        daysSinceLastSaleColumn.setCellValueFactory(cellData -> {
            Date lastSale = inventoryDAO.getLastSaleDate(cellData.getValue().getBarcode());
            if (lastSale == null) {
                return new SimpleStringProperty("N/A");
            } else {
                long days = ChronoUnit.DAYS.between(
                    lastSale.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now());
                return new SimpleStringProperty(String.valueOf(days));
            }
        });
        
        daysInInventoryColumn.setCellValueFactory(cellData -> {
            Date lastEntry = inventoryDAO.getLastEntryDate(cellData.getValue().getBarcode());
            if (lastEntry == null) {
                return new SimpleStringProperty("N/A");
            } else {
                long days = ChronoUnit.DAYS.between(
                    lastEntry.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now());
                return new SimpleStringProperty(String.valueOf(days));
            }
        });
        
        unitCostColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        
        totalValueColumn.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getPurchasePrice() * cellData.getValue().getStockQuantity();
            return new SimpleStringProperty(String.format("$%.2f", total));
        });
        
        salePriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        
        expirationDateColumn.setCellValueFactory(cellData -> {
            Date expDate = cellData.getValue().getExpirationDate();
            return new SimpleStringProperty(expDate != null ? dateFormat.format(expDate) : "N/A");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Date expDate = cellData.getValue().getExpirationDate();
            if (expDate == null) {
                return new SimpleStringProperty("No perecedero");
            } else {
                LocalDate expLocalDate = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate now = LocalDate.now();
                if (expLocalDate.isBefore(now)) {
                    return new SimpleStringProperty("VENCIDO");
                } else if (expLocalDate.isBefore(now.plusDays(30))) {
                    return new SimpleStringProperty("PRÓXIMO A VENCER");
                } else {
                    return new SimpleStringProperty("OK");
                }
            }
        });
        
        // Add color formatting for status column
        statusColumn.setCellFactory(column -> {
            return new TableCell<Product, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("VENCIDO".equals(item)) {
                            setStyle("-fx-background-color: #ffcccc;");
                        } else if ("PRÓXIMO A VENCER".equals(item)) {
                            setStyle("-fx-background-color: #ffffcc;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
        });
        
        // Initialize action combo boxes
        actionComboBox.setItems(FXCollections.observableArrayList(
            "Promoción / Descuento", "Cambiar ubicación", "Devolución a proveedor", "Donación", "Baja de inventario"));
            
        actionFilterComboBox.setItems(FXCollections.observableArrayList(
            "Todos", "Vencidos", "Próximos a vencer", "Sin movimiento >30 días", "Sin movimiento >60 días", "Sin movimiento >90 días"));
        actionFilterComboBox.setValue("Todos");
        
        // Add listener for selection changes
        noMovementTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateNotes(newSelection);
            }
        });
        
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });
    }
    
    // Initialize data with products without movement
    public void initData(List<Product> products) {
        noMovementProducts.setAll(products);
        noMovementTable.setItems(noMovementProducts);
        updateSummary();
    }
    
    private void updateSummary() {
        int totalProducts = noMovementProducts.size();
        double totalValue = noMovementProducts.stream()
            .mapToDouble(p -> p.getPurchasePrice() * p.getStockQuantity())
            .sum();
            
        totalProductsLabel.setText("Total de productos: " + totalProducts);
        totalValueLabel.setText(String.format("Valor total: $%.2f", totalValue));
    }
    
    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            noMovementTable.setItems(noMovementProducts);
        } else {
            ObservableList<Product> filteredList = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();
            
            for (Product product : noMovementProducts) {
                if (product.getName().toLowerCase().contains(lowerCaseFilter) ||
                    product.getBarcode().toLowerCase().contains(lowerCaseFilter) ||
                    product.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    filteredList.add(product);
                }
            }
            noMovementTable.setItems(filteredList);
        }
    }
    
    @FXML
    private void handleApplyFilter() {
        String filter = actionFilterComboBox.getValue();
        if (filter == null || "Todos".equals(filter)) {
            noMovementTable.setItems(noMovementProducts);
            return;
        }
        
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        LocalDate now = LocalDate.now();
        
        for (Product product : noMovementProducts) {
            boolean includeProduct = false;
            
            switch(filter) {
                case "Vencidos":
                    Date expDate = product.getExpirationDate();
                    if (expDate != null) {
                        LocalDate expLocalDate = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        includeProduct = expLocalDate.isBefore(now);
                    }
                    break;
                    
                case "Próximos a vencer":
                    expDate = product.getExpirationDate();
                    if (expDate != null) {
                        LocalDate expLocalDate = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        includeProduct = !expLocalDate.isBefore(now) && expLocalDate.isBefore(now.plusDays(30));
                    }
                    break;
                    
                case "Sin movimiento >30 días":
                    Date lastSale = inventoryDAO.getLastSaleDate(product.getBarcode());
                    if (lastSale != null) {
                        LocalDate lastSaleDate = lastSale.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        includeProduct = ChronoUnit.DAYS.between(lastSaleDate, now) > 30;
                    } else {
                        includeProduct = true; // Never sold
                    }
                    break;
                    
                case "Sin movimiento >60 días":
                    lastSale = inventoryDAO.getLastSaleDate(product.getBarcode());
                    if (lastSale != null) {
                        LocalDate lastSaleDate = lastSale.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        includeProduct = ChronoUnit.DAYS.between(lastSaleDate, now) > 60;
                    } else {
                        includeProduct = true; // Never sold
                    }
                    break;
                    
                case "Sin movimiento >90 días":
                    lastSale = inventoryDAO.getLastSaleDate(product.getBarcode());
                    if (lastSale != null) {
                        LocalDate lastSaleDate = lastSale.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        includeProduct = ChronoUnit.DAYS.between(lastSaleDate, now) > 90;
                    } else {
                        includeProduct = true; // Never sold
                    }
                    break;
            }
            
            if (includeProduct) {
                filteredList.add(product);
            }
        }
        
        noMovementTable.setItems(filteredList);
        totalProductsLabel.setText("Total de productos filtrados: " + filteredList.size());
    }
    
    @FXML
    private void handleApplyAction() {
        Product selectedProduct = noMovementTable.getSelectionModel().getSelectedItem();
        String action = actionComboBox.getValue();
        
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida", 
                    "Por favor seleccione un producto para aplicar la acción.");
            return;
        }
        
        if (action == null) {
            showAlert(Alert.AlertType.WARNING, "Acción requerida", 
                    "Por favor seleccione una acción para aplicar al producto.");
            return;
        }
        
        String notes = notesTextArea.getText();
        
        // Here would typically be code to apply the selected action
        // For demonstration, we'll just show a confirmation
        String confirmMessage = String.format("¿Desea aplicar la acción '%s' al producto '%s'?", 
                action, selectedProduct.getName());
                
        if (showConfirmation("Confirmar acción", confirmMessage)) {
            // In a real implementation, you would:
            // 1. Update the product in the database
            // 2. Record the action in a log or history table
            // 3. If necessary, update inventory levels
            
            showAlert(Alert.AlertType.INFORMATION, "Acción aplicada", 
                    "La acción ha sido registrada correctamente.");
        }
    }
    
    @FXML
    private void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Productos Sin Movimiento");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("productos_sin_movimiento.xlsx");
        
        Stage stage = (Stage) noMovementTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            showAlert(Alert.AlertType.INFORMATION, "Exportación", 
                    "Los datos se exportarán a: " + file.getPath() + "\n\n" +
                    "Funcionalidad de exportación pendiente de implementar.");
        }
    }
    
    private void updateNotes(Product product) {
        // In a real implementation, you would load notes from database
        // For now, we'll just set placeholder text
        notesTextArea.setText("Notas para " + product.getName() + ":\n" +
                "- Último ingreso: " + (inventoryDAO.getLastEntryDate(product.getBarcode()) != null ? 
                        dateFormat.format(inventoryDAO.getLastEntryDate(product.getBarcode())) : "N/A") + "\n" +
                "- Última venta: " + (inventoryDAO.getLastSaleDate(product.getBarcode()) != null ? 
                        dateFormat.format(inventoryDAO.getLastSaleDate(product.getBarcode())) : "Nunca") + "\n" +
                "- Posibles razones de inactividad: Falta de demanda, precio elevado.");
    }
    
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
