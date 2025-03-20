package com.minimercado.javafxinventario;

import com.minimercado.javafxinventario.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.IkonResolver;

/**
 * Clase principal que inicia la aplicación
 */
public class SystemInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Registrar las fuentes de iconos antes de cargar el FXML
        registerIkonHandler();
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/main-menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        // Aplicar tema predeterminado
        ThemeManager.applyDefaultTheme(scene);
        
        // Configurar ventana principal
        stage.setTitle("Sistema de Gestión de Inventario");
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/minimercado/javafxinventario/icons/app-icon.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el ícono de la aplicación: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }
    
    /**
     * Registra los manejadores de iconos de Ikonli
     */
    private void registerIkonHandler() {
        // Explicitar el registro de FontAwesome para asegurar que esté disponible
        try {
            // Forzar la carga de clases de FontAwesome
            Class.forName(FontAwesomeSolid.class.getName());
            System.out.println("FontAwesome Solid icons registered successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Error registering FontAwesome Solid icons: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
