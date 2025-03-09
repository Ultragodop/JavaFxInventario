package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.UserAuthentication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login view
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    
    private UserAuthentication auth;
    
    @FXML
    private void initialize() {
        auth = UserAuthentication.getInstance();
        
        // Set focus on username field
        usernameField.requestFocus();
        
        // Add enter key handler to perform login
        passwordField.setOnAction(event -> handleLogin());
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Missing Information", 
                    "Please enter both username and password.");
            return;
        }
        
        // Attempt authentication
        if (auth.authenticate(username, password)) {
            try {
                // Load the main menu
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/main-menu.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Sistema de Gestión de Inventario - Bienvenido " + username);
                
                // For testing, show role
                System.out.println("Logged in as: " + username + ", Role: " + auth.getUserRole());
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Navigation Error", 
                        "Could not load the main menu: " + e.getMessage());
            }
        } else {
            showAlert("Authentication Failed", "Invalid Credentials", 
                    "The username or password you entered is incorrect.");
            passwordField.clear();
        }
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
