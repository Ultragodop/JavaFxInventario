package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.enums.PaymentMethod;
import com.minimercado.javafxinventario.modules.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VentaController implements Initializable {

    // --- TextField components ---
    @FXML private TextField codigoBarraField;
    @FXML private TextField montoField;
    @FXML private TextField descuentoField;
    @FXML private TextField totalFinalField;
    @FXML private TextField montoRecibidoField;
    @FXML private TextField cambioField;
    @FXML private TextField referenciaTarjetaField;
    @FXML private TextField comprobanteField;
    @FXML private TextField confirmacionBilleteraField;
    @FXML private TextField efectivoMixtoField;
    @FXML private TextField tarjetaMixtoField;
    @FXML private TextField transferenciaMixtoField;
    @FXML private TextField billeteraMixtoField;
    @FXML private TextField restanteMixtoField;
    
    // --- TableView and columns ---
    @FXML private TableView<ProductoVenta> saleItemsTable;
    @FXML private TableColumn<ProductoVenta, String> codigoColumn;
    @FXML private TableColumn<ProductoVenta, String> nombreColumn;
    @FXML private TableColumn<ProductoVenta, Integer> cantidadColumn;
    @FXML private TableColumn<ProductoVenta, Double> precioUnitarioColumn;
    @FXML private TableColumn<ProductoVenta, Double> descuentoColumn;
    @FXML private TableColumn<ProductoVenta, Double> totalColumn;
    @FXML private TableColumn<ProductoVenta, Void> accionesColumn;
    
    // --- ComboBox components ---
    @FXML private ComboBox<String> tipoTarjetaCombo;
    @FXML private ComboBox<String> bancoCombo;
    @FXML private ComboBox<String> proveedorBilleteraCombo;
    
    // --- CheckBox components ---
    @FXML private CheckBox efectivoCheck;
    @FXML private CheckBox tarjetaCheck;
    @FXML private CheckBox transferenciaCheck;
    @FXML private CheckBox billeteraCheck;
    
    // --- RadioButton components ---
    @FXML private RadioButton facturaFisicaRadio;
    @FXML private RadioButton facturaElectronicaRadio;
    @FXML private RadioButton ambosRadio;
    @FXML private ToggleGroup impresionGroup;
    
    // --- Button components ---
    @FXML private Button procesarVentaButton;
    @FXML private Button cancelarVentaButton;
    @FXML private Button gestionarDevolucionButton;
    @FXML private Button historialVentasButton;
    @FXML private Button toggleClienteDisplayButton;
    @FXML private Button opcionesIdiomaButton;
    @FXML private Button offlineModeButton;
    @FXML private Button agregarProductoButton;
    
    // --- Container components ---
    @FXML private VBox efectivoContainer;
    @FXML private VBox tarjetaContainer;
    @FXML private VBox transferenciaContainer;
    @FXML private VBox billeteraContainer;
    @FXML private GridPane pagoMixtoGrid;
    @FXML private TabPane metodoPagoTabPane;
    
    // --- Label components ---
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label detalleVentaLabel;

    // Modules for business logic - Lazy initialization to avoid issues during FXML loading
    private InventoryDAO inventoryDAO;
    private TransactionModule transactionModule;
    private AccountingModule accountingModule;

    // Observable list to hold products in the current sale
    private ObservableList<ProductoVenta> productosList;
    
    // Sale totals
    private double totalVenta = 0.0;
    private double totalDescuentos = 0.0;
    private boolean modoOffline = false;

    // Default constructor required by FXML loader
    public VentaController() {
        // Empty constructor - initialization will be done in initialize() method
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize business logic modules
        inventoryDAO = new InventoryDAO();
        try {
            transactionModule = TransactionModule.getInstance();
            accountingModule = AccountingModule.getInstance();
        } catch (Exception e) {
            System.err.println("Error initializing modules: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize observable list
        productosList = FXCollections.observableArrayList();
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up data bindings for the table
        saleItemsTable.setItems(productosList);
        
        // Initialize comboboxes with data
        setupComboBoxes();
        
        // Set up radio buttons
        facturaFisicaRadio.setSelected(true);
        
        // Set initial totals
        updateTotals();
        
        // Set up action buttons
        setupActionButtons();
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        // Set up F2 key for product search
        KeyCombination searchKeyComb = new KeyCodeCombination(KeyCode.F2);
        
        // Add event filter to the scene (must be done after scene is shown)
        Platform.runLater(() -> {
            Scene scene = codigoBarraField.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (searchKeyComb.match(event)) {
                        handleBuscarProductoManual();
                        event.consume();
                    }
                });
                
                // Make barcode field automatically get focus
                codigoBarraField.requestFocus();
            }
        });
    }
    
    private void setupTableColumns() {
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        precioUnitarioColumn.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        descuentoColumn.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        // Make the table editable
        saleItemsTable.setEditable(true);
        
        // Configure the price column to be editable
        precioUnitarioColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        precioUnitarioColumn.setEditable(true);
        
        // Add event handler for price changes
        precioUnitarioColumn.setOnEditCommit(event -> {
            ProductoVenta producto = event.getRowValue();
            double oldPrice = producto.getPrecioUnitario();
            double newPrice = event.getNewValue();
            
            // Set the new price and update the table
            producto.setPrecioUnitario(newPrice);
            
            // Update totals
            updateTotals();
            
            // Show confirmation message
            statusLabel.setText("Precio modificado: " + producto.getNombre() + 
                            " - Precio anterior: $" + String.format("%.2f", oldPrice) + 
                            " - Nuevo precio: $" + String.format("%.2f", newPrice));
        });
        
        // Setup action column with buttons (+ and - buttons)
        setupActionColumn();
    }
    
    private void setupActionColumn() {
        Callback<TableColumn<ProductoVenta, Void>, TableCell<ProductoVenta, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ProductoVenta, Void> call(final TableColumn<ProductoVenta, Void> param) {
                final TableCell<ProductoVenta, Void> cell = new TableCell<>() {
                    private final Button btnPlus = new Button("+");
                    private final Button btnMinus = new Button("-");
                    private final Button btnDelete = new Button("X");
                    {
                        // Setup button actions
                        btnPlus.setOnAction((ActionEvent event) -> {
                            ProductoVenta producto = getTableView().getItems().get(getIndex());
                            // Check if there's enough stock before incrementing
                            try {
                                Product actualProduct = inventoryDAO.getProductByBarcode(producto.getCodigo());
                                if (actualProduct != null && producto.getCantidad() >= actualProduct.getStockQuantity()) {
                                    statusLabel.setText("No hay más stock disponible para: " + producto.getNombre() + 
                                                      ". Stock máximo: " + actualProduct.getStockQuantity());
                                    return;
                                }
                            } catch (Exception e) {
                                statusLabel.setText("Error al verificar stock: " + e.getMessage());
                                return;
                            }
                            producto.setCantidad(producto.getCantidad() + 1);
                            updateTotals();
                        });
                        
                        btnMinus.setOnAction((ActionEvent event) -> {
                            ProductoVenta producto = getTableView().getItems().get(getIndex());
                            if (producto.getCantidad() > 1) {
                                producto.setCantidad(producto.getCantidad() - 1);
                                updateTotals();
                            }
                        });
                        
                        btnDelete.setOnAction((ActionEvent event) -> {
                            ProductoVenta producto = getTableView().getItems().get(getIndex());
                            productosList.remove(producto);
                            updateTotals();
                        });
                        
                        // Style the buttons
                        btnDelete.setStyle("-fx-background-color: #ff6666;");
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create button container
                            HBox hBox = new HBox(btnMinus, btnPlus, btnDelete);
                            hBox.setSpacing(2);
                            setGraphic(hBox);
                        }
                    }
                };
                return cell;
            }
        };
        accionesColumn.setCellFactory(cellFactory);
    }
    
    private void setupComboBoxes() {
        // Clear any existing items first
        tipoTarjetaCombo.getItems().clear();
        bancoCombo.getItems().clear();
        proveedorBilleteraCombo.getItems().clear();
        
        // Create string converter to handle string values properly
        StringConverter<String> stringConverter = new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object;
            }
            
            @Override
            public String fromString(String string) {
                return string;
            }
        };
        
        // Set converters
        tipoTarjetaCombo.setConverter(stringConverter);
        bancoCombo.setConverter(stringConverter);
        proveedorBilleteraCombo.setConverter(stringConverter);
        
        // Add items after setting converters
        tipoTarjetaCombo.getItems().addAll("Visa", "MasterCard", "American Express", "Otra");
        bancoCombo.getItems().addAll("Banco 1", "Banco 2", "Banco 3", "Otro banco");
        proveedorBilleteraCombo.getItems().addAll("Mercado Pago", "PayPal", "Google Pay", "Apple Pay", "Otro");
    }
    
    private void setupActionButtons() {
        // Style the process sale button
        procesarVentaButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        
        // Style the cancel sale button
        cancelarVentaButton.setStyle("-fx-background-color: #ff6666;");
        
        // Disable certain features if not admin
        if (!isUserAdmin()) {
            gestionarDevolucionButton.setDisable(true);
        }
    }
    
    private boolean isUserAdmin() {
        // Mock function - in real implementation would check user permissions
        return true; // For testing, assume the user is an admin
    }
    
    // Handler for scanning/entering barcode
    @FXML
    private void handleCodigoBarraAction() {
        String codigo = codigoBarraField.getText().trim();
        if (!codigo.isEmpty()) {
            buscarYAgregarProducto(codigo); // Fixed method call - was incorrectly calling searchProducts
            codigoBarraField.clear();
            // Set focus back to the input field for continuous scanning
            codigoBarraField.requestFocus();
        }
    }
    
    private void buscarYAgregarProducto(String codigo) {
        try {
            // Get product directly from database
            Product product = inventoryDAO.getProductByBarcode(codigo);
            
            if (product == null) {
                statusLabel.setText("Producto no encontrado: " + codigo);
                return;
            }
            
            // Check if product has stock
            if (product.getStockQuantity() <= 0) {
                statusLabel.setText("No hay stock disponible para: " + product.getName());
                return;
            }
            
            // Check if product is already in the sale list
            boolean found = false;
            for (ProductoVenta p : productosList) {
                if (p.getCodigo().equals(codigo)) {
                    // Check if adding one more would exceed available stock
                    if (p.getCantidad() + 1 > product.getStockQuantity()) {
                        statusLabel.setText("No hay suficiente stock para: " + product.getName() + ". Stock disponible: " + product.getStockQuantity());
                        return;
                    }
                    
                    // Increment quantity if product already exists in cart
                    p.setCantidad(p.getCantidad() + 1);
                    found = true;
                    break;
                }
            }
            
            // If product not in list, create new entry
            if (!found) {
                ProductoVenta nuevoProducto = new ProductoVenta(
                    product.getBarcode(),
                    product.getName(),
                    1, // Default quantity
                    product.getSellingPrice(),
                    product.getDiscount()
                );
                productosList.add(nuevoProducto);
                statusLabel.setText("Producto agregado: " + product.getName());
            } else {
                statusLabel.setText("Cantidad incrementada: " + product.getName());
            }
            
            // Update totals after adding product
            updateTotals();
        } catch (Exception e) {
            statusLabel.setText("Error al agregar producto: " + e.getMessage());
            System.err.println("Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBuscarProductoManual() {
        // Open a product search dialog to search in the database
        try {
            Dialog<Product> dialog = new Dialog<>();
            dialog.setTitle("Búsqueda de productos");
            dialog.setHeaderText("Buscar producto");
            
            // Set the button types
            ButtonType searchButtonType = new ButtonType("Buscar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);
            
            // Create the search field and search result table
            TextField searchField = new TextField();
            searchField.setPromptText("Nombre o descripción del producto");
            
            TableView<Product> resultTable = new TableView<>();
            TableColumn<Product, String> barcodeCol = new TableColumn<>("Código");
            TableColumn<Product, String> nameCol = new TableColumn<>("Nombre");
            TableColumn<Product, Double> priceCol = new TableColumn<>("Precio");
            TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
            
            barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            priceCol.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
            stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
            resultTable.getColumns().addAll(barcodeCol, nameCol, priceCol, stockCol);
            
            // Add search functionality
            ObservableList<Product> searchResults = FXCollections.observableArrayList();
            resultTable.setItems(searchResults);
            
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    List<Product> results = inventoryDAO.searchProducts(newValue);
                    searchResults.setAll(results);
                } else {
                    searchResults.clear();
                }
            });
            
            // Layout the dialog
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            grid.add(new Label("Buscar:"), 0, 0);
            grid.add(searchField, 1, 0);
            grid.add(resultTable, 0, 1, 2, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Set the result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == searchButtonType) {
                    return resultTable.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<Product> result = dialog.showAndWait();
            result.ifPresent(product -> {
                ProductoVenta productoVenta = new ProductoVenta(
                    product.getBarcode(),
                    product.getName(),
                    1,
                    product.getSellingPrice(),
                    product.getDiscount()
                );
                buscarYAgregarProducto(product.getBarcode());
            });
            
        } catch (Exception e) {
            mostrarError("Error al buscar productos: " + e.getMessage());
        }
    }
    
    @FXML
    private void calcularCambio(KeyEvent event) {
        calcularCambioMonto();
    }
    
    /**
     * Calculate change amount based on current values - now in separate method
     * so it can be called from multiple places
     */
    private void calcularCambioMonto() {
        try {
            // Get and clean input text
            String montoRecibidoText = montoRecibidoField.getText().trim();
            
            // If empty, clear the change field and return
            if (montoRecibidoText.isEmpty()) {
                cambioField.setText("0.00");
                cambioField.setStyle("-fx-text-fill: black;");
                return;
            }
            
            // Replace commas with periods to support different locale formats
            montoRecibidoText = montoRecibidoText.replace(',', '.');
            
            // Parse the values
            double montoRecibido = Double.parseDouble(montoRecibidoText);
            
            // Get the total value, ensuring it's not empty
            String totalFinalText = totalFinalField.getText().trim();
            if (totalFinalText.isEmpty()) {
                cambioField.setText("0.00");
                cambioField.setStyle("-fx-text-fill: black;");
                return;
            }
            
            // Replace commas with periods in the total field too
            totalFinalText = totalFinalText.replace(',', '.');
            double totalFinal = Double.parseDouble(totalFinalText);
            
            // Calculate change
            double cambio = montoRecibido - totalFinal;
            
            // Display formatted result
            cambioField.setText(String.format("%.2f", cambio));
            
            // Set color based on whether there's enough money
            if (cambio >= 0) {
                cambioField.setStyle("-fx-text-fill: green;");
            } else {
                cambioField.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            // Handle parsing errors
            cambioField.setText("Error");
            cambioField.setStyle("-fx-text-fill: red;");
            // Log the error for debugging
            System.err.println("Error parsing amount: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected errors
            cambioField.setText("Error");
            cambioField.setStyle("-fx-text-fill: red;");
            // Log the error for debugging
            System.err.println("Unexpected error calculating change: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void toggleMixedPaymentOption(ActionEvent event) {
        // Enable/disable payment fields based on checkbox selections
        efectivoMixtoField.setDisable(!efectivoCheck.isSelected());
        tarjetaMixtoField.setDisable(!tarjetaCheck.isSelected());
        transferenciaMixtoField.setDisable(!transferenciaCheck.isSelected());
        billeteraMixtoField.setDisable(!billeteraCheck.isSelected());
        calcularRestantePagoMixto();
    }
    
    private void calcularRestantePagoMixto() {
        try {
            // Get the total from the field and clean it
            String totalFinalText = totalFinalField.getText().trim();
            if (totalFinalText.isEmpty()) {
                restanteMixtoField.setText("0.00");
                return;
            }
            totalFinalText = totalFinalText.replace(',', '.');
            double total = Double.parseDouble(totalFinalText);
            double sumaPagos = 0;
            
            // Parse each enabled payment field with proper format handling
            if (efectivoCheck.isSelected() && !efectivoMixtoField.getText().trim().isEmpty()) {
                String montoText = efectivoMixtoField.getText().trim().replace(',', '.');
                sumaPagos += Double.parseDouble(montoText);
            }
            if (tarjetaCheck.isSelected() && !tarjetaMixtoField.getText().trim().isEmpty()) {
                String montoText = tarjetaMixtoField.getText().trim().replace(',', '.');
                sumaPagos += Double.parseDouble(montoText);
            }
            if (transferenciaCheck.isSelected() && !transferenciaMixtoField.getText().trim().isEmpty()) {
                String montoText = transferenciaMixtoField.getText().trim().replace(',', '.');
                sumaPagos += Double.parseDouble(montoText);
            }
            if (billeteraCheck.isSelected() && !billeteraMixtoField.getText().trim().isEmpty()) {
                String montoText = billeteraMixtoField.getText().trim().replace(',', '.');
                sumaPagos += Double.parseDouble(montoText);
            }
            
            double restante = total - sumaPagos;
            restanteMixtoField.setText(String.format("%.2f", restante));
            
            // Change color based on whether the amount is fully paid
            if (restante <= 0) {
                restanteMixtoField.setStyle("-fx-text-fill: green;");
            } else {
                restanteMixtoField.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            restanteMixtoField.setText("Error en formato");
            System.err.println("Error en cálculo del pago mixto: " + e.getMessage());
        }
    }
    
    private void updateTotals() {
        totalVenta = 0;
        totalDescuentos = 0;
        
        for (ProductoVenta p : productosList) {
            totalVenta += p.getTotal();
            totalDescuentos += p.getDescuentoTotal();
        }
        
        double finalTotal = totalVenta;
        montoField.setText(String.format("%.2f", totalVenta + totalDescuentos));
        descuentoField.setText(String.format("%.2f", totalDescuentos));
        totalFinalField.setText(String.format("%.2f", finalTotal));
        
        calcularCambioMonto();
    }
    
    @FXML
    private void handleProcesarVenta() {
        if (productosList.isEmpty()) {
            mostrarError("No hay productos en la venta");
            return;
        }
        
        // Verify stock availability before processing
        if (!verificarStockDisponible()) {
            return;
        }
        
        // Get selected payment method
        Tab selectedTab = metodoPagoTabPane.getSelectionModel().getSelectedItem();
        String paymentMethodStr = selectedTab.getText();
        
        // Convert string to PaymentMethod enum
        PaymentMethod paymentMethod = PaymentMethod.findByDisplayName(paymentMethodStr);
        if (paymentMethod == null) {
            mostrarError("Método de pago no reconocido");
            return;
        }
        
        // Validate payment info based on method
        boolean paymentValid = validarPago(paymentMethod);
        if (!paymentValid) {
            return;
        }
        
        // Recalculate change amount to ensure it's up to date
        if (paymentMethod == PaymentMethod.EFECTIVO) {
            calcularCambioMonto();
        }
        
        // Save a copy of the products for inventory update
        List<ProductoVenta> productosVendidos = new ArrayList<>(productosList);
        
        // Process the sale
        procesarVentaEnSistema(paymentMethod);
        
        // Update inventory first - BEFORE clearing the sale
        actualizarInventario(productosVendidos);
        
        // Handle printing
        imprimirFactura();
        
        // Clear the sale
        limpiarVenta();
        statusLabel.setText("Venta procesada correctamente");
    }
    
    private boolean validarPago(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case EFECTIVO:
                try {
                    // Get and clean input text
                    String montoRecibidoText = montoRecibidoField.getText().trim();
                    
                    // Check if empty
                    if (montoRecibidoText.isEmpty()) {
                        mostrarError("Ingrese un monto recibido");
                        return false;
                    }
                    
                    // Replace commas with periods for locale-compatible parsing
                    montoRecibidoText = montoRecibidoText.replace(',', '.');
                    
                    // Get total final text and clean it
                    String totalFinalText = totalFinalField.getText().trim();
                    totalFinalText = totalFinalText.replace(',', '.');
                    
                    // Parse the values
                    double montoRecibido = Double.parseDouble(montoRecibidoText);
                    double totalFinal = Double.parseDouble(totalFinalText);
                    
                    if (montoRecibido < totalFinal) {
                        mostrarError("El monto recibido es menor al total");
                        return false;
                    }
                    System.out.println("Monto validado correctamente: " + montoRecibido + " >= " + totalFinal);
                } catch (NumberFormatException e) {
                    mostrarError("Ingrese un monto válido");
                    System.err.println("Error al analizar monto: " + e.getMessage());
                    return false;
                }
                break;
                
            case TARJETA:
                if (tipoTarjetaCombo.getValue() == null || referenciaTarjetaField.getText().isEmpty()) {
                    mostrarError("Complete la información de la tarjeta");
                    return false;
                }
                break;
                
            case TRANSFERENCIA:
                if (bancoCombo.getValue() == null || comprobanteField.getText().isEmpty()) {
                    mostrarError("Complete la información de la transferencia");
                    return false;
                }
                break;
                
            case BILLETERA_DIGITAL:
                if (proveedorBilleteraCombo.getValue() == null || confirmacionBilleteraField.getText().isEmpty()) {
                    mostrarError("Complete la información de la billetera digital");
                    return false;
                }
                break;
                
            case PAGO_MIXTO:
                // Check if at least one payment method is selected
                if (!efectivoCheck.isSelected() && !tarjetaCheck.isSelected() && !transferenciaCheck.isSelected() && !billeteraCheck.isSelected()) {
                    mostrarError("Seleccione al menos un método de pago");
                    return false;
                }
                
                // Check if the total amount is covered
                try {
                    double restante = Double.parseDouble(restanteMixtoField.getText());
                    if (restante > 0) {
                        mostrarError("El monto pagado no cubre el total");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    mostrarError("Error en los montos ingresados");
                    return false;
                }
                break;
        }
        return true;
    }
    
    private void procesarVentaEnSistema(PaymentMethod paymentMethod) {
        try {
            // Improved parsing of numbers by cleaning the input strings
            String totalStr = totalFinalField.getText().trim().replace(" ", "").replace(",", ".");
            String descuentoStr = descuentoField.getText().trim().replace(" ", "").replace(",", ".");
            
            // Debug output to help diagnose the issue
            System.out.println("Parsing total: '" + totalStr + "'");
            System.out.println("Parsing descuento: '" + descuentoStr + "'");
            
            double total = Double.parseDouble(totalStr);
            double descuento = Double.parseDouble(descuentoStr);
            
            // Create a list of items to pass to transaction module
            List<ProductoVenta> items = new ArrayList<>(productosList);
            
            // Create detailed sale information
            String detalleVenta = "Venta de " + items.size() + " productos distintos, " + 
                                  getTotalItemsCount() + " unidades en total";
            
            // Log transaction - using the TransactionModule
            boolean success;
            if (modoOffline) {
                success = transactionModule.logOfflineTransaction(items, paymentMethod, total, descuento);
            } else {
                success = transactionModule.logTransaction(items, paymentMethod, total, descuento);
            }
            
            if (!success) {
                mostrarError("Error al procesar la transacción");
                return;
            }
            
            // Verify that the transaction was correctly recorded in the databases
            try {
                // This is just a debug check - in a real application, we might not need this
                List<Transaction> recentTransactions = accountingModule.getTransactionsByPeriod("diario");
                boolean foundInAccounting = recentTransactions.stream()
                    .filter(tx -> Math.abs(tx.getAmount() - total) < 0.01)
                    .findAny()
                    .isPresent();
                    
                if (!foundInAccounting) {
                    System.err.println("Warning: Transaction may not have been properly recorded in accounting system");
                } else {
                    System.out.println("Transaction successfully verified in accounting system");
                }
            } catch (Exception e) {
                System.err.println("Error verifying transaction in accounting system: " + e.getMessage());
            }
            
            // Update the transaction details display
            statusLabel.setText("Venta registrada con éxito");
        } catch (NumberFormatException e) {
            mostrarError("Error en formato de números: " + e.getMessage() + 
                         ". Asegúrese de ingresar solo números válidos.");
            e.printStackTrace(); // This will help debugging by showing the error in console
        } catch (Exception e) {
            mostrarError("Error al procesar venta: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calculate the total number of items in the current sale
     * @return Total count of all items
     */
    private int getTotalItemsCount() {
        return productosList.stream().mapToInt(ProductoVenta::getCantidad).sum();
    }
    
    private boolean verificarStockDisponible() {
        for (ProductoVenta p : productosList) {
            try {
                Product product = inventoryDAO.getProductByBarcode(p.getCodigo());
                if (product == null || p.getCantidad() > product.getStockQuantity()) {
                    mostrarError("Stock insuficiente para: " + p.getNombre());
                    return false;
                }
            } catch (Exception e) {
                mostrarError("Error al verificar stock para: " + p.getNombre());
                return false;
            }
        }
        return true;
    }
    
    private void imprimirFactura() {
        // Get selected printing option
        if (facturaFisicaRadio.isSelected()) {
            System.out.println("Imprimiendo factura física");
        } else if (facturaElectronicaRadio.isSelected()) {
            System.out.println("Enviando factura electrónica");
        } else if (ambosRadio.isSelected()) {
            System.out.println("Imprimiendo factura física y enviando electrónica");
        }
    }
    
    private void limpiarVenta() {
        productosList.clear();
        montoField.clear();
        descuentoField.clear();
        totalFinalField.clear();
        montoRecibidoField.clear();
        cambioField.clear();
        referenciaTarjetaField.clear();
        comprobanteField.clear();
        confirmacionBilleteraField.clear();
        efectivoMixtoField.clear();
        tarjetaMixtoField.clear();
        transferenciaMixtoField.clear();
        billeteraMixtoField.clear();
        restanteMixtoField.clear();
        efectivoCheck.setSelected(false);
        tarjetaCheck.setSelected(false);
        transferenciaCheck.setSelected(false);
        billeteraCheck.setSelected(false);
    }
    
    private void actualizarInventario(List<ProductoVenta> productosVendidos) {
        try {
            int productosActualizados = 0;
            int totalProductos = productosVendidos.size();
            List<String> productosNoActualizados = new ArrayList<>();
            
            // Update the inventory directly using our inventoryDAO
            for (ProductoVenta p : productosVendidos) {
                // Update stock in database - subtract the sold quantity
                boolean updated = inventoryDAO.updateProductStock(p.getCodigo(), -p.getCantidad());
                if (updated) {
                    productosActualizados++;
                } else {
                    productosNoActualizados.add(p.getNombre());
                    System.err.println("Error al actualizar el stock del producto: " + p.getCodigo());
                }
            }
            
            if (productosActualizados < totalProductos) {
                mostrarError(String.format("Se actualizaron %d de %d productos en inventario. " +
                    "Productos no actualizados: %s", productosActualizados, totalProductos, 
                    String.join(", ", productosNoActualizados)));
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar inventario: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al actualizar inventario: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelarVenta() {
        if (productosList.isEmpty()) {
            statusLabel.setText("No hay venta para cancelar");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancelar Venta");
        confirmation.setHeaderText("¿Está seguro de cancelar la venta actual?");
        confirmation.setContentText("Todos los productos serán removidos.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                limpiarVenta();
                statusLabel.setText("Venta cancelada");
            }
        });
    }
    
    @FXML
    private void handleGestionarDevolucion() {
        if (!isUserAdmin()) {
            mostrarError("No tiene permisos para gestionar devoluciones");
            return;
        }
        
        // This would open a return management dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gestión de Devoluciones");
        alert.setHeaderText("Función de gestión de devoluciones");
        alert.setContentText("Aquí se mostraría un diálogo para gestionar devoluciones de productos.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleHistorialVentas() {
        // This would open a sales history dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historial de Ventas");
        alert.setHeaderText("Función de historial de ventas");
        alert.setContentText("Aquí se mostraría un diálogo con el historial de ventas recientes.");
        alert.showAndWait();
    }
    
    @FXML
    private void handleToggleClienteDisplay() {
        // This would toggle the customer display
        boolean isDisplayVisible = toggleClienteDisplayButton.getText().equals("Ocultar Pantalla Cliente");
        
        if (isDisplayVisible) {
            toggleClienteDisplayButton.setText("Mostrar Pantalla Cliente");
            // Code to hide customer display
            System.out.println("Pantalla de cliente ocultada");
        } else {
            toggleClienteDisplayButton.setText("Ocultar Pantalla Cliente");
            // Code to show customer display
            System.out.println("Pantalla de cliente mostrada");
        }
    }
    
    @FXML
    private void handleOpcionesIdioma() {
        // This would open a language options dialog
        String[] idiomas = {"Español", "English", "Português", "Français"};
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Español", idiomas);
        dialog.setTitle("Opciones de Idioma");
        dialog.setHeaderText("Seleccione un idioma");
        dialog.setContentText("Idioma:");
        
        dialog.showAndWait().ifPresent(idioma -> {
            statusLabel.setText("Idioma seleccionado: " + idioma);
            // Code to change the application language
        });
    }
    
    @FXML
    private void handleOfflineMode() {
        modoOffline = !modoOffline;
        
        if (modoOffline) {
            connectionStatusLabel.setText("Fuera de línea");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            offlineModeButton.setText("Modo Online");
            statusLabel.setText("Modo offline activado. Los datos se sincronizarán cuando vuelvas a estar en línea.");
        } else {
            connectionStatusLabel.setText("En línea");
            connectionStatusLabel.setStyle("-fx-text-fill: green;");
            offlineModeButton.setText("Modo Offline");
            statusLabel.setText("Modo online activado. Conexión establecida con el servidor.");
            
            // Sync pending transactions
            sincronizarDatosPendientes();
        }
    }
    
    private void sincronizarDatosPendientes() {
        try {
            boolean syncSuccess = transactionModule.syncOfflineTransactions();
            if (syncSuccess) {
                statusLabel.setText("Todas las transacciones pendientes han sido sincronizadas");
            } else {
                statusLabel.setText("Algunas transacciones no pudieron ser sincronizadas");
            }
        } catch (Exception e) {
            statusLabel.setText("Error al sincronizar datos: " + e.getMessage());
        }
    }
    
    private void mostrarError(String mensaje) {
        statusLabel.setText("Error: " + mensaje);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    @FXML
    private void handleAgregarProducto() {
        handleCodigoBarraAction();
    }
    
    // For testing purposes - add sample products
    private void addSampleProductsForTesting() {
        // Add sample products from our database
        String[] testBarcodes = {"123456789", "234567890", "345678901"};
        for (String barcode : testBarcodes) {
            buscarYAgregarProducto(barcode);
        }
    }
    
    // Inner class to represent a product in the sale
    public static class ProductoVenta {
        private final SimpleStringProperty codigo;
        private final SimpleStringProperty nombre;
        private final SimpleIntegerProperty cantidad;
        private final SimpleDoubleProperty precioUnitario;
        private final SimpleDoubleProperty descuento;
        private final SimpleDoubleProperty total;

        public ProductoVenta(String codigo, String nombre, int cantidad, double precioUnitario, double descuento) {
            this.codigo = new SimpleStringProperty(codigo);
            this.nombre = new SimpleStringProperty(nombre);
            this.cantidad = new SimpleIntegerProperty(cantidad);
            this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
            this.descuento = new SimpleDoubleProperty(descuento);
            this.total = new SimpleDoubleProperty(calcularTotal());
        }

        public String getCodigo() {
            return codigo.get();
        }

        public String getNombre() {
            return nombre.get();
        }

        public int getCantidad() {
            return cantidad.get();
        }

        public void setCantidad(int cantidad) {
            this.cantidad.set(cantidad);
            // Update total when quantity changes
            total.set(calcularTotal());
        }

        public double getPrecioUnitario() {
            return precioUnitario.get();
        }

        public void setPrecioUnitario(double precioUnitario) {
            this.precioUnitario.set(precioUnitario);
            // Update total when price changes
            total.set(calcularTotal());
        }

        public double getDescuento() {
            return descuento.get();
        }

        public double getTotal() {
            return total.get();
        }

        public double getDescuentoTotal() {
            return descuento.get() * cantidad.get();
        }

        private double calcularTotal() {
            return (precioUnitario.get() - descuento.get()) * cantidad.get();
        }

        // Property getters for TableView binding
        public SimpleStringProperty codigoProperty() {
            return codigo;
        }

        public SimpleStringProperty nombreProperty() {
            return nombre;
        }

        public SimpleIntegerProperty cantidadProperty() {
            return cantidad;
        }

        public SimpleDoubleProperty precioUnitarioProperty() {
            return precioUnitario;
        }

        public SimpleDoubleProperty descuentoProperty() {
            return descuento;
        }

        public SimpleDoubleProperty totalProperty() {
            return total;
        }
    }
}