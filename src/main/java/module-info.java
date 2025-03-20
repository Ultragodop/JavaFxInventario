module JavaFxInventario {
    // JavaFX dependencies
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jdk.httpserver;
    requires spring.security.crypto;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.core;
    // Database connection
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.ooxml.schemas;
    
    // PDF generation
    requires itextpdf;
    
    // Excel export
    requires org.apache.poi.poi;
        
    // Other Java modules
    requires java.desktop;
    requires java.prefs;
    requires javafx.base;  // For ThemeManager's Preferences
    
    // Open packages to FXML
    opens com.minimercado.javafxinventario to javafx.fxml;
    opens com.minimercado.javafxinventario.controllers to javafx.fxml;
    opens com.minimercado.javafxinventario.modules to javafx.fxml;
    opens com.minimercado.javafxinventario.enums to javafx.fxml;
    opens com.minimercado.javafxinventario.DAO to javafx.fxml;
    opens com.minimercado.javafxinventario.utils to javafx.fxml;
    
    // Export our packages
    exports com.minimercado.javafxinventario;
    exports com.minimercado.javafxinventario.controllers;
    exports com.minimercado.javafxinventario.modules;
    exports com.minimercado.javafxinventario.enums;
    exports com.minimercado.javafxinventario.DAO;
    exports com.minimercado.javafxinventario.utils;
}
