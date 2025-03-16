package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;

/**
 * Representa un pago realizado a un empleado.
 */
public class EmployeePayment {
    private int id;
    private int employeeId;
    private LocalDate paymentDate;
    private double amount;
    private String paymentType; // Salario, Bono, Comisión, etc.
    private String paymentMethod; // Efectivo, Transferencia, etc.
    private String period; // Periodo al que corresponde el pago (ej: "Enero 2023")
    private String description;
    private String referenceNumber; // Número de referencia para transferencias
    private boolean reconciled; // Indica si está reconciliado con contabilidad

    public EmployeePayment() {
        this.paymentDate = LocalDate.now();
        this.reconciled = false;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }
}
