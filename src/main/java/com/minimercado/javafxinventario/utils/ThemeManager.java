package com.minimercado.javafxinventario.utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Gestiona los temas de la aplicación
 */
public class ThemeManager {
    
    private static final String LIGHT_THEME = "/com/minimercado/javafxinventario/theme-light.css";
    private static final String DARK_THEME = "/com/minimercado/javafxinventario/theme-dark.css";
    private static final String BASE_STYLES = "/com/minimercado/javafxinventario/styles.css";
    
    private static final String PREF_KEY = "theme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    /**
     * Establece el tema de la aplicación
     * @param scene La escena a la que aplicar el tema
     * @param themeName Nombre del tema ("light" o "dark")
     */
    public static void setTheme(Scene scene, String themeName) {
        // Elimina temas anteriores
        scene.getStylesheets().remove(LIGHT_THEME);
        scene.getStylesheets().remove(DARK_THEME);
        
        // Asegura que los estilos base estén cargados
        if (!scene.getStylesheets().contains(BASE_STYLES)) {
            scene.getStylesheets().add(BASE_STYLES);
        }
        
        // Aplica el tema seleccionado
        if ("dark".equalsIgnoreCase(themeName)) {
            scene.getStylesheets().add(DARK_THEME);
        } else {
            scene.getStylesheets().add(LIGHT_THEME);
        }
        
        prefs.put(PREF_KEY, themeName);
    }
    
    /**
     * Cambia entre temas claro y oscuro
     * @param scene La escena a la que aplicar el tema
     * @return El nombre del nuevo tema aplicado
     */
    public static String toggleTheme(Scene scene) {
        boolean isDarkTheme = scene.getStylesheets().contains(DARK_THEME);
        setTheme(scene, isDarkTheme ? "light" : "dark");
        return isDarkTheme ? "light" : "dark";
    }
    
    /**
     * Aplica el tema predeterminado (claro)
     * @param scene La escena a la que aplicar el tema
     */
    public static void applyDefaultTheme(Scene scene) {
        setTheme(scene, "light");
    }
    
    /**
     * Aplica el tema guardado en las preferencias a una escena
     * @param scene La escena a la que aplicar el tema
     */
    public static void applyTheme(Scene scene) {
        String themeName = prefs.get(PREF_KEY, "light");
        setTheme(scene, themeName);
    }
    
    /**
     * Muestra un diálogo de selección de tema y aplica el tema seleccionado
     * @param scene La escena a la que aplicar el tema
     */
    public static void showThemeDialog(Scene scene) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Theme Selection");
        alert.setHeaderText("Choose Application Theme");
        alert.setContentText("Select your preferred theme:");
        
        ButtonType lightButton = new ButtonType("Light Theme");
        ButtonType darkButton = new ButtonType("Dark Theme");
        
        alert.getButtonTypes().setAll(lightButton, darkButton, ButtonType.CANCEL);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == lightButton) {
                setTheme(scene, "light");
            } else if (result.get() == darkButton) {
                setTheme(scene, "dark");
            }
        }
    }
    
    /**
     * Obtiene el nombre del tema actual
     * @return El nombre del tema actual
     */
    public static String getCurrentTheme() {
        return prefs.get(PREF_KEY, "light");
    }
}
