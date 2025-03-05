package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import com.minimercado.javafxinventario.modules.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InventoryViewController {
    @FXML private HBox searchBox;
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
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    
    // Paneles de opciones
    @FXML private VBox agregarPane;
    @FXML private VBox actualizarPane;
    @FXML private VBox eliminarPane;
    @FXML private VBox searchPane;
    
    // Panel búsqueda general
    @FXML private TextField searchFieldBuscar;
    @FXML private TableView<Product> searchResultsTable;
    @FXML private TableColumn<Product, String> searchIdColumn;
    @FXML private TableColumn<Product, String> searchNameColumn;
    @FXML private TableColumn<Product, String> searchStockColumn;
    @FXML private TableColumn<Product, String> searchThresholdColumn;
    @FXML private TableColumn<Product, String> searchPriceColumn;
    @FXML private Label statusLabelBuscar;
    
    // Panel de actualización
    @FXML private TextField searchFieldUpdate;
    @FXML private TableView<Product> updateResultsTable;
    @FXML private TableColumn<Product, String> updateIdColumn;
    @FXML private TableColumn<Product, String> updateNameColumn;
    @FXML private TextField newPriceField;
    @FXML private Label statusLabelUpdate;
    
    // Panel de eliminación
    @FXML private TextField searchFieldDelete;
    @FXML private TableView<Product> deleteResultsTable;
    @FXML private TableColumn<Product, String> deleteIdColumn;
    @FXML private TableColumn<Product, String> deleteNameColumn;
    @FXML private Label statusLabelDelete;
    
    @FXML private StackPane optionsStack;
    
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Inicialización de la tabla principal
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        stockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        thresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getThreshold()).asObject());
        priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        productsTable.setItems(productList);
        loadProducts();
        
        // Inicialización de columnas para actualización
        updateIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        updateNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        // Inicialización de columnas para eliminación, validando inyección desde el FXML
        if(deleteIdColumn == null || deleteNameColumn == null) {
            System.err.println("Verificar fx:id de las columnas de eliminación.");
        } else {
            deleteIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
            deleteNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        }
        
        // Inicialización de columnas para el panel de búsqueda
        if(searchPane != null) {
            searchIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
            searchNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
            searchStockColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asString());
            searchThresholdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getThreshold()).asString());
            searchPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asString());
        }
    }
    
    private void loadProducts() {
        productList.setAll(inventoryDAO.getAllProducts());
    }
    
    // Método genérico para actualizar resultados de búsqueda en un TableView según un TextField
    private void updateSearchResults(TextField queryField, TableView<Product> table, Label statusLabel, String emptyMsg, String notFoundMsg) {
        String query = queryField.getText().trim();
        if(query.isEmpty()){
            statusLabel.setText(emptyMsg);
            table.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        statusLabel.setText(results.isEmpty() ? notFoundMsg : "");
        table.setItems(results);
    }
    
    // Métodos de manejo de productos
    @FXML
    protected void handleAddProduct() {
        try {
            Product p = new Product(
                idField.getText(),
                nameField.getText(),
                Integer.parseInt(stockField.getText()),
                Integer.parseInt(thresholdField.getText()),
                Double.parseDouble(priceField.getText())
            );
            try {
                inventoryDAO.addProduct(p);
                loadProducts();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                return;
            }

            statusLabel.setText("Producto agregado exitosamente.");
        } catch(Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleUpdateProduct() {
        try {
            Product selected = productList.stream().filter(p -> p.getId().equals(idField.getText())).findFirst().orElse(null);
            if (selected == null) {
                statusLabel.setText("Seleccione un producto para actualizar.");
                return;
            }
            selected.setName(nameField.getText());
            selected.setStock(Integer.parseInt(stockField.getText()));
            selected.setThreshold(Integer.parseInt(thresholdField.getText()));
            selected.setPrice(Double.parseDouble(priceField.getText()));
            inventoryDAO.updateProduct(selected);
            loadProducts();
            statusLabel.setText("Producto actualizado exitosamente.");
        } catch(Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleRemoveProduct() {
        Product selected = productList.stream().filter(p -> p.getId().equals(idField.getText())).findFirst().orElse(null);
        if (selected == null) {
            statusLabel.setText("Seleccione un producto para eliminar.");
            return;
        }
        inventoryDAO.removeProduct(selected.getId());
        loadProducts();
        statusLabel.setText("Producto eliminado exitosamente.");
    }
    
    @FXML
    protected void handleSearchProduct() {
        String query = searchField.getText().trim();
        if(query.isEmpty()){
            loadProducts();
        } else {
            productList.setAll(inventoryDAO.searchProducts(query));
        }
    }
    
    // Manejo de paneles: ocultar todos y mostrar solo el deseado
    private void showOnly(VBox paneToShow) {
        agregarPane.setVisible(false);
        actualizarPane.setVisible(false);
        eliminarPane.setVisible(false);
        searchPane.setVisible(false);
        paneToShow.setVisible(true);
    }
    
    @FXML
    private void showAgregar() {
        showOnly(agregarPane);
    }
    
    @FXML
    private void showActualizar() {
        showOnly(actualizarPane);
    }
    
    @FXML
    private void showEliminar() {
        showOnly(eliminarPane);
    }
    
    @FXML
    private void showBuscar() {
        showOnly(searchPane);
    }
    
    // Manejo de búsqueda en el panel de actualización
    @FXML
    private void handleSearchUpdateProducts() {
        updateSearchResults(searchFieldUpdate, updateResultsTable, statusLabelUpdate,
                "Ingrese un criterio de búsqueda.", "No se encontraron productos.");
    }
    
    @FXML
    protected void handleUpdateProductPrice() {
        Product selected = updateResultsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            statusLabelUpdate.setText("Seleccione un producto de la lista.");
            return;
        }
        try {
            double newPrice = Double.parseDouble(newPriceField.getText().trim());
            selected.setPrice(newPrice);
            inventoryDAO.updateProduct(selected);
            loadProducts();
            updateResultsTable.refresh();
            statusLabelUpdate.setText("Precio actualizado exitosamente.");
        } catch(Exception e) {
            statusLabelUpdate.setText("Error: " + e.getMessage());
        }
    }
    
    // Manejo de búsqueda en el panel de eliminación
 
    
    // Método para eliminar producto a partir de la búsqueda
    @FXML
    protected void handleRemoveProductBySearch() {
        String query = searchFieldDelete.getText().trim();
        if(query.isEmpty()){
            statusLabelDelete.setText("Ingrese un criterio de búsqueda.");
            return;
        }
        Product p = inventoryDAO.searchProducts(query).stream().findFirst().orElse(null);
        if(p == null){
            statusLabelDelete.setText("Producto no encontrado.");
            return;
        }
        inventoryDAO.removeProduct(p.getId());
        loadProducts(); // Se recarga la lista de productos actualizando el panel de agregar
        statusLabelDelete.setText("Producto eliminado exitosamente.");
    }
    
    // Nuevo método para buscar productos a eliminar
    @FXML
    private void handleSearchDeleteProducts() {
        String query = searchFieldDelete.getText().trim();
        if(query.isEmpty()){
            statusLabelDelete.setText("Ingrese un criterio de búsqueda.");
            deleteResultsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        if(results.isEmpty()){
            statusLabelDelete.setText("No se encontraron productos.");
        } else {
            statusLabelDelete.setText("");
        }
        deleteResultsTable.setItems(results);
    }
    
    // Nuevo método para eliminar el producto seleccionado de la lista de eliminación
    @FXML
    protected void handleRemoveProductBySelection() {
        Product selected = deleteResultsTable.getSelectionModel().getSelectedItem();
        if(selected == null) {
            statusLabelDelete.setText("Seleccione un producto de la lista.");
            return;
        }
        try {
            inventoryDAO.removeProduct(selected.getId());
            loadProducts(); // Se recarga la lista de productos actualizando el panel de agregar
            deleteResultsTable.getItems().remove(selected);
            statusLabelDelete.setText("Producto eliminado exitosamente.");
        } catch(Exception e) {
            statusLabelDelete.setText("Error: " + e.getMessage());
        }
    }
    
    // Método que ejecuta la búsqueda en el nuevo panel
    @FXML
    private void handleBuscarProducto() {
        String query = searchFieldBuscar.getText().trim();
        if(query.isEmpty()){
            statusLabelBuscar.setText("Ingrese un criterio de búsqueda.");
            searchResultsTable.getItems().clear();
            return;
        }
        ObservableList<Product> results = FXCollections.observableArrayList(inventoryDAO.searchProducts(query));
        if(results.isEmpty()){
            statusLabelBuscar.setText("No se encontraron productos.");
        } else {
            statusLabelBuscar.setText("");
        }
        searchResultsTable.setItems(results);
    }
}
