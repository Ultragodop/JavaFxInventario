package com.minimercado.javafxinventario.modules;

import java.sql.Timestamp;

/**
 * Representa la relación entre un producto y un proveedor
 */
public class ProductSupplier {
    private int id;
    private String productBarcode;
    private int supplierId;
    private String supplierName; // Para mostrar en UI
    private boolean isPrimary;
    private double purchasePrice;
    private Timestamp lastPurchaseDate;
    
    // Constructor
    public ProductSupplier() {
    }
    
    public ProductSupplier(String productBarcode, int supplierId, boolean isPrimary, double purchasePrice) {
        this.productBarcode = productBarcode;
        this.supplierId = supplierId;
        this.isPrimary = isPrimary;
        this.purchasePrice = purchasePrice;
    }
    
    // Getters y setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getProductBarcode() {
        return productBarcode;
    }
    
    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }
    
    public int getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    public String getSupplierName() {
        return supplierName;
    }
    
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    public double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public Timestamp getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(Timestamp lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
}
