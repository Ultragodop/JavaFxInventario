package com.minimercado.javafxinventario.modules;

/**
 * Representa una categoría de gastos en el sistema.
 */
public class ExpenseCategory {
    private int id;
    private String name;
    private String description;
    private String accountCode;
    private boolean active = true;

    public ExpenseCategory() {
        // Constructor por defecto
    }

    public ExpenseCategory(String name, String description, String accountCode) {
        this.name = name;
        this.description = description;
        this.accountCode = accountCode;
        this.active = true;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name;
    }
}
