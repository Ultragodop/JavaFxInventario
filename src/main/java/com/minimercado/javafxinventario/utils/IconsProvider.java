package com.minimercado.javafxinventario.utils;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Utilidad para proporcionar iconos para la aplicación
 */
public class IconsProvider {
    
    /**
     * Crea un FontIcon de FontAwesome Solid
     * @param icon Constante de icono de FontAwesomeSolid
     * @param size Tamaño del icono
     * @return FontIcon configurado
     */
    public static FontIcon createFontAwesomeIcon(FontAwesomeSolid icon, int size) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(size);
        return fontIcon;
    }
    
    /**
     * Crea un FontIcon de FontAwesome Solid con color personalizado
     * @param icon Constante de icono de FontAwesomeSolid
     * @param size Tamaño del icono
     * @param color Color del icono
     * @return FontIcon configurado
     */
    public static FontIcon createFontAwesomeIcon(FontAwesomeSolid icon, int size, Color color) {
        FontIcon fontIcon = createFontAwesomeIcon(icon, size);
        fontIcon.setIconColor(color);
        return fontIcon;
    }
    
    /**
     * Crea un FontIcon a partir de un literal de icono
     * @param iconLiteral Literal del icono (ej: "fas-store")
     * @param size Tamaño del icono
     * @return FontIcon configurado o null si hay error
     */
    public static FontIcon createIconFromLiteral(String iconLiteral, int size) {
        try {
            FontIcon fontIcon = new FontIcon(iconLiteral);
            fontIcon.setIconSize(size);
            return fontIcon;
        } catch (Exception e) {
            System.err.println("Error creating icon from literal '" + iconLiteral + "': " + e.getMessage());
            // Fallback to a default icon
            return new FontIcon(FontAwesomeSolid.QUESTION_CIRCLE);
        }
    }
}
