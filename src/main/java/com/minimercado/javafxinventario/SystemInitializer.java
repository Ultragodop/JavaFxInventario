package com.minimercado.javafxinventario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.InventoryModule;
import com.minimercado.javafxinventario.modules.SalesModule;
import com.minimercado.javafxinventario.modules.SecurityModule;
import com.minimercado.javafxinventario.modules.ApiGateway;
import com.minimercado.javafxinventario.utils.ThemeManager;

public class SystemInitializer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/main-menu.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Apply theme
            ThemeManager.applyTheme(scene);
            
            stage.setTitle("Sistema de Gestión de Inventario");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            
            // Initialize modules
            initModules();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    private void initModules() {
        initAccountingModule();
        initInventoryModule();
        initSalesModule();
        initSecurityModule();
        initApiGateway();
    }

    private void initAccountingModule() {
        // Initialize accounting module
        AccountingModule.getInstance();
    }

    private void initInventoryModule() {
        // Initialize inventory module
        InventoryModule.getInstance();
    }

    private void initSalesModule() {
        // Initialize sales module (handled through dependency injection)
    }

    private void initSecurityModule() {
        // Initialize security module
    }

    private void initApiGateway() {
        // Initialize API gateway for external services
    }

    public static void main(String[] args) {
        launch(args);
    }
}
