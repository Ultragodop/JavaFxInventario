package com.minimercado.javafxinventario.enums;

/**
 * Enum representing different payment methods available in the sales system.
 * This enum is used to standardize payment method handling across the application.
 */
public enum PaymentMethod {
    EFECTIVO("Efectivo", "Pago en efectivo"),
    TARJETA("Tarjeta", "Pago con tarjeta de crédito/débito"),
    TRANSFERENCIA("Transferencia", "Transferencia bancaria"),
    BILLETERA_DIGITAL("Billetera digital", "Pago con billetera o aplicación digital"),
    PAGO_MIXTO("Pago mixto", "Combinación de varios métodos de pago");

    private final String displayName;
    private final String description;

    /**
     * Constructor for PaymentMethod enum
     * 
     * @param displayName The user-friendly name to be displayed in the UI
     * @param description A longer description of the payment method
     */
    PaymentMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the user-friendly display name of the payment method
     * 
     * @return The display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description of the payment method
     * 
     * @return The description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Find a payment method by its display name
     * 
     * @param displayName The display name to search for
     * @return The matching PaymentMethod or null if not found
     */
    public static PaymentMethod findByDisplayName(String displayName) {
        for (PaymentMethod method : values()) {
            if (method.getDisplayName().equals(displayName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Checks if this payment method requires validation of a reference number
     * 
     * @return true if reference validation is required, false otherwise
     */
    public boolean requiresReferenceValidation() {
        return this == TARJETA || this == TRANSFERENCIA || this == BILLETERA_DIGITAL;
    }

    /**
     * Checks if this payment method can provide change to the customer
     * 
     * @return true if change can be given, false otherwise
     */
    public boolean canProvideChange() {
        return this == EFECTIVO;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
