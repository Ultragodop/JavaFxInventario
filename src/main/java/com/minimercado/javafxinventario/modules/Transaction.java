package com.minimercado.javafxinventario.modules;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private String id;
    private String type;
    private double amount;
    private double taxAmount;
    private String description;
    private LocalDateTime timestamp;
    private boolean reversed;
    private String reversalReason;
    
    public Transaction(String type, double amount, String description) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.taxAmount = 0.0;
        this.reversed = false;
    }
    
    // Constructor para transacciones históricas con timestamp específico
    public Transaction(String type, double amount, String description, LocalDateTime timestamp) {
        this(type, amount, description);
        this.timestamp = timestamp;
    }
    
    public String getId() { return id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public boolean isReversed() { return reversed; }
    public void setReversed(boolean reversed) { this.reversed = reversed; }
    public String getReversalReason() { return reversalReason; }
    public void setReversalReason(String reversalReason) { this.reversalReason = reversalReason; }
    
    /**
     * Sets the ID of this transaction.
     * Used when recreating transactions from database records.
     *
     * @param id The ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Transaction{" +
            "id='" + id + '\'' +
            ", type='" + type + '\'' +
            ", amount=" + amount +
            ", description='" + description + '\'' +
            ", timestamp=" + timestamp +
            ", reversed=" + reversed +
            '}';
    }

}
