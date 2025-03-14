package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenuController {
    @FXML private StackPane contentPane;
    
    @FXML
    private void initialize() {
        // No cargar ningún módulo por defecto para mostrar solo los botones
    }
    
    @FXML
    private void handleBuscarProductos() {
        loadModule("/com/minimercado/javafxinventario/inventory-view.fxml");
    }

    @FXML
    private void handleVentas() {
        loadModule("/com/minimercado/javafxinventario/venta-view.fxml");
    }

    @FXML
    private void handleContabilidad() {
        loadModule("/com/minimercado/javafxinventario/contabilidad-view.fxml");
    }
    
    @FXML
    private void handlePurchaseOrder() {
        loadModule("/com/minimercado/javafxinventario/purchase-order-view.fxml");
    }
    
    @FXML
    private void handleSuppliers() {
        loadModule("/com/minimercado/javafxinventario/supplier-view.fxml");
    }

    
    @FXML
    private void handleToggleTheme() {
        Scene scene = contentPane.getScene();
        String newTheme = ThemeManager.toggleTheme(scene);
        
        // Update status or show toast notification
        showToast("Theme changed to " + (newTheme.equals("dark") ? "Dark" : "Light") + " mode");
    }
    
    @FXML
    private void handleThemeSettings() {
        Scene scene = contentPane.getScene();
        ThemeManager.showThemeDialog(scene);
    }
    
    private void loadModule(String fxmlPath) {
        try {
            Parent moduleRoot = FXMLLoader.load(getClass().getResource(fxmlPath));

            contentPane.getChildren().setAll(moduleRoot);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showToast(String message) {
        // Simple implementation - in a real app would show a proper toast notification
        System.out.println(message);
    }
}
