package com.minimercado.javafxinventario.modules;

/**
 * Represents a payment method used in transactions
 */
public class PaymentMethod {
    private String code;
    private String displayName;
    private String reference;
    
    public PaymentMethod(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
        this.reference = "";
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
