package com.minimercado.javafxinventario;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class SystemAppController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        // Se simula la verificación de seguridad y la carga del módulo de inventario
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        welcomeText.getScene().setRoot(loader.getRoot());
        
        // ...llamar a métodos que inicialicen InventoryManager, SecurityManager, etc...
    }
}