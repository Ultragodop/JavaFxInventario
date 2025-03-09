package com.minimercado.javafxinventario.utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Utility class for managing application themes
 */
public class ThemeManager {
    
    private static final String DEFAULT_THEME = "/com/minimercado/javafxinventario/styles.css";
    private static final String DARK_THEME = "/com/minimercado/javafxinventario/dark-theme.css";
    
    private static final String PREF_KEY = "theme";
    private static final String LIGHT_THEME_NAME = "light";
    private static final String DARK_THEME_NAME = "dark";
    
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    /**
     * Applies the saved theme preference to a scene
     * @param scene The scene to apply the theme to
     */
    public static void applyTheme(Scene scene) {
        String themeName = prefs.get(PREF_KEY, LIGHT_THEME_NAME);
        
        if (DARK_THEME_NAME.equals(themeName)) {
            applyDarkTheme(scene);
        } else {
            applyLightTheme(scene);
        }
    }
    
    /**
     * Applies the light theme to a scene
     * @param scene The scene to apply the light theme to
     */
    public static void applyLightTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource(DEFAULT_THEME).toExternalForm());
        prefs.put(PREF_KEY, LIGHT_THEME_NAME);
    }
    
    /**
     * Applies the dark theme to a scene
     * @param scene The scene to apply the dark theme to
     */
    public static void applyDarkTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource(DARK_THEME).toExternalForm());
        prefs.put(PREF_KEY, DARK_THEME_NAME);
    }
    
    /**
     * Toggles between light and dark themes
     * @param scene The scene to toggle the theme for
     * @return The name of the new theme
     */
    public static String toggleTheme(Scene scene) {
        String currentTheme = prefs.get(PREF_KEY, LIGHT_THEME_NAME);
        
        if (LIGHT_THEME_NAME.equals(currentTheme)) {
            applyDarkTheme(scene);
            return DARK_THEME_NAME;
        } else {
            applyLightTheme(scene);
            return LIGHT_THEME_NAME;
        }
    }
    
    /**
     * Shows a theme selection dialog and applies the selected theme
     * @param scene The scene to apply the theme to
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
                applyLightTheme(scene);
            } else if (result.get() == darkButton) {
                applyDarkTheme(scene);
            }
        }
    }
    
    /**
     * Gets the current theme name
     * @return The current theme name
     */
    public static String getCurrentTheme() {
        return prefs.get(PREF_KEY, LIGHT_THEME_NAME);
    }
}
