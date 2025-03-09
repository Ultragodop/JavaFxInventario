package com.minimercado.javafxinventario.modules;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProductoVenta {
    private final SimpleStringProperty codigo;
    private final SimpleStringProperty nombre;
    private final SimpleIntegerProperty cantidad;
    private final SimpleDoubleProperty precioUnitario;
    private final SimpleDoubleProperty descuento;
    private final SimpleDoubleProperty total;

    public ProductoVenta(String codigo, String nombre, int cantidad, double precioUnitario, double descuento) {
        this.codigo = new SimpleStringProperty(codigo);
        this.nombre = new SimpleStringProperty(nombre);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
        this.descuento = new SimpleDoubleProperty(descuento);
        this.total = new SimpleDoubleProperty(calcularTotal());
    }

    public String getCodigo() {
        return codigo.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public void setCantidad(int cantidad) {
        this.cantidad.set(cantidad);
        // Update total when quantity changes
        total.set(calcularTotal());
    }

    public double getPrecioUnitario() {
        return precioUnitario.get();
    }

    public double getDescuento() {
        return descuento.get();
    }

    public double getTotal() {
        return total.get();
    }

    public double getDescuentoTotal() {
        return descuento.get() * cantidad.get();
    }

    private double calcularTotal() {
        return (precioUnitario.get() - descuento.get()) * cantidad.get();
    }

    // Property getters for TableView binding
    public SimpleStringProperty codigoProperty() {
        return codigo;
    }

    public SimpleStringProperty nombreProperty() {
        return nombre;
    }

    public SimpleIntegerProperty cantidadProperty() {
        return cantidad;
    }

    public SimpleDoubleProperty precioUnitarioProperty() {
        return precioUnitario;
    }

    public SimpleDoubleProperty descuentoProperty() {
        return descuento;
    }

    public SimpleDoubleProperty totalProperty() {
        return total;
    }
}
