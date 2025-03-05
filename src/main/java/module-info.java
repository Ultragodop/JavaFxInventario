module com.minimercado.javafxinventario {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql; // ...nueva dependencia para DB...
    requires jdk.httpserver;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires spring.security.crypto;

    opens com.minimercado.javafxinventario to javafx.fxml;
    opens com.minimercado.javafxinventario.controllers to javafx.fxml;
    exports com.minimercado.javafxinventario;
    exports com.minimercado.javafxinventario.controllers;
    exports com.minimercado.javafxinventario.modules;
}
