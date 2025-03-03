package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.InventoryModule;
import com.minimercado.javafxinventario.modules.StockManager;
import com.minimercado.javafxinventario.modules.Product;

public class InventoryController {
    private InventoryModule inventoryModule = new InventoryModule();
    
    public StockManager getStockManager() {
        return inventoryModule.getStockManager();
    }
    
    public void addProduct(Product product) {
        inventoryModule.getStockManager().addProduct(product);
    }
    
    public Product getProduct(String id) {
        return inventoryModule.getStockManager().getProduct(id);
    }
    
    // ...otros métodos de control...
}
