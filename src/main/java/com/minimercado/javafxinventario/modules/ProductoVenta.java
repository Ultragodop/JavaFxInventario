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

    /**
     * Actualiza el precio unitario y recalcula el total
     * @param precioUnitario Nuevo precio unitario
     */
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario.set(precioUnitario);
        // Update total when price changes
        total.set(calcularTotal());
    }

    /**
     * Actualiza el descuento y recalcula el total
     * @param descuento Nuevo valor de descuento
     */
    public void setDescuento(double descuento) {
        this.descuento.set(descuento);
        // Update total when discount changes
        total.set(calcularTotal());
    }

    /**
     * Crea una copia del producto de venta
     * @return Una nueva instancia con los mismos valores
     */
    public ProductoVenta copy() {
        return new ProductoVenta(
            getCodigo(),
            getNombre(),
            getCantidad(),
            getPrecioUnitario(),
            getDescuento()
        );
    }

    /**
     * Aumenta la cantidad en 1
     */
    public void aumentarCantidad() {
        setCantidad(getCantidad() + 1);
    }

    /**
     * Disminuye la cantidad en 1 si es mayor que 1
     * @return true si la cantidad fue disminuida, false si ya estaba en 1
     */
    public boolean disminuirCantidad() {
        if (getCantidad() > 1) {
            setCantidad(getCantidad() - 1);
            return true;
        }
        return false;
    }
}
