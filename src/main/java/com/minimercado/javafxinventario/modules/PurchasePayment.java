package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un pago realizado para una orden de compra.
 */
public class PurchasePayment {
    private int id;
    private int purchaseOrderId;
    private LocalDate paymentDate;
    private double amount;
    private double originalAmount;
    private String paymentMethod;
    private boolean isCompletePayment;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private boolean reconciled;

    // Constructor predeterminado
    public PurchasePayment() {
        this.paymentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.createdBy = "Sistema";
        this.reconciled = false;
    }

    // Constructor con parámetros principales
    public PurchasePayment(int purchaseOrderId, double amount, String paymentMethod) {
        this();
        this.purchaseOrderId = purchaseOrderId;
        this.amount = amount;
        this.originalAmount = amount;
        this.paymentMethod = paymentMethod;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(int purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isCompletePayment() {
        return isCompletePayment;
    }

    public void setCompletePayment(boolean completePayment) {
        isCompletePayment = completePayment;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
