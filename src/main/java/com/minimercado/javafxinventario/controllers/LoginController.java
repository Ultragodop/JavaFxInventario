package com.minimercado.javafxinventario.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.minimercado.javafxinventario.modules.SecurityModule;

import java.util.Objects;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField adminCodeField;
    @FXML private Label errorLabel;

    private SecurityModule securityModule = new SecurityModule();

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String userpasswd = passwordField.getText();
        boolean authenticated = securityModule.login(username, userpasswd);
        if (authenticated) {
            errorLabel.setText("Acceso concedido");
            try {
                // Close the current stage
                // Load and show new scene in a new stage
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("inventory-view.fxml")));
                Scene scene = new Scene(root, 800, 600);
                Stage newStage = new Stage();
                newStage.setScene(scene);
                newStage.show();
                Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                currentStage.close();
            } catch(Exception e) {
                errorLabel.setText("Error al cargar inventario: " + e.getMessage());
            }
        } else {
            errorLabel.setText("Credenciales incorrectas");
        }
    }

    @FXML
    protected void handleRegister() {
        String username = usernameField.getText();
        String userpasswd = passwordField.getText();
        String adminCode = adminCodeField.getText();
        try {
            boolean registered = securityModule.registerAdmin(username, userpasswd, adminCode);
            if (registered) {
                errorLabel.setText("Registro exitoso. !TE PERMITO! !OJO YO TE TENGO QUE PERMITIR! iniciar sesión.");
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
}
