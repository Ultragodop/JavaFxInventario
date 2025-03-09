package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.enums.PaymentMethod;

public class POSController {
    public POSController() { }
    
    public void processSale(String productId, int quantity, InventoryModule inventory, AccountingModule accounting) {
        inventory.getStockManager().updateStock(productId, -quantity);
        double saleAmount = quantity * inventory.getStockManager().getProduct(productId).getSellingPrice();
        Transaction tx = new Transaction("venta", saleAmount, "Venta: " + productId);
        accounting.recordTransaction(tx);
    }
    
    /**
     * Process a sale with payment method information
     * 
     * @param productId Product identifier
     * @param quantity Number of items sold
     * @param inventory Inventory module reference
     * @param accounting Accounting module reference
     * @param paymentMethod Payment method used
     */
    public void processSale(String productId, int quantity, InventoryModule inventory, 
                           AccountingModule accounting, PaymentMethod paymentMethod) {
        inventory.getStockManager().updateStock(productId, -quantity);
        double saleAmount = quantity * inventory.getStockManager().getProduct(productId).getSellingPrice();
        
        Transaction tx = new Transaction(
            "venta", 
            saleAmount, 
            "Venta: " + productId + " - Método: " + paymentMethod.getDisplayName()
        );
        
        accounting.recordTransaction(tx);
    }
}
