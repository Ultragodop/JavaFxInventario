package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.enums.PaymentMethod;
import java.util.HashMap;
import java.util.Map;

/**
 * Module for handling payment processing in the sales system.
 * Works with TransactionModule to record payment transactions.
 */
public class PaymentModule {
    
    private final Map<String, Double> paymentLimits;
    private final TransactionModule transactionModule;
    
    public PaymentModule() {
        paymentLimits = new HashMap<>();
        // Default payment limits
        paymentLimits.put(PaymentMethod.EFECTIVO.getDisplayName(), 10000.0);
        paymentLimits.put(PaymentMethod.TARJETA.getDisplayName(), 50000.0);
        paymentLimits.put(PaymentMethod.TRANSFERENCIA.getDisplayName(), 100000.0);
        paymentLimits.put(PaymentMethod.BILLETERA_DIGITAL.getDisplayName(), 20000.0);
        
        transactionModule = TransactionModule.getInstance();
    }
    
    /**
     * Process a payment with the specified method
     * 
     * @param method The payment method to use
     * @param amount The payment amount
     * @param reference Optional reference information for the payment
     * @return true if payment was successful, false otherwise
     */
    public boolean processPayment(PaymentMethod method, double amount, String reference) {
        // Check payment limits
        Double limit = paymentLimits.get(method.getDisplayName());
        if (limit != null && amount > limit) {
            System.err.println("Payment exceeds limit for method: " + method.getDisplayName());
            return false;
        }
        
        // Different processing logic based on payment method
        switch (method) {
            case EFECTIVO:
                // Cash payments are processed immediately
                return true;
                
            case TARJETA:
                // Process card payment (in real implementation would call payment gateway)
                return processCardPayment(amount, reference);
                
            case TRANSFERENCIA:
                // Process bank transfer
                return processBankTransfer(amount, reference);
                
            case BILLETERA_DIGITAL:
                // Process digital wallet payment
                return processDigitalWalletPayment(amount, reference);
                
            case PAGO_MIXTO:
                // Mixed payments need special handling
                return processMixedPayment(amount, reference);
                
            default:
                System.err.println("Unknown payment method: " + method);
                return false;
        }
    }
    
    // Mock implementations of payment processing methods
    private boolean processCardPayment(double amount, String reference) {
        // In a real implementation, this would call a payment gateway API
        System.out.println("Processing card payment: $" + amount + " with reference: " + reference);
        return true;
    }
    
    private boolean processBankTransfer(double amount, String reference) {
        System.out.println("Processing bank transfer: $" + amount + " with reference: " + reference);
        return true;
    }
    
    private boolean processDigitalWalletPayment(double amount, String reference) {
        System.out.println("Processing digital wallet payment: $" + amount + " with reference: " + reference);
        return true;
    }
    
    private boolean processMixedPayment(double amount, String reference) {
        System.out.println("Processing mixed payment: $" + amount + " with reference: " + reference);
        // In a real implementation, would split and process each payment method separately
        return true;
    }
    
    /**
     * Set a payment limit for a specific method
     * 
     * @param method The payment method
     * @param limit The maximum amount allowed
     */
    public void setPaymentLimit(PaymentMethod method, double limit) {
        paymentLimits.put(method.getDisplayName(), limit);
    }
    
    /**
     * Get the current payment limit for a method
     * 
     * @param method The payment method
     * @return The current limit or null if no limit is set
     */
    public Double getPaymentLimit(PaymentMethod method) {
        return paymentLimits.get(method.getDisplayName());
    }
}
