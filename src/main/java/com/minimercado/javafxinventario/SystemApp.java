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

public class SystemApp extends Application {
    private AccountingModule accountingModule;
    private InventoryModule inventoryModule;
    private SalesModule salesModule;
    private SecurityModule securityModule;
    private ApiGateway apiGateway;

    @Override
    public void start(Stage stage) throws IOException {
        initAccountingModule();
        initInventoryModule();
        initSalesModule();
        initSecurityModule();
        initApiGateway();
        FXMLLoader loader = new FXMLLoader(SystemApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("Sistema Inventario y Contabilidad");
        stage.setScene(scene);
        stage.show();
    }

    private void initAccountingModule() { accountingModule = new AccountingModule(); }
    private void initInventoryModule() { inventoryModule = new InventoryModule(); }
    private void initSalesModule() { salesModule = new SalesModule(); }
    private void initSecurityModule() { securityModule = new SecurityModule(); }
    private void initApiGateway() { apiGateway = new ApiGateway(); }

    public static void main(String[] args) {
        launch();
    }
}
