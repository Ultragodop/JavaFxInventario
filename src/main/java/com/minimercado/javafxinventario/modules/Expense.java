package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un gasto general del negocio.
 */
public class Expense {
    private int id;
    private int categoryId;
    private String categoryName; // Para facilitar la visualización
    private double amount;
    private LocalDate expenseDate;
    private String description;
    private String paymentMethod; // Efectivo, Transferencia, etc.
    private String receiptNumber; // Número de factura o recibo
    private String supplier; // Proveedor o beneficiario 
    private String receiptImagePath; // Ruta al comprobante digitalizado
    private LocalDateTime createdAt;
    private String createdBy;
    private boolean reconciled; // Si fue conciliado con la contabilidad
    private String notes;

    public Expense() {
        this.expenseDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public Expense(int categoryId, double amount, LocalDate expenseDate, String description, String paymentMethod) {
        this();
        this.categoryId = categoryId;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getReceiptImagePath() {
        return receiptImagePath;
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
        return createdBy;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
