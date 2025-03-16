package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.awt.Desktop;
import java.io.File;

public class MainMenuController {
    @FXML private StackPane contentPane;
    
    @FXML
    private void initialize() {
        // No cargar ningún módulo por defecto para mostrar solo los botones
    }
    
    // Map handleBuscarProductos to handleInventory for consistency
    @FXML
    private void handleInventory() {
        loadModule("/com/minimercado/javafxinventario/inventory-view.fxml");
    }
    
    // Keep for backward compatibility
    @FXML
    private void handleBuscarProductos() {
        handleInventory();
    }

    // Map handleVentas to handlePOS for consistency
    @FXML
    private void handlePOS() {
        loadModule("/com/minimercado/javafxinventario/venta-view.fxml");
    }
    
    // Keep for backward compatibility
    @FXML
    private void handleVentas() {
        handlePOS();
    }

    // Map handleContabilidad to handleFinancialReports for consistency
    @FXML
    private void handleFinancialReports() {
        loadModule("/com/minimercado/javafxinventario/contabilidad-view.fxml");
    }
    
    // Keep for backward compatibility
    @FXML
    private void handleContabilidad() {
        handleFinancialReports();
    }
    
    // Map handlePurchaseOrder to handlePurchaseOrders for consistency
    @FXML
    private void handlePurchaseOrders() {
        loadModule("/com/minimercado/javafxinventario/purchase-order-view.fxml");
    }
    
    // Keep for backward compatibility
    @FXML
    private void handlePurchaseOrder() {
        handlePurchaseOrders();
    }
    
    @FXML
    private void handleSuppliers() {
        loadModule("/com/minimercado/javafxinventario/supplier-view.fxml");
    }

    // Keep for backward compatibility
    @FXML
    private void handleReporteFinanciero() {
        handleFinancialReports();
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
    
    // Implementing missing methods from FXML
    
    @FXML
    private void handleSettings() {
        showNotImplementedMessage("Configuración del Sistema");
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    @FXML
    private void handleCategories() {
        loadModule("/com/minimercado/javafxinventario/categories-view.fxml");
    }
    
    @FXML
    private void handleInventoryReports() {
        loadModule("/com/minimercado/javafxinventario/inventory-reports-view.fxml");
    }
    
    @FXML
    private void handleSalesSearch() {
        loadModule("/com/minimercado/javafxinventario/sales-search-view.fxml");
    }
    
    @FXML
    private void handleSalesReports() {
        loadModule("/com/minimercado/javafxinventario/sales-reports-view.fxml");
    }
    
    @FXML
    private void handleReceiveProducts() {
        loadModule("/com/minimercado/javafxinventario/receive-products-view.fxml");
    }
    
    @FXML
    private void handleExpenses() {
        loadModule("/com/minimercado/javafxinventario/expenses-view.fxml");
    }
    
    @FXML
    private void handleExpenseCategories() {
        loadModule("/com/minimercado/javafxinventario/expense-categories-view.fxml");
    }
    
    @FXML
    private void handleEmployees() {
        loadModule("/com/minimercado/javafxinventario/employees-view.fxml");
    }
    @FXML
    private void handleFinancyReports() {
        loadModule("/com/minimercado/javafxinventario/FinancialReportView.fxml");
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Sistema de Gestión de Inventario");
        alert.setContentText("Versión 1.0\nDesarrollado por TuEmpresa\nCopyright © 2023");
        alert.showAndWait();
    }
    
    @FXML
    private void handleUserManual() {
        try {
            File userManual = new File("docs/manual.pdf");
            if (userManual.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(userManual);
            } else {
                showError("Manual no disponible", "No se pudo abrir el manual de usuario.");
            }
        } catch (Exception e) {
            showError("Error", "No se pudo abrir el manual: " + e.getMessage());
        }
    }
    
    private void showNotImplementedMessage(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funcionalidad no implementada");
        alert.setHeaderText(feature);
        alert.setContentText("Esta funcionalidad aún no está implementada.");
        alert.showAndWait();
    }
    
    private void loadModule(String fxmlPath) {
        try {
            // Debug output to verify the path
            System.out.println("Loading FXML from path: " + fxmlPath);
            
            // Get the resource URL
            URL fxmlUrl = getClass().getResource(fxmlPath);
            
            // Debug - check if URL is null
            if (fxmlUrl == null) {
                System.err.println("FXML resource not found at path: " + fxmlPath);
                throw new IOException("Could not locate FXML file: " + fxmlPath);
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent viewNode = loader.load();
            contentPane.getChildren().setAll(viewNode);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error Loading Module", "Could not load the requested module: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to verify if a resource exists at a given path
     * @param resourcePath The path to check
     */
    private void checkResourceExists(String resourcePath) {
        URL resourceUrl = getClass().getResource(resourcePath);
        if (resourceUrl == null) {
            System.err.println("Resource NOT FOUND: " + resourcePath);
        } else {
            System.out.println("Resource found: " + resourcePath + " -> " + resourceUrl);
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showToast(String message) {
        // Simple implementation - in a real app would show a proper toast notification
        System.out.println(message);
    }
}
