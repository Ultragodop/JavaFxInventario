package com.minimercado.javafxinventario.modules;

import javafx.collections.ObservableList;

/**
 * Module that manages product inventory functionality.
 */
public class InventoryModule {
    private static InventoryModule instance;
    private final StockManager stockManager;
    
    /**
     * Constructor initializes the StockManager.
     */
    public InventoryModule() {
        stockManager = new StockManager();
    }
    
    /**
     * Gets singleton instance of the InventoryModule.
     * @return The InventoryModule instance
     */
    public static synchronized InventoryModule getInstance() {
        if (instance == null) {
            instance = new InventoryModule();
        }
        return instance;
    }
    
    /**
     * Gets the StockManager instance.
     * @return The StockManager
     */
    public StockManager getStockManager() {
        return stockManager;
    }
    
    /**
     * Gets all products in inventory.
     * @return Observable list of all products
     */
    public ObservableList<Product> getAllProducts() {
        return stockManager.getAllProducts();
    }
    
    /**
     * Gets products that are low in stock.
     * @return Observable list of low stock products
     */
    public ObservableList<Product> getLowStockProducts() {
        return stockManager.getLowStockProducts();
    }
    
    /**
     * Updates stock quantity for a product.
     * @param barcode The product barcode
     * @param quantityChange The change in quantity (positive to add, negative to subtract)
     * @return true if update was successful
     */
    public boolean updateStock(String barcode, int quantityChange) {
        return stockManager.updateStock(barcode, quantityChange);
    }
    
    /**
     * Adds a new product to inventory.
     * @param product The product to add
     * @return true if addition was successful
     */
    public boolean addProduct(Product product) {
        return stockManager.addProduct(product);
    }
    
    /**
     * Updates product information.
     * @param product The product with updated information
     * @return true if update was successful
     */
    public boolean updateProduct(Product product) {
        return stockManager.updateProduct(product);
    }
    
    /**
     * Removes a product from inventory.
     * @param barcode The barcode of the product to remove
     * @return true if removal was successful
     */
    public boolean removeProduct(String barcode) {
        return stockManager.removeProduct(barcode);
    }
    
    /**
     * Refreshes inventory data from the database.
     */
    public void refreshInventory() {
        stockManager.refreshProducts();
    }
    
    /**
     * Searches for products by name or barcode.
     * @param query The search term
     * @return Observable list of matching products
     */
    public ObservableList<Product> searchProducts(String query) {
        return stockManager.searchProducts(query);
    }
}
