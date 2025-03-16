package com.minimercado.javafxinventario.modules;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a purchase order in the inventory system
 */
public class PurchaseOrder {
    private int id;
    private int supplierId;
    private String supplierName; // For display purposes
    private Date orderDate;
    private Date expectedDate;
    private Date receivedDate;
    private String status; // PENDING, ORDERED, RECEIVED, CANCELED
    private double totalAmount;
    private String notes;
    private List<Item> items;
    
    /**
     * Default constructor
     */
    public PurchaseOrder() {
        this.orderDate = new Date();
        this.status = "PENDING";
        this.items = new ArrayList<>();
    }
    
    /**
     * Constructor with supplier info
     */
    public PurchaseOrder(int supplierId, String supplierName) {
        this();
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }
    
    /**
     * Adds an item to the purchase order
     * 
     * @param productId ID of the product
     * @param quantity Quantity to order
     * @param price Price per unit
     */
    public void addItem(String productId, int quantity, double price) {
        items.add(new Item(productId, quantity, price));
        calculateTotal();
    }
    
    /**
     * Removes an item from the purchase order
     * 
     * @param productId ID of the product to remove
     * @return true if item was removed, false if not found
     */
    public boolean removeItem(String productId) {
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (removed) {
            calculateTotal();
        }
        return removed;
    }
    
    /**
     * Calculates the total amount of the purchase order
     * based on the items and their quantities and prices
     */
    public void calculateTotal() {
        totalAmount = items.stream()
            .mapToDouble(Item::getSubtotal)
            .sum();
    }
    
    /**
     * Gets the total quantity of items in the order
     * 
     * @return Total quantity
     */
    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(Item::getQuantity)
            .sum();
    }
    
    /**
     * Gets the total number of distinct products in the order
     * 
     * @return Number of distinct products
     */
    public int getItemCount() {
        return items.size();
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public Date getExpectedDate() {
        return expectedDate;
    }
    
    public void setExpectedDate(Date expectedDate) {
        this.expectedDate = expectedDate;
    }
    
    public Date getReceivedDate() {
        return receivedDate;
    }
    
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public void setItems(List<Item> items) {
        this.items = items;
        calculateTotal();
    }

    public void setReceiveDate(Timestamp receiveDate) {
        this.receivedDate = receiveDate;
    }

    /**
     * Inner class representing an item in a purchase order
     */
    public static class Item {
        private String productId;
        private int quantity;
        private double price;
        private double subtotal;
        
        public Item(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = quantity * price;
        }
        
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = quantity * price;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
            this.subtotal = quantity * price;
        }
        
        public double getSubtotal() {
            return subtotal;
        }
    }
}
