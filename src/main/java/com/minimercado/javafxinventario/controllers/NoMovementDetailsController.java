package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import javafx.event.ActionEvent;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

public class NoMovementDetailsController {

    public ComboBox actionFilterComboBox;
    public Label totalProductsLabel;
    public TableView<Product> noMovementTable;
    public TableColumn barcodeColumn;
    public TableColumn nameColumn;
    public TableColumn categoryColumn;
    public TableColumn stockColumn;
    public TableColumn lastEntryDateColumn;
    public TableColumn lastSaleDateColumn;
    public TableColumn daysSinceLastSaleColumn;
    public TableColumn daysInInventoryColumn;
    public TableColumn unitCostColumn;
    public TableColumn totalValueColumn;
    public TableColumn salePriceColumn;
    public TableColumn expirationDateColumn;
    public TableColumn statusColumn;
    public TextArea notesTextArea;
    public ComboBox<String> actionComboBox;
    public Button applyActionButton;
    @FXML private TableView<Product> productsTable;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> periodCombo;
    @FXML private Label totalValueLabel;
    @FXML private Label productCountLabel;

    private ObservableList<Product> noMovementProducts;
    private Map<String, String> productNotes = new HashMap<>();

    /**
     * Initialize the controller with product data
     */
    public void initData(List<Product> products) {
        this.noMovementProducts = FXCollections.observableArrayList(products);
        
        // Initialize period filter - add null check
        if (periodCombo != null) {
            periodCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Últimos 30 días", "Últimos 60 días", "Últimos 90 días"
            ));
            periodCombo.setValue("Todos");
        } else {
            System.err.println("WARNING: periodCombo is null. Check if fx:id is properly set in FXML.");
        }
        
        // Initialize action filter combobox
        if (actionFilterComboBox != null) {
            actionFilterComboBox.setItems(FXCollections.observableArrayList(
                "Todas las acciones", "Pendiente de revisión", "Liquidación", "Promoción", "Devolución al proveedor"
            ));
            actionFilterComboBox.setValue("Todas las acciones");
        }
        
        // Initialize action combobox for selected product
        if (actionComboBox != null) {
            actionComboBox.setItems(FXCollections.observableArrayList(
                "Seleccionar acción", "Liquidación", "Promoción", "Devolución al proveedor", "Reasignar", "Dar de baja"
            ));
            actionComboBox.setValue("Seleccionar acción");
        }
        
        // Initialize table
        initializeTable();
        
        // Calculate and display totals
        updateTotals(noMovementProducts);
        
        // Set initial status
        if (statusLabel != null) {
            statusLabel.setText("Se encontraron " + products.size() + " productos sin movimiento");
        } else {
            System.err.println("WARNING: statusLabel is null. Check if fx:id is properly set in FXML.");
        }
        
        // Setup table to display selected product details in notes area
        setupTableSelectionHandler();
    }
    
    /**
     * Initialize table columns
     */
    private void initializeTable() {
        // Check if we should use productsTable or noMovementTable
        TableView<Product> tableToUse = productsTable != null ? productsTable : noMovementTable;
        
        // Add null check before initializing the table
        if (tableToUse == null) {
            System.err.println("WARNING: No table available. Check if fx:id is properly set in FXML.");
            return;
        }
        
        // Clear existing columns to avoid duplicates
        tableToUse.getColumns().clear();
        
        // Create and add columns
        TableColumn<Product, String> barcodeCol = new TableColumn<>("Código");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Producto");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, String> categoryCol = new TableColumn<>("Categoría");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        TableColumn<Product, Double> costCol = new TableColumn<>("Costo Unitario");
        costCol.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        costCol.setCellFactory(col -> new TableCell<Product, Double>() {
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
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Precio Venta");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
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
        
        TableColumn<Product, Double> valueCol = new TableColumn<>("Valor Total");
        valueCol.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getStockQuantity() * cellData.getValue().getSellingPrice();
            return new javafx.beans.property.SimpleDoubleProperty(value).asObject();
        });
        valueCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", value));
                }
            }
        });
        
        TableColumn<Product, Date> lastMovementCol = new TableColumn<>("Último Movimiento");
        // Replace PropertyValueFactory with custom cell value factory
        lastMovementCol.setCellValueFactory(cellData -> {
            // This returns null for now - in a real application, you would get this from a service or DAO
            // For example: return new SimpleObjectProperty<>(inventoryService.getLastMovementDate(cellData.getValue()));
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        
        lastMovementCol.setCellFactory(col -> {
            return new TableCell<Product, Date>() {
                private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText("Sin movimientos");
                    } else {
                        setText(dateFormat.format(date));
                    }
                }
            };
        });
        
        TableColumn<Product, Date> expirationCol = new TableColumn<>("Fecha Vencimiento");
        expirationCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        expirationCol.setCellFactory(col -> {
            return new TableCell<Product, Date>() {
                private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                
                @Override
                protected void updateItem(Date date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText("N/A");
                    } else {
                        setText(dateFormat.format(date));
                    }
                }
            };
        });
        
        TableColumn<Product, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String status = "Normal";
            
            // Determine status based on product data
            if (product.getExpirationDate() != null) {
                Date today = new Date();
                if (product.getExpirationDate().before(today)) {
                    status = "Vencido";
                } else {
                    // Check if expiration is within 30 days
                    LocalDate expirationLocal = product.getExpirationDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate todayLocal = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    
                    long daysUntilExpiration = ChronoUnit.DAYS.between(todayLocal, expirationLocal);
                    if (daysUntilExpiration <= 30) {
                        status = "Próximo a vencer";
                    }
                }
            }
            
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        // Add columns to table
        tableToUse.getColumns().addAll(
            barcodeCol, nameCol, categoryCol, stockCol, costCol, priceCol, valueCol, lastMovementCol, expirationCol, statusCol
        );
        
        // Set items to table
        tableToUse.setItems(noMovementProducts);
    }
    
    /**
     * Setup table selection handler to update the notes area
     */
    private void setupTableSelectionHandler() {
        TableView<Product> tableToUse = productsTable != null ? productsTable : noMovementTable;
        
        if (tableToUse != null && notesTextArea != null) {
            tableToUse.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Display product details in the notes area
                    String note = productNotes.getOrDefault(newSelection.getBarcode(), "");
                    
                    String productDetails = String.format(
                        "Producto: %s\nCódigo: %s\nCategoría: %s\nStock: %d\nPrecio: $%.2f\n\nNotas adicionales:\n%s",
                        newSelection.getName(),
                        newSelection.getBarcode(),
                        newSelection.getCategory(),
                        newSelection.getStockQuantity(),
                        newSelection.getSellingPrice(),
                        note
                    );
                    
                    notesTextArea.setText(productDetails);
                } else {
                    notesTextArea.clear();
                }
            });
        }
    }
    
    /**
     * Apply filters based on search text and period selection
     */
    @FXML
    private void applyFilters() {
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String period = periodCombo != null ? periodCombo.getValue() : "Todos";
        String actionFilter = actionFilterComboBox != null ? (String)actionFilterComboBox.getValue() : "Todas las acciones";
        
        ObservableList<Product> filteredProducts = noMovementProducts.filtered(product -> {
            boolean searchMatch = searchText.isEmpty() || 
                                 (product.getName() != null && product.getName().toLowerCase().contains(searchText)) ||
                                 (product.getBarcode() != null && product.getBarcode().toLowerCase().contains(searchText));
            
            // Period filtering would depend on having lastMovementDate in the Product class
            boolean periodMatch = "Todos".equals(period);
            
            // Action filtering - this would normally check a product's assigned action
            boolean actionMatch = "Todas las acciones".equals(actionFilter);
            
            return searchMatch && periodMatch && actionMatch;
        });
        
        // Set filtered list to the appropriate table
        TableView<Product> tableToUse = productsTable != null ? productsTable : noMovementTable;
        if (tableToUse != null) {
            tableToUse.setItems(filteredProducts);
        }
        
        // Update totals
        updateTotals(filteredProducts);
        
        // Update status
        if (statusLabel != null) {
            statusLabel.setText("Se encontraron " + filteredProducts.size() + " productos sin movimiento");
        }
    }
    
    /**
     * Update total value and count labels
     */
    private void updateTotals(List<Product> products) {
        double totalValue = products.stream()
            .mapToDouble(p -> p.getStockQuantity() * p.getSellingPrice())
            .sum();
            
        // Add null checks before updating labels
        if (totalValueLabel != null) {
            totalValueLabel.setText(String.format("$%.2f", totalValue));
        }
        
        if (productCountLabel != null) {
            productCountLabel.setText(String.valueOf(products.size()));
        }
        
        // Also update totalProductsLabel if it exists
        if (totalProductsLabel != null) {
            totalProductsLabel.setText("Total: " + products.size());
        }
    }
    
    /**
     * Handler for apply action button
     */
    @FXML
    private void handleApplyAction(ActionEvent event) {
        TableView<Product> tableToUse = productsTable != null ? productsTable : noMovementTable;
        
        if (tableToUse == null) {
            showMessage("Error", "No se pudo encontrar la tabla de productos");
            return;
        }
        
        Product selectedProduct = tableToUse.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showMessage("Selección requerida", "Por favor, seleccione un producto de la lista");
            return;
        }
        
        String selectedAction = actionComboBox != null ? actionComboBox.getValue() : null;
        if (selectedAction == null || "Seleccionar acción".equals(selectedAction)) {
            showMessage("Acción requerida", "Por favor, seleccione una acción a realizar");
            return;
        }
        
        // Save notes if they were modified
        if (notesTextArea != null) {
            productNotes.put(selectedProduct.getBarcode(), notesTextArea.getText());
        }
        
        // Handle different actions
        switch (selectedAction) {
            case "Liquidación":
                handleLiquidationAction(selectedProduct);
                break;
            case "Promoción":
                handlePromotionAction(selectedProduct);
                break;
            case "Devolución al proveedor":
                handleReturnToSupplierAction(selectedProduct);
                break;
            case "Reasignar":
                handleReassignAction(selectedProduct);
                break;
            case "Dar de baja":
                handleDiscontinueAction(selectedProduct);
                break;
            default:
                showMessage("Acción no implementada", 
                          "La acción '" + selectedAction + "' no está implementada actualmente");
        }
    }
    
    private void handleLiquidationAction(Product product) {
        try {
            // Calcular precio de liquidación (por ejemplo, un 30% menos)
            double originalPrice = product.getSellingPrice();
            double discountRate = 0.30; // 30% de descuento
            double liquidationPrice = originalPrice * (1 - discountRate);
            
            // Mostrar diálogo de confirmación con los detalles
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmar Liquidación");
            confirmDialog.setHeaderText("Aplicar liquidación al producto: " + product.getName());
            confirmDialog.setContentText(
                "Precio original: $" + String.format("%.2f", originalPrice) + "\n" +
                "Descuento: " + (discountRate * 100) + "%\n" +
                "Precio de liquidación: $" + String.format("%.2f", liquidationPrice) + "\n\n" +
                "¿Confirma aplicar este precio de liquidación?"
            );
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Aquí iría la lógica para actualizar el precio en la base de datos
                product.setSellingPrice(liquidationPrice);
                product.setDiscount(originalPrice - liquidationPrice);
                
                // Agregar etiqueta o estado de liquidación al producto
                // product.setStatus("LIQUIDACION");
                
                showMessage("Liquidación aplicada", 
                          "Se ha aplicado liquidación al producto: " + product.getName() + 
                          "\nNuevo precio: $" + String.format("%.2f", liquidationPrice));
            }
        } catch (Exception e) {
            showMessage("Error", "No se pudo aplicar la liquidación: " + e.getMessage());
        }
    }

    private void handlePromotionAction(Product product) {
        try {
            // Crear un formulario personalizado para la promoción
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Configurar Promoción");
            dialog.setHeaderText("Configurar promoción para: " + product.getName());
            
            // Botones
            ButtonType applyButtonType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);
            
            // Crear los campos del formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Tipo de promoción
            ComboBox<String> promoTypeCombo = new ComboBox<>();
            promoTypeCombo.getItems().addAll("Descuento porcentual", "Precio especial", "2x1", "Paquete");
            promoTypeCombo.setValue("Descuento porcentual");
            
            // Valor del descuento
            TextField discountField = new TextField("10"); // 10% por defecto
            discountField.setPromptText("Porcentaje o valor");
            
            // Fecha de fin
            DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(7)); // 1 semana por defecto
            
            grid.add(new Label("Tipo de promoción:"), 0, 0);
            grid.add(promoTypeCombo, 1, 0);
            grid.add(new Label("Valor:"), 0, 1);
            grid.add(discountField, 1, 1);
            grid.add(new Label("Fecha fin:"), 0, 2);
            grid.add(endDatePicker, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convertir el resultado cuando se hace clic en aplicar
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == applyButtonType) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("type", promoTypeCombo.getValue());
                    result.put("value", discountField.getText());
                    result.put("endDate", endDatePicker.getValue());
                    return result;
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(values -> {
                String type = (String) values.get("type");
                String valueStr = (String) values.get("value");
                LocalDate endDate = (LocalDate) values.get("endDate");
                
                // Aquí iría la lógica para aplicar la promoción al producto
                double originalPrice = product.getSellingPrice();
                double newPrice = originalPrice;
                
                if ("Descuento porcentual".equals(type)) {
                    double discountPercent = Double.parseDouble(valueStr);
                    newPrice = originalPrice * (1 - (discountPercent / 100));
                    product.setDiscount(originalPrice - newPrice);
                }
                
                showMessage("Promoción aplicada", 
                          "Se ha aplicado promoción al producto: " + product.getName() + 
                          "\nTipo: " + type +
                          "\nVálida hasta: " + endDate.toString());
            });
        } catch (Exception e) {
            showMessage("Error", "No se pudo aplicar la promoción: " + e.getMessage());
        }
    }

    private void handleReturnToSupplierAction(Product product) {
        try {
            // Crear un formulario para la devolución
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Devolución al Proveedor");
            dialog.setHeaderText("Devolver producto: " + product.getName() + " al proveedor");
            
            // Botones
            ButtonType returnButton = new ButtonType("Devolver", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(returnButton, ButtonType.CANCEL);
            
            // Crear los campos del formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Cantidad a devolver (máximo el stock actual)
            Spinner<Integer> quantitySpinner = new Spinner<>(1, product.getStockQuantity(), 1);
            quantitySpinner.setEditable(true);
            
            // Motivo de devolución
            ComboBox<String> reasonCombo = new ComboBox<>();
            reasonCombo.getItems().addAll(
                "Producto sin movimiento", 
                "Producto defectuoso",
                "Producto próximo a vencer",
                "Acuerdo con proveedor",
                "Otro"
            );
            reasonCombo.setValue("Producto sin movimiento");
            
            // Notas adicionales
            TextArea notesArea = new TextArea();
            notesArea.setPromptText("Ingrese detalles adicionales si es necesario");
            
            // Número de autorización
            TextField authNumberField = new TextField();
            authNumberField.setPromptText("Número de autorización del proveedor (opcional)");
            
            grid.add(new Label("Cantidad a devolver:"), 0, 0);
            grid.add(quantitySpinner, 1, 0);
            grid.add(new Label("Motivo:"), 0, 1);
            grid.add(reasonCombo, 1, 1);
            grid.add(new Label("Autorización:"), 0, 2);
            grid.add(authNumberField, 1, 2);
            grid.add(new Label("Notas:"), 0, 3);
            grid.add(notesArea, 1, 3);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convertir el resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == returnButton) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("quantity", quantitySpinner.getValue());
                    result.put("reason", reasonCombo.getValue());
                    result.put("authNumber", authNumberField.getText());
                    result.put("notes", notesArea.getText());
                    return result;
                }
                return null;
            });
            
            // Mostrar el diálogo y procesar el resultado
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(values -> {
                int quantity = (int) values.get("quantity");
                String reason = (String) values.get("reason");
                String authNumber = (String) values.get("authNumber");
                String notes = (String) values.get("notes");
                
                // Aquí iría la lógica para registrar la devolución
                // 1. Actualizar inventario
                // 2. Registrar documento de devolución
                // 3. Actualizar estado del producto
                
                showMessage("Devolución registrada", 
                          "Se ha registrado la devolución de " + quantity + " unidades del producto: " + 
                          product.getName() + " al proveedor.\n" +
                          "Por motivo: " + reason);
            });
        } catch (Exception e) {
            showMessage("Error", "No se pudo procesar la devolución: " + e.getMessage());
        }
    }

    private void handleReassignAction(Product product) {
        showMessage("Reasignar", 
                  "Se reasignará el producto: " + product.getName() + 
                  "\nEsta funcionalidad se implementará en una versión futura.");
    }
    
    private void handleDiscontinueAction(Product product) {
        showMessage("Dar de baja", 
                  "Se dará de baja al producto: " + product.getName() + 
                  "\nEsta funcionalidad se implementará en una versión futura.");
    }
    
    /**
     * Apply filter button handler
     */
    @FXML
    private void handleApplyFilter(ActionEvent event) {
        applyFilters();
    }
    
    /**
     * Search button handler
     */
    @FXML
    private void handleSearch() {
        applyFilters();
    }
    
    /**
     * Reset filters button handler
     */
    @FXML
    private void handleResetFilters() {
        if (searchField != null) searchField.clear();
        if (periodCombo != null) periodCombo.setValue("Todos");
        if (actionFilterComboBox != null) actionFilterComboBox.setValue("Todas las acciones");
        applyFilters();
    }

    /**
     * Export data handler
     */
    @FXML
    private void handleExportData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Datos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("productos_sin_movimiento.xlsx");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            // Add actual export logic here
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación");
            alert.setHeaderText(null);
            alert.setContentText("Datos exportados exitosamente a: " + file.getAbsolutePath());
            alert.showAndWait();
        }
    }

    /**
     * Print report handler
     */
    @FXML
    private void handlePrint(ActionEvent event) {
        showMessage("Imprimir reporte", 
                  "La funcionalidad de impresión será implementada en una versión futura.");
    }
    
    /**
     * Save notes for selected product
     */
    @FXML
    private void handleSaveNotes(ActionEvent event) {
        TableView<Product> tableToUse = productsTable != null ? productsTable : noMovementTable;
        
        if (tableToUse != null && notesTextArea != null) {
            Product selectedProduct = tableToUse.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                productNotes.put(selectedProduct.getBarcode(), notesTextArea.getText());
                showMessage("Notas guardadas", "Las notas para el producto han sido guardadas.");
            } else {
                showMessage("Selección requerida", "Por favor, seleccione un producto para guardar notas.");
            }
        }
    }
    
    // Helper method for showing messages
    private void showMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
