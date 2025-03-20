package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.PurchaseOrder;
import com.minimercado.javafxinventario.modules.Supplier;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.Transaction;
import javafx.application.Platform;  // Added missing import for Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderController {

    // Main form fields
    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private DatePicker orderDatePicker;
    @FXML private DatePicker expectedDatePicker;
    @FXML private TextField totalAmountField;
    @FXML private TextArea notesArea;
    @FXML private Label orderNumberLabel;
    @FXML private Label statusLabel;
    
    // Order items table
    @FXML private TableView<PurchaseOrder.Item> itemsTable;
    @FXML private TableColumn<PurchaseOrder.Item, String> productIdColumn;
    @FXML private TableColumn<PurchaseOrder.Item, String> productNameColumn; // Added for product name display
    @FXML private TableColumn<PurchaseOrder.Item, Integer> quantityColumn;
    @FXML private TableColumn<PurchaseOrder.Item, Double> priceColumn;
    @FXML private TableColumn<PurchaseOrder.Item, Double> subtotalColumn;
    
    // Order list table
    @FXML private TableView<PurchaseOrder> ordersTable;
    @FXML private TableColumn<PurchaseOrder, Integer> orderIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> supplierColumn;
    @FXML private TableColumn<PurchaseOrder, Date> dateColumn;
    @FXML private TableColumn<PurchaseOrder, String> statusColumn;
    @FXML private TableColumn<PurchaseOrder, Double> amountColumn;
    
    // Status and filter
    @FXML private ComboBox<String> statusFilterComboBox;
    
    // Data
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private PurchaseOrder currentOrder;
    private final ObservableList<PurchaseOrder.Item> orderItems = FXCollections.observableArrayList();
    private final ObservableList<PurchaseOrder> ordersList = FXCollections.observableArrayList();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    
    // Add a new field to track if we're showing mock data
    private boolean showingMockData = false;
    private Button mockDataButton;

    // Add reference to AccountingModule
    private final AccountingModule accountingModule = AccountingModule.getInstance();

    @FXML
    public void initialize() {
        // Initialize current order
        currentOrder = new PurchaseOrder();
        
        // Setup tables
        setupItemsTable();
        setupOrdersTable();
        
        // Load suppliers into combo box
        loadSuppliers();
        
        // Load products
        loadProducts();
        
        // Setup date pickers
        orderDatePicker.setValue(LocalDate.now());
        expectedDatePicker.setValue(LocalDate.now().plusDays(7));
        
        // Setup status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "Todos", "PENDING", "ORDERED", "RECEIVED", "CANCELED"));
        statusFilterComboBox.setValue("Todos");
        statusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadOrders();
        });
        
        // Add mock data button
        mockDataButton = new Button("Usar datos de ejemplo");
        mockDataButton.setOnAction(e -> toggleMockData());
        mockDataButton.setStyle("-fx-background-color: #e0e0e0;");
        // Add to your layout - you'll need to add this to your FXML or add it programmatically
        
        // Load orders
        loadOrders();
        
        // Update UI with empty order
        updateOrderUI();
    }
    
    /**
     * Sets up the order items table
     */
    private void setupItemsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        
        // Add property for product name - this is a custom field we'll need to handle
        productNameColumn.setCellValueFactory(cellData -> {
            String productId = cellData.getValue().getProductId();
            Product product = inventoryDAO.getProductByBarcode(productId);
            return new javafx.beans.property.SimpleStringProperty(
                product != null ? product.getName() : "Unknown Product");
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        // Add action column for removing items
        TableColumn<PurchaseOrder.Item, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            
            {
                removeButton.setOnAction(event -> {
                    PurchaseOrder.Item item = getTableView().getItems().get(getIndex());
                    orderItems.remove(item);
                    currentOrder.removeItem(item.getProductId());
                    updateOrderUI();
                });
                
                removeButton.setStyle("-fx-background-color: #ff6666;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        itemsTable.getColumns().add(actionColumn);
        itemsTable.setItems(orderItems);
    }
    
    /**
     * Sets up the orders table
     */
    private void setupOrdersTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        // Format date
        dateColumn.setCellFactory(column -> {
            TableCell<PurchaseOrder, Date> cell = new TableCell<>() {
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
            return cell;
        });
        
        // Format amount as currency
        amountColumn.setCellFactory(column -> {
            TableCell<PurchaseOrder, Double> cell = new TableCell<>() {
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
            return cell;
        });
        
        // Format status column with colors
        statusColumn.setCellFactory(column -> new TableCell<PurchaseOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "RECEIVED":
                            setStyle("-fx-text-fill: #28a745;"); // Green
                            break;
                        case "ORDERED":
                            setStyle("-fx-text-fill: #007bff;"); // Blue
                            break;
                        case "PAID":
                            setStyle("-fx-text-fill: #6f42c1;"); // Purple
                            break;
                        case "PARTIALLY_PAID":
                            setStyle("-fx-text-fill: #fd7e14;"); // Orange
                            break;
                        case "CANCELED":
                            setStyle("-fx-text-fill: #dc3545;"); // Red
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Add action buttons to orders table
        TableColumn<PurchaseOrder, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonBox = new HBox(5);
            private final Button viewBtn = new Button("Ver");
            private final Button receiveBtn = new Button("Recibir");
            private final Button payBtn = new Button("Pagar");
            
            {
                viewBtn.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    viewOrder(order);
                });
                
                receiveBtn.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    receiveOrder(order);
                });
                
                payBtn.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    showPaymentDialog(order);
                });
                
                buttonBox.getChildren().addAll(viewBtn, receiveBtn, payBtn);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    
                    // Habilitar/deshabilitar botones según el estado de la orden
                    receiveBtn.setDisable(!"ORDERED".equals(order.getStatus()));
                    
                    // Solo mostrar el botón de pago para órdenes recibidas o con estado UNPAID/PARTIALLY_PAID
                    boolean canPay = "RECEIVED".equals(order.getStatus()) || 
                                    "PARTIALLY_PAID".equals(order.getStatus()) ||
                                    "UNPAID".equals(order.getStatus());
                    
                    payBtn.setDisable(!canPay);
                    payBtn.setVisible(canPay);
                    
                    setGraphic(buttonBox);
                }
            }
        });
        
        ordersTable.getColumns().add(actionColumn);
        ordersTable.setItems(ordersList);
    }
    
    /**
     * Deletes a purchase order after confirmation
     */
    private void deleteOrder(PurchaseOrder order) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Eliminación");
        confirmDialog.setHeaderText("¿Está seguro que desea eliminar esta orden de compra?");
        confirmDialog.setContentText(String.format(
            "Orden #%d para %s con un total de $%.2f\nStatus: %s", 
            order.getId(), order.getSupplierName(), order.getTotalAmount(), order.getStatus())
        );
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed deletion, proceed with delete
            boolean success = inventoryDAO.deletePurchaseOrder(order.getId());
            
            if (success) {
                // Remove from the observable list
                ordersList.remove(order);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Orden Eliminada", 
                          "Orden de compra eliminada", 
                          "La orden de compra ha sido eliminada exitosamente.");
                
                // Refresh the table
                ordersTable.refresh();
            } else {
                // Show error message
                showAlert(Alert.AlertType.ERROR, "Error", 
                          "Error al eliminar la orden", 
                          "No se pudo eliminar la orden de compra. Por favor intente nuevamente.");
            }
        }
    }
    
    /**
     * Loads suppliers into the combo box
     */
    private void loadSuppliers() {
        List<Supplier> suppliers = inventoryDAO.getAllSuppliers();
        
        // Create a string converter for the supplier combo box
        supplierComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getName() : "";
            }
            
            @Override
            public Supplier fromString(String string) {
                return supplierComboBox.getItems().stream()
                    .filter(s -> s.getName().equals(string))
                    .findFirst().orElse(null);
            }
        });
        
        supplierComboBox.setItems(FXCollections.observableArrayList(suppliers));
    }
    
    /**
     * Load products for the order
     */
    private void loadProducts() {
        products.setAll(inventoryDAO.getAllProducts());
    }
    
    /**
     * Loads purchase orders from the database
     */
    private void loadOrders() {
        // Skip database loading if we're in mock data mode
        if (showingMockData) {
            return;
        }
        
        try {
            // Clear any existing orders first
            ordersList.clear();
            
            // Get the selected status filter value
            String statusFilter = statusFilterComboBox.getValue();
            
            // Use the DAO to get orders from database
            List<PurchaseOrder> orders;

            if ("Todos".equals(statusFilter)) {
                // Get all orders regardless of status
                orders = inventoryDAO.getAllPurchaseOrders();
            } else {
                // Filter by selected status
                orders = inventoryDAO.getPurchaseOrdersByStatus(statusFilter);
            }

            // Add orders to the observable list
            if (orders != null) {
                ordersList.addAll(orders);
            }

            // Show appropriate status message
            if (ordersList.isEmpty()) {
                statusLabel.setText("No se encontraron órdenes de compra en la base de datos");
            } else {
                statusLabel.setText("Se cargaron " + ordersList.size() + " órdenes de compra");
            }

            // Make sure the table refreshes its view
            ordersTable.refresh();
            
            // Update any status information
            updateOrderSummary();
            
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error cargando órdenes: " + e.getMessage());
        }
    }
    
    /**
     * Creates mock order data for demonstration purposes
     */
    private void createMockOrders() {
        // Clear existing mock orders to avoid duplicates
        ordersList.clear();
        
        // Add a pending order
        PurchaseOrder pendingOrder = new PurchaseOrder(1, "Distribuidora Láctea");
        pendingOrder.setId(1);
        pendingOrder.setStatus("PENDING");
        pendingOrder.setOrderDate(new Date());
        pendingOrder.addItem("123456789", 10, 80.0);
        ordersList.add(pendingOrder);
        
        // Add an ordered order
        PurchaseOrder orderedOrder = new PurchaseOrder(2, "Proveedor General");
        orderedOrder.setId(2);
        orderedOrder.setStatus("ORDERED");
        orderedOrder.setOrderDate(new Date());
        orderedOrder.addItem("987654321", 5, 120.0);
        ordersList.add(orderedOrder);
        
        // Add a received order with date from a week ago
        PurchaseOrder receivedOrder = new PurchaseOrder(3, "Importadora ABC");
        receivedOrder.setId(3);
        receivedOrder.setStatus("RECEIVED");
        
        // Set date to a week ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        receivedOrder.setOrderDate(cal.getTime());
        
        receivedOrder.addItem("456789123", 20, 45.0);
        ordersList.add(receivedOrder);
    }
    
    /**
     * Updates the order summary information (could display counts or totals)
     */
    private void updateOrderSummary() {
        // Example: Update a status label with order counts by status
        long pendingCount = ordersList.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long orderedCount = ordersList.stream().filter(o -> "ORDERED".equals(o.getStatus())).count();
        long receivedCount = ordersList.stream().filter(o -> "RECEIVED".equals(o.getStatus())).count();
        
        // If you have a status display label, you can update it
        if (statusLabel != null) {
            statusLabel.setText(String.format("Pedidos: %d pendientes, %d ordenados, %d recibidos", 
                                             pendingCount, orderedCount, receivedCount));
        }
    }
    
    /**
     * Updates the UI with current order information
     */
    private void updateOrderUI() {
        totalAmountField.setText(String.format("%.2f", currentOrder.getTotalAmount()));
    }
    
    /**
     * Creates a new purchase order
     */
    @FXML
    private void handleNewOrder() {
        currentOrder = new PurchaseOrder();
        orderItems.clear();
        supplierComboBox.setValue(null);
        orderDatePicker.setValue(LocalDate.now());
        expectedDatePicker.setValue(LocalDate.now().plusDays(7));
        totalAmountField.setText("0.00");
        notesArea.clear();
        orderNumberLabel.setText("New Order");
        statusLabel.setText("");
    }
    
    /**
     * Shows dialog to add item to order
     */
    @FXML
    private void handleAddItem() {
        // Validate that a supplier has been selected first
        if (supplierComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No supplier selected", 
                     "Please select a supplier before adding items to the order.");
            return;
        }
        
        Dialog<PurchaseOrder.Item> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Add product to order");
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the grid and add components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Product selection
        ComboBox<Product> productCombo = new ComboBox<>(products);
        TextField quantityField = new TextField("1");
        TextField priceField = new TextField("0.00");
        
        // Product converter
        productCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getName() + " (" + product.getBarcode() + ")" : "";
            }
            
            @Override
            public Product fromString(String string) {
                return null; // Not used for ComboBox
            }
        });
        
        // Auto-fill price when product selected
        productCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                priceField.setText(String.format("%.2f", newVal.getPurchasePrice()));
            }
        });
        
        grid.add(new Label("Product:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Price per unit:"), 0, 2);
        grid.add(priceField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the product field
        Platform.runLater(productCombo::requestFocus);
        
        // Convert the result to an order item when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Product selectedProduct = productCombo.getValue();
                    if (selectedProduct == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Product not selected", "Please select a product.");
                        return null;
                    }
                    
                    // Debug output
                    System.out.println("Quantity text: [" + quantityField.getText() + "]");
                    System.out.println("Price text: [" + priceField.getText() + "]");
                    
                    // Normalize input - replace comma with period for decimal separator
                    String normalizedQuantity = quantityField.getText().trim().replace(",", ".");
                    String normalizedPrice = priceField.getText().trim().replace(",", ".");
                    
                    // Parse with error handling
                    int quantity;
                    try {
                        quantity = Integer.parseInt(normalizedQuantity);
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity", 
                            "Please enter a valid integer for quantity: " + e.getMessage());
                        return null;
                    }
                    
                    if (quantity <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity", "Quantity must be greater than zero.");
                        return null;
                    }
                    
                    double price;
                    try {
                        price = Double.parseDouble(normalizedPrice);
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid price", 
                            "Please enter a valid number for price: " + e.getMessage());
                        return null;
                    }
                    
                    if (price < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid price", "Price cannot be negative.");
                        return null;
                    }
                    
                    return new PurchaseOrder.Item(selectedProduct.getBarcode(), quantity, price);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Unexpected error", 
                        "An unexpected error occurred: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });
        
        Optional<PurchaseOrder.Item> result = dialog.showAndWait();
        
        result.ifPresent(item -> {
            // Add item to the order
            currentOrder.addItem(item.getProductId(), item.getQuantity(), item.getPrice());
            orderItems.add(item);
            updateOrderUI();
        });
    }
    
    /**
     * Saves the current order
     */
    @FXML
    private void handleSaveOrder() {
        // Validate order
        if (supplierComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No supplier selected", "Please select a supplier for this order.");
            return;
        }
        
        if (orderItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No items in order", "Please add at least one item to the order.");
            return;
        }
        
        // Set order properties
        currentOrder.setSupplierId(supplierComboBox.getValue().getId());
        currentOrder.setSupplierName(supplierComboBox.getValue().getName());
        currentOrder.setOrderDate(Date.from(orderDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        currentOrder.setExpectedDate(expectedDatePicker.getValue() != null ?
            Date.from(expectedDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
        currentOrder.setNotes(notesArea.getText());
        currentOrder.setStatus("ORDERED");
        
        // Save order to database
        boolean success = inventoryDAO.createPurchaseOrder(currentOrder);
        
        if (success) {
            // Create a pending transaction record
            recordPurchaseTransaction(currentOrder, false);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order Created", "Purchase order has been created successfully.");
            
            // Make sure the new order appears in the orders list
            // Option 1: Add directly to the ordersList if we have the complete order
            if (currentOrder.getId() > 0) {  // If the DAO assigned an ID
                ordersList.add(currentOrder);
                ordersTable.refresh();
            }
            
            // Option 2: Or reload all orders to ensure everything is up to date
            loadOrders();
            
            handleNewOrder(); // Clear the form for a new order
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Order Creation Failed", "Could not create purchase order. Please try again.");
        }
    }
    
    /**
     * Displays an existing order
     */
    private void viewOrder(PurchaseOrder order) {
        // In a real app, we would load the full order from the database
        currentOrder = order;
        orderItems.setAll(order.getItems());
        orderNumberLabel.setText("Order #" + order.getId());
        
        // Find supplier in combo box
        for (Supplier supplier : supplierComboBox.getItems()) {
            if (supplier.getId() == order.getSupplierId()) {
                supplierComboBox.setValue(supplier);
                break;
            }
        }
        
        // Convert Date to LocalDate for DatePicker
        if (order.getOrderDate() != null) {
            orderDatePicker.setValue(order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        
        if (order.getExpectedDate() != null) {
            expectedDatePicker.setValue(order.getExpectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        
        totalAmountField.setText(String.format("%.2f", order.getTotalAmount()));
        notesArea.setText(order.getNotes());
        
        // Disable editing if order is already processed
        boolean isEditable = "PENDING".equals(order.getStatus());
        supplierComboBox.setDisable(!isEditable);
        orderDatePicker.setDisable(!isEditable);
        expectedDatePicker.setDisable(!isEditable);
        notesArea.setEditable(isEditable);
        
        statusLabel.setText("Order Status: " + order.getStatus());
    }
    
    /**
     * Procesa la recepción de una orden y registra el pago en un flujo integrado
     */
    private void receiveOrder(PurchaseOrder order) {
        try {
            // Cargar el diálogo de pago usando FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/purchase-payment-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // Obtener el controlador y pasar los datos de la orden
            PurchaseOrderPaymentController controller = loader.getController();
            controller.initData(order);
            controller.setReceivingMode(true); // Modo de recepción activado
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Recibir y Registrar Pago - Orden #" + order.getId());
            
            // Mostrar el diálogo y esperar respuesta
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Si el usuario confirmó la recepción y pago
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // La transacción contable ahora se registrará en PurchaseOrderPaymentController
                // pero también podemos registrarla aquí como respaldo
                recordPurchaseTransaction(order, true);
                
                // Refrescar la lista de órdenes para reflejar los cambios
                handleRefreshOrders();
                
                showAlert(Alert.AlertType.INFORMATION, "Orden Procesada", 
                          "Orden recibida y pago registrado", 
                          "La orden ha sido recibida exitosamente y el pago ha sido registrado.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al procesar orden", 
                      "No se pudo procesar la recepción y pago: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Muestra el diálogo para registrar un pago a una orden existente
     */
    private void showPaymentDialog(PurchaseOrder order) {
        try {
            // Cargar el diálogo de pago usando FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/purchase-payment-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // Obtener el controlador y pasar los datos de la orden
            PurchaseOrderPaymentController controller = loader.getController();
            controller.initData(order);
            controller.setReceivingMode(false); // Modo de solo pago
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Registrar Pago - Orden #" + order.getId());
            
            // Mostrar el diálogo y esperar respuesta
            Optional<ButtonType> result = dialog.showAndWait();
            
            // Si el usuario confirmó el pago
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Registrar pago en contabilidad
                recordPurchaseTransaction(order, true);
                
                // Refrescar la lista de órdenes para reflejar los cambios
                handleRefreshOrders();
                
                showAlert(Alert.AlertType.INFORMATION, "Pago Registrado", 
                          "Pago registrado correctamente", 
                          "El pago a la orden ha sido registrado correctamente.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al procesar pago", 
                      "No se pudo procesar el pago: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método para registrar transacciones contables para compras
     * @param order La orden de compra
     * @param isPayment true si es un pago real, false si es solo una reserva/pedido
     */
    private void recordPurchaseTransaction(PurchaseOrder order, boolean isPayment) {
        try {
            // Crear descripción detallada
            String description = String.format(
                "Compra #%d - %s - %s", 
                order.getId(), 
                order.getSupplierName(),
                isPayment ? "Pago realizado" : "Pedido registrado"
            );
            
            // Si es un pago real, registramos transacción negativa (egreso)
            if (isPayment) {
                Transaction transaction = new Transaction(
                    "compra",
                    -order.getTotalAmount(), // Negativo porque es un egreso
                    description
                );
                
                // Establecer fecha actual
                transaction.setTimestamp(LocalDateTime.now());
                
                // Registrar en el módulo contable (esto actualiza la base de datos)
                boolean success = accountingModule.recordTransaction(transaction);
                
                if (success) {
                    System.out.println("Transacción de compra registrada correctamente: " + description);
                } else {
                    System.err.println("Error al registrar transacción de compra: " + description);
                }
            } else {
                // Si es solo un pedido, no registramos movimiento contable real
                // pero podríamos registrar una reserva o compromiso si fuera necesario
                System.out.println("Pedido registrado (pendiente de recepción y pago): " + description);
            }
        } catch (Exception e) {
            System.err.println("Error al registrar transacción contable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Cancels the current order
     */
    @FXML
    private void handleCancelOrder() {
        if (orderItems.isEmpty()) {
            handleNewOrder();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Order");
        alert.setHeaderText("Cancel Current Order");
        alert.setContentText("Are you sure you want to cancel this order? All items will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleNewOrder();
        }
    }

    /**
     * Toggles between real database data and mock data
     */
    private void toggleMockData() {
        showingMockData = !showingMockData;
        if (showingMockData) {
            // Show mock data
            createMockOrders();
            mockDataButton.setText("Usar datos reales");
            mockDataButton.setStyle("-fx-background-color: #ffcc80;"); // Orange warning color
            statusLabel.setText("Mostrando datos de ejemplo (no conectado a base de datos)");
        } else {
            // Show real data
            loadOrders();
            mockDataButton.setText("Usar datos de ejemplo");
            mockDataButton.setStyle("-fx-background-color: #e0e0e0;");
        }
    }

    // Add a refresh method to easily reload data from database
    @FXML
    private void handleRefreshOrders() {
        // Reset to real data mode
        showingMockData = false;
        mockDataButton.setText("Usar datos de ejemplo");
        mockDataButton.setStyle("-fx-background-color: #e0e0e0;");
        
        // Clear and reload
        ordersList.clear();
        loadOrders();
    }
}
