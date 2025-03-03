package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.controllers.InventoryController;
import com.minimercado.javafxinventario.modules.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class InventoryViewController {
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> thresholdColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField stockField;
    @FXML private TextField thresholdField;
    @FXML private TextField priceField;
    @FXML private Label statusLabel;
    
    private InventoryController inventoryController = new InventoryController();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        stockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        thresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getThreshold()).asObject());
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        
        productsTable.setItems(productList);
        loadProducts();
    }
    
    private void loadProducts() {
        // ...existing code para cargar productos, si aplica...
    }
    
    @FXML
    protected void handleAddProduct() {
        try {
            String id = idField.getText();
            String name = nameField.getText();
            int stock = Integer.parseInt(stockField.getText());
            int threshold = Integer.parseInt(thresholdField.getText());
            double price = Double.parseDouble(priceField.getText());
            Product p = new Product(id, name, stock, threshold, price);
            inventoryController.addProduct(p);
            productList.add(p);
            statusLabel.setText("Producto agregado exitosamente.");
        } catch(Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleUpdateProduct() {
        try {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Seleccione un producto para actualizar.");
                return;
            }
            selected.setName(nameField.getText());
            selected.setStock(Integer.parseInt(stockField.getText()));
            selected.setThreshold(Integer.parseInt(thresholdField.getText()));
            selected.setPrice(Double.parseDouble(priceField.getText()));
            productsTable.refresh();
            statusLabel.setText("Producto actualizado exitosamente.");
        } catch(Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleRemoveProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Seleccione un producto para eliminar.");
            return;
        }
        inventoryController.getStockManager().removeProduct(selected);
        productList.remove(selected);
        statusLabel.setText("Producto eliminado exitosamente.");
    }
}
