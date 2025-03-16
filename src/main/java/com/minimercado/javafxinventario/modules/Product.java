package com.minimercado.javafxinventario.modules;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product in the inventory
 */
public class Product {
    private int id;
    private String barcode;
    private String name;
    private String description;
    private String category;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;
    private int reorderLevel;
    private double discount;
    private String supplier;
    private Date lastUpdated;
    // Added new fields
    private String location; // Physical location in store
    private Date expirationDate; // For perishable products
    private String sku; // Stock keeping unit - alternative ID
    private Date lastPurchaseDate;
    private boolean active;
    private int pendingOrderQuantity;
    private List<ProductSupplier> suppliers = new ArrayList<>();
    
    // Default constructor
    public Product() {
        this.discount = 0.0;
        this.lastUpdated = new Date();
        this.active = true;
        this.pendingOrderQuantity = 0;
    }
    
    // Parameterized constructor
    public Product(String barcode, String name, String description, String category, 
                  double purchasePrice, double sellingPrice, int stockQuantity, 
                  int reorderLevel, double discount, String supplier) {
        this.barcode = barcode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.discount = discount;
        this.supplier = supplier;
        this.lastUpdated = new Date();
        this.active = true;
        this.pendingOrderQuantity = 0;
    }
    
    // Extended constructor with new fields
    public Product(String barcode, String name, String description, String category, 
                  double purchasePrice, double sellingPrice, int stockQuantity, 
                  int reorderLevel, double discount, String supplier,
                  String location, Date expirationDate, String sku) {
        this(barcode, name, description, category, purchasePrice, sellingPrice, 
             stockQuantity, reorderLevel, discount, supplier);
        this.location = location;
        this.expirationDate = expirationDate;
        this.sku = sku;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public double getSellingPrice() {
        return sellingPrice;
    }
    
    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public int getReorderLevel() {
        return reorderLevel;
    }
    
    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    
    public String getSupplier() {
        ProductSupplier primarySupplier = getPrimarySupplier();
        return primarySupplier != null ? primarySupplier.getSupplierName() : supplier;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Date getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public Date getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(Date lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public int getPendingOrderQuantity() {
        return pendingOrderQuantity;
    }
    
    public void setPendingOrderQuantity(int pendingOrderQuantity) {
        this.pendingOrderQuantity = pendingOrderQuantity;
    }
    
    public List<ProductSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<ProductSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public void addSupplier(ProductSupplier supplier) {
        this.suppliers.add(supplier);
    }

    public ProductSupplier getPrimarySupplier() {
        return suppliers.stream()
                .filter(ProductSupplier::isPrimary)
                .findFirst()
                .orElse(suppliers.isEmpty() ? null : suppliers.get(0));
    }
    
    // Business methods
    
    /**
     * Checks if the product is low in stock
     * @return true if stock is below reorder level
     */
    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
    }
    
    /**
     * Calculates the profit margin percentage
     * @return profit margin as percentage
     */
    public double getProfitMargin() {
        if (purchasePrice == 0) return 0;
        return ((sellingPrice - purchasePrice) / purchasePrice) * 100;
    }
    
    /**
     * Calculates the final price after discount
     * @return price after discount
     */
    public double getFinalPrice() {
        return sellingPrice - discount;
    }
    
    /**
     * Checks if product is expired
     * @return true if product is expired
     */
    public boolean isExpired() {
        if (expirationDate == null) return false;
        return new Date().after(expirationDate);
    }
    
    /**
     * Calculates days until expiration
     * @return days until expiration, negative if expired
     */
    public long getDaysUntilExpiration() {
        if (expirationDate == null) return Integer.MAX_VALUE;
        long diff = expirationDate.getTime() - new Date().getTime();
        return diff / (24 * 60 * 60 * 1000);
    }
    
    /**
     * Calculates total stock (physical + on order)
     * @return total stock including pending orders
     */
    public int getTotalStockWithPending() {
        return stockQuantity + pendingOrderQuantity;
    }
    
    /**
     * Calculate how many units to order based on reorder level
     * @return quantity to order or 0 if no order needed
     */
    public int calculateOrderQuantity() {
        int deficit = reorderLevel - (stockQuantity + pendingOrderQuantity);
        return Math.max(0, deficit);
    }
    
    @Override
    public String toString() {
        return name + " (" + barcode + ")";
    }
}
