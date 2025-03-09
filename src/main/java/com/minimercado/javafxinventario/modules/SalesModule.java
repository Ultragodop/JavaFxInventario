package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.enums.PaymentMethod;

public class SalesModule {
    private POSController posController;
    private PaymentModule paymentModule;

    public SalesModule() {
        posController = new POSController();
        paymentModule = new PaymentModule();
    }
    
    public POSController getPosController() {
        return posController;
    }

    public PaymentModule getPaymentModule() {
        return paymentModule;
    }
    
    /**
     * Process a sale with specified payment method
     * 
     * @param productId Product ID
     * @param quantity Quantity sold
     * @param method Payment method
     * @return true if sale completed successfully, false otherwise
     */
    public boolean processSaleWithPayment(String productId, int quantity, PaymentMethod method) {
        InventoryModule inventory = InventoryModule.getInstance();
        AccountingModule accounting = AccountingModule.getInstance();
        
        // Process the payment first
        double amount = quantity * inventory.getStockManager().getProduct(productId).getSellingPrice();
        if (paymentModule.processPayment(method, amount, null)) {
            // If payment successful, process the sale
            posController.processSale(productId, quantity, inventory, accounting, method);
            return true;
        }
        return false;
    }
}
