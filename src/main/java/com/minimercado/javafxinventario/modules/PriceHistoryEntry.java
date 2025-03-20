package com.minimercado.javafxinventario.modules;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un registro en el historial de precios de un producto.
 */
public class PriceHistoryEntry {
    public int id;
    public int productId;
    public double price;
    public LocalDateTime changeDate;
    public String changedBy;
    public String notes;
    public boolean active = true;

    public PriceHistoryEntry() {
        this.changeDate = LocalDateTime.now();
    }

    public PriceHistoryEntry(int productId, double price) {
        this();
        this.productId = productId;
        this.price = price;
    }

    public PriceHistoryEntry(int productId, double price, String changedBy) {
        this(productId, price);
        this.changedBy = changedBy;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Precio: " + price + " - Fecha: " + changeDate;
    }

    /**
     * Obtiene la fecha de cambio formateada para su visualización
     * @return Cadena con la fecha formateada
     */
    public String getFormattedChangeDate() {
        if (changeDate == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return changeDate.format(formatter);
    }
    
    /**
     * Obtiene el precio formateado como cadena con dos decimales
     * @return Precio formateado
     */
    public String getFormattedPrice() {
        return String.format("%.2f", price);
    }
    
    /**
     * Obtiene un arreglo de objetos con los datos para exportar a Excel
     * @return Arreglo con datos exportables
     */
    public Object[] toExcelRow() {
        return new Object[] {
            id,
            productId,
            price,
            getFormattedPrice(),
            changeDate,
            getFormattedChangeDate(),
            changedBy,
            notes,
            active ? "Activo" : "Inactivo"
        };
    }
    
    /**
     * Obtiene los encabezados para exportación a Excel
     * @return Lista de encabezados
     */
    public static List<String> getExcelHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("ID Producto");
        headers.add("Precio");
        headers.add("Precio Formateado");
        headers.add("Fecha de Cambio");
        headers.add("Fecha Formateada");
        headers.add("Modificado Por");
        headers.add("Notas");
        headers.add("Estado");
        return headers;
    }
    
    /**
     * Prepara un mapa de datos para exportación
     * @return Lista de valores para exportar
     */
    public List<Object> getExportableData() {
        List<Object> data = new ArrayList<>();
        data.add(id);
        data.add(productId);
        data.add(price);
        data.add(changeDate);
        data.add(changedBy);
        data.add(notes);
        data.add(active);
        return data;
    }
}
