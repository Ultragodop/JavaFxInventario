package com.minimercado.javafxinventario.modules;

public class InventoryModule {
    private StockManager stockManager;
    
    public InventoryModule() {
        stockManager = new StockManager();
    }
    
    public StockManager getStockManager() {
        return stockManager;
    }
}
