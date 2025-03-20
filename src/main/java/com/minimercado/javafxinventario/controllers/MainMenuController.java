package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

/**
 * Controlador para el menú principal de navegación
 */
public class MainMenuController {
    @FXML private Label lblTituloSeccion;
    @FXML private StackPane contentPane;
    @FXML private FontIcon themeIcon;
    @FXML private Button btnSalir;
    @FXML private Button btnInventario;
    @FXML private Button btnVentas;
    @FXML private Button btnCompras;
    @FXML private Button btnGastos;
    @FXML private Button btnReportesGen;
    @FXML private Button btnReportesFin;
    @FXML private Button btnEmpleados;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnTema;
    @FXML private Label lblUsuario;
    
    private boolean isDarkTheme = false;
    
    /**
     * Método de inicialización que se llama automáticamente después de cargar el FXML
     */
    @FXML
    private void initialize() {
        // Inicialización del controlador
        updateThemeIcon();
        
        // Inicializar el texto de usuario si es necesario
        if (lblUsuario != null) {
            lblUsuario.setText("Usuario: Admin");
        }
    }
    
    /**
     * Maneja el evento para la sección de Inventario
     */
    @FXML
    private void handleInventario() {
        lblTituloSeccion.setText("Gestión de Inventario");
        loadModule("/com/minimercado/javafxinventario/inventory-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Ventas
     */
    @FXML
    private void handleVentas() {
        lblTituloSeccion.setText("Punto de Venta");
        loadModule("/com/minimercado/javafxinventario/venta-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Compras
     */
    @FXML
    private void handleCompras() {
        lblTituloSeccion.setText("Gestión de Compras");
        loadModule("/com/minimercado/javafxinventario/purchase-order-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Gastos
     */
    @FXML
    private void handleGastos() {
        lblTituloSeccion.setText("Control de Gastos");
        loadModule("/com/minimercado/javafxinventario/expenses-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Reportes Generales
     */
    @FXML
    private void handleReportesGenerales() {
        lblTituloSeccion.setText("Reportes Generales");
        loadModule("/com/minimercado/javafxinventario/FinancialReportView.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Reportes Financieros
     */
    @FXML
    private void handleReportesFinancieros() {
        lblTituloSeccion.setText("Reportes Financieros");
        loadModule("/com/minimercado/javafxinventario/views/financial-reports-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Empleados
     */
    @FXML
    private void handleEmpleados() {
        lblTituloSeccion.setText("Gestión de Empleados");
        loadModule("/com/minimercado/javafxinventario/employees-view.fxml");
    }
    
    /**
     * Maneja el evento para la sección de Configuración
     */
    @FXML
    private void handleConfiguracion() {
        lblTituloSeccion.setText("Configuración del Sistema");
        loadModule("/com/minimercado/javafxinventario/settings-view.fxml");
    }
    
    /**
     * Maneja el cambio de tema (claro/oscuro)
     */
    @FXML
    private void handleCambiarTema() {
        isDarkTheme = !isDarkTheme;
        ThemeManager.setTheme(contentPane.getScene(), isDarkTheme ? "dark" : "light");
        updateThemeIcon();
    }
    
    /**
     * Actualiza el ícono según el tema actual
     */
    private void updateThemeIcon() {
        if (themeIcon != null) {
            themeIcon.setIconLiteral(isDarkTheme ? "fas-sun" : "fas-moon");
        }
    }
    
    /**
     * Maneja el evento de salir de la aplicación
     */
    @FXML
    private void handleSalir() {
        Stage stage = (Stage) btnSalir.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Carga un módulo en el área de contenido
     * @param fxmlPath La ruta al archivo FXML a cargar
     */
    private void loadModule(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent moduleView = loader.load();
            
            // Limpia y coloca la nueva vista
            contentPane.getChildren().clear();
            contentPane.getChildren().add(moduleView);
            
        } catch (IOException e) {
            System.err.println("Error al cargar el módulo: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar mensaje de error en la interfaz
            showModuleError(fxmlPath, e);
        }
    }
    
    /**
     * Muestra un mensaje de error cuando falla la carga de un módulo
     * @param modulePath Ruta del módulo que falló al cargar
     * @param error La excepción de error ocurrida
     */
    private void showModuleError(String modulePath, Exception error) {
        // Crear un componente visual para mostrar el error
        Label errorLabel = new Label("Error al cargar el módulo: " + modulePath + "\n" + error.getMessage());
        errorLabel.getStyleClass().add("error-message");
        
        contentPane.getChildren().clear();
        contentPane.getChildren().add(errorLabel);
    }

}
