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

public class SystemInitializer extends Application {
    private AccountingModule accountingModule;
    private InventoryModule inventoryModule;
    private SalesModule salesModule;
    private SecurityModule securityModule;
    private ApiGateway apiGateway;

    @Override
    public void start(Stage stage) throws IOException {
        // ...inicialización de módulos de negocio...
        initAccountingModule();
        initInventoryModule();
        initSalesModule();
        initSecurityModule();
        initApiGateway();
        FXMLLoader fxmlLoader = new FXMLLoader(SystemInitializer.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // ventana aumentada para mayor escalabilidad
        stage.setTitle("Sistema Inventario y Contabilidad");
        stage.setScene(scene);
        stage.show();
    }

    private void initAccountingModule() {
        accountingModule = new AccountingModule();
        // Registro de transacciones, generación de balances, integración con impuestos y reportes financieros
    }

    private void initInventoryModule() {
        inventoryModule = new InventoryModule();
        // Control de stock, alertas de inventario bajo, gestión de proveedores y seguimiento de caducidad
    }

    private void initSalesModule() {
        salesModule = new SalesModule();
        // Punto de Venta (POS), facturación electrónica y historial de transacciones por cliente
    }

    private void initSecurityModule() {
        securityModule = new SecurityModule();
        // Autenticación y autorización de usuarios
    }

    private void initApiGateway() {
        apiGateway = new ApiGateway();
        // Centralizar peticiones, logs, auditoría y notificaciones (correo, SMS)
    }

    public static void main(String[] args) {
        // ...inicialización de conexión a BD, pooling, etc...
        launch();
    }
}
