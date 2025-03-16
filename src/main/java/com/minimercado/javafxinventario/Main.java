package com.minimercado.javafxinventario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane mainPane;
    private String currentView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainPane = new BorderPane();
        Scene scene = new Scene(mainPane, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Inventario");
        primaryStage.show();

        // Load initial view
        handleMenuItemClick("home");
    }

    private void handleMenuItemClick(String viewName) {
        try {
            if ("home".equals(viewName)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/HomeView.fxml"));
                Parent root = loader.load();
                mainPane.setCenter(root);
                currentView = viewName;
            } else if ("products".equals(viewName)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/ProductsView.fxml"));
                Parent root = loader.load();
                mainPane.setCenter(root);
                currentView = viewName;
            } else if ("suppliers".equals(viewName)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/SuppliersView.fxml"));
                Parent root = loader.load();
                mainPane.setCenter(root);
                currentView = viewName;
            } else if ("purchase-orders".equals(viewName)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/PurchaseOrdersView.fxml"));
                Parent root = loader.load();
                mainPane.setCenter(root);
                currentView = viewName;
            } else if ("financial-report".equals(viewName)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/FinancialReportView.fxml"));
                Parent root = loader.load();
                mainPane.setCenter(root);
                currentView = viewName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}