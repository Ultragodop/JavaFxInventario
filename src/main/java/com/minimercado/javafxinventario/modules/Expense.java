package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an expense in the system
 */
public class Expense {
    // Fields for ID support with both int and String types
    private int intId;
    private String id;
    
    // Category information - consolidado
    private int categoryId;
    private String category; // Nombre de la categoría
    
    private double amount;
    private String description;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String receiptNumber;
    private String accountCode;
    
    // Supplier/vendor fields - consolidado
    private String vendor; // Campo unificado para proveedor
    
    // Additional fields
    private String receiptImagePath;
    private LocalDateTime createdAt;
    private String createdBy;
    private boolean reconciled;
    private boolean isTaxDeductible;
    private String status;
    private String notes;
    
    /**
     * Default constructor
     */
    public Expense() {
        this.id = UUID.randomUUID().toString();
        this.expenseDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    /**
     * Constructor with essential fields
     */
    public Expense(String category, double amount, String description, LocalDate expenseDate, 
                  String paymentMethod, String accountCode) {
        this();
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.paymentMethod = paymentMethod;
        this.accountCode = accountCode;
    }
    
    // Getters and Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setId(int id) {
        this.intId = id;
        this.id = String.valueOf(id);
    }
    
    public int getIntId() {
        return intId;
    }
    
    /**
     * Obtiene el nombre de la categoría
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Establece el nombre de la categoría
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Obtiene el ID de la categoría
     */
    public int getCategoryId() {
        return categoryId;
    }
    
    /**
     * Establece el ID de la categoría
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * Obtiene el nombre de la categoría (alias para compatibilidad)
     * @deprecated Use getCategory() instead
     */
    @Deprecated
    public String getCategoryName() {
        return category;
    }
    
    /**
     * Establece el nombre de la categoría (alias para compatibilidad)
     * @deprecated Use setCategory() instead
     */
    @Deprecated
    public void setCategoryName(String categoryName) {
        this.category = categoryName;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getExpenseDate() {
        return expenseDate;
    }
    
    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getReceiptNumber() {
        return receiptNumber;
    }
    
    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
    
    public String getAccountCode() {
        return accountCode;
    }
    
    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }
    
    /**
     * Obtiene el nombre del proveedor
     */
    public String getVendor() {
        return vendor;
    }
    
    /**
     * Establece el nombre del proveedor
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    /**
     * Obtiene el nombre del proveedor (alias para compatibilidad)
     * @deprecated Use getVendor() instead
     */
    @Deprecated
    public String getVendorName() {
        return vendor;
    }
    
    /**
     * Establece el nombre del proveedor (alias para compatibilidad)
     * @deprecated Use setVendor() instead
     */
    @Deprecated
    public void setVendorName(String vendorName) {
        this.vendor = vendorName;
    }
    
    /**
     * Obtiene el nombre del proveedor (alias para compatibilidad)
     * @deprecated Use getVendor() instead
     */
    @Deprecated
    public String getSupplier() {
        return vendor;
    }
    
    /**
     * Establece el nombre del proveedor (alias para compatibilidad)
     * @deprecated Use setVendor() instead
     */
    @Deprecated
    public void setSupplier(String supplier) {
        this.vendor = supplier;
    }
    
    public String getReceiptImagePath() {
        return receiptImagePath != null ? receiptImagePath : "";
    }
    
    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy != null ? createdBy : "System";
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public boolean isReconciled() {
        return reconciled;
    }
    
    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }
    
    public boolean isTaxDeductible() {
        return isTaxDeductible;
    }
    
    public void setTaxDeductible(boolean taxDeductible) {
        this.isTaxDeductible = taxDeductible;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Get the integer ID of the expense
     * This handles conversion between String ID and int ID
     */
    public int getId() {
        if (this.intId > 0) {
            return this.intId;
        }
        try {
            return Integer.parseInt(this.id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // The existing getter for String ID should be renamed to avoid conflicts
    public String getStringId() {
        return id;
    }
    
    /**
     * Create a Transaction object from this expense
     * This allows the expense to be integrated with the accounting system
     */
    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setType("gasto");
        transaction.setAmount(-this.amount); // Negative amount for expense
        
        String transactionDesc = this.category + " - " + this.description;
        
        if (this.vendor != null && !this.vendor.isEmpty()) {
            transactionDesc += " - " + this.vendor;
        }
        
        if (this.receiptNumber != null && !this.receiptNumber.isEmpty()) {
            transactionDesc += " - Recibo: " + this.receiptNumber;
        }
        
        transaction.setDescription(transactionDesc);
        transaction.setTimestamp(this.expenseDate.atStartOfDay());
        transaction.setAdditionalInfo(this.notes);
        transaction.setPaymentMethod(this.paymentMethod);
        
        return transaction;
    }
}
