package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.PurchaseOrder;
import com.minimercado.javafxinventario.modules.Supplier;
import javafx.application.Platform;  // Added missing import for Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
        
        // Add action buttons to orders table
        TableColumn<PurchaseOrder, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button receiveButton = new Button("Receive");
            private final HBox pane = new HBox(5, viewButton, receiveButton);
            
            {
                viewButton.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    viewOrder(order);
                });
                
                receiveButton.setOnAction(event -> {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    receiveOrder(order);
                });
                
                receiveButton.setStyle("-fx-background-color: #66cc66;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder order = getTableView().getItems().get(getIndex());
                    // Only allow receiving orders that are in ORDERED status
                    receiveButton.setDisable(!"ORDERED".equals(order.getStatus()));
                    setGraphic(pane);
                }
            }
        });
        
        ordersTable.getColumns().add(actionColumn);
        ordersTable.setItems(ordersList);
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
        // In a real implementation, this would filter by the selected status
        // For now, we'll just load all orders (mock implementation)
        // ordersList.setAll(inventoryDAO.getAllPurchaseOrders(statusFilterComboBox.getValue()));
        
        // Mock data for now
        if (ordersList.isEmpty()) {
            PurchaseOrder mockOrder = new PurchaseOrder(1, "Distribuidora Láctea");
            mockOrder.setId(1);
            mockOrder.setStatus("ORDERED");
            mockOrder.setOrderDate(new Date());
            mockOrder.addItem("123456789", 10, 80.0);
            ordersList.add(mockOrder);
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
                    
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity", "Quantity must be greater than zero.");
                        return null;
                    }
                    
                    double price = Double.parseDouble(priceField.getText());
                    if (price < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Invalid price", "Price cannot be negative.");
                        return null;
                    }
                    
                    return new PurchaseOrder.Item(selectedProduct.getBarcode(), quantity, price);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid input", "Please enter valid numbers for quantity and price.");
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
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order Created", "Purchase order has been created successfully.");
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
     * Process receiving an order
     */
    private void receiveOrder(PurchaseOrder order) {
        if (!"ORDERED".equals(order.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cannot Receive Order", "Only orders with ORDERED status can be received.");
            return;
        }
        
        // Create a dialog for receiving the order
        Dialog<Date> dialog = new Dialog<>();
        dialog.setTitle("Receive Order");
        dialog.setHeaderText("Receive Purchase Order #" + order.getId());
        
        // Set the button types
        ButtonType receiveButtonType = new ButtonType("Receive", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(receiveButtonType, ButtonType.CANCEL);
        
        // Create the grid and add components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker receiveDatePicker = new DatePicker(LocalDate.now());
        TextArea receiveNotesArea = new TextArea();
        receiveNotesArea.setPrefRowCount(3);
        
        grid.add(new Label("Receive Date:"), 0, 0);
        grid.add(receiveDatePicker, 1, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(receiveNotesArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the date picker
        Platform.runLater(receiveDatePicker::requestFocus);
        
        // Convert the result when the receive button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == receiveButtonType) {
                return Date.from(receiveDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            return null;
        });
        
        Optional<Date> result = dialog.showAndWait();
        
        result.ifPresent(receiveDate -> {
            boolean success = inventoryDAO.receivePurchaseOrder(order.getId(), receiveDate, receiveNotesArea.getText());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order Received", "Purchase order has been received successfully.");
                // Update the order status in the list
                loadOrders();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Receiving Failed", "Could not process the order reception. Please try again.");
            }
        });
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
}
