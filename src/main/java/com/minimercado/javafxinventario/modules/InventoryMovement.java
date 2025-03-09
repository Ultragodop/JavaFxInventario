package com.minimercado.javafxinventario.modules;

import java.util.Date;

/**
 * Represents a movement in inventory (addition, subtraction, adjustment)
 */
public class InventoryMovement {
    private int id;
    private String productId;
    private String productName; // Optional, for display purposes
    private String movementType; // PURCHASE, SALE, ADJUSTMENT, RETURN, INVENTORY_COUNT, etc.
    private int quantity; // Positive for additions, negative for subtractions
    private String reference; // Reference to source (e.g., order number, sale ID)
    private Date date;
    private String userId; // User who performed the movement
    
    /**
     * Default constructor
     */
    public InventoryMovement() {
        this.date = new Date();
    }
    
    /**
     * Constructor with basic movement information
     */
    public InventoryMovement(String productId, String movementType, int quantity, String reference) {
        this();
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reference = reference;
    }

    /**
     * Constructor with all fields
     */
    public InventoryMovement(String productId, String productName, String movementType, 
                            int quantity, String reference, String userId) {
        this(productId, movementType, quantity, reference);
        this.productName = productName;
        this.userId = userId;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getMovementType() {
        return movementType;
    }
    
    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Determines if this is an addition to inventory
     * @return true if movement adds to inventory
     */
    public boolean isAddition() {
        return quantity > 0;
    }
    
    /**
     * Determines if this is a subtraction from inventory
     * @return true if movement subtracts from inventory
     */
    public boolean isSubtraction() {
        return quantity < 0;
    }
    
    /**
     * Get absolute quantity value
     * @return absolute value of quantity
     */
    public int getAbsoluteQuantity() {
        return Math.abs(quantity);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %+d of %s on %s", 
            movementType, quantity, productId, date.toString());
    }
}
