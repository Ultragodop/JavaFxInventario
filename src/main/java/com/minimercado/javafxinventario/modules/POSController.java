package com.minimercado.javafxinventario.modules;

public class POSController {
    public POSController() { }
    
    public void processSale(String productId, int quantity, InventoryModule inventory, AccountingModule accounting) {
        inventory.getStockManager().updateStock(productId, -quantity);
        double saleAmount = quantity * inventory.getStockManager().getProduct(productId).getPrice(); ;
        Transaction tx = new Transaction("venta", saleAmount, "Venta: " + productId);
        accounting.recordTransaction(tx);
    }
    
}
