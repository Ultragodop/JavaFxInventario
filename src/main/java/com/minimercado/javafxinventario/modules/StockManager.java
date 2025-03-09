package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.DAO.InventoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages inventory stock levels and product data.
 */
public class StockManager {
    private final Map<String, Product> productMap;
    private final ObservableList<Product> productList;
    private final InventoryDAO inventoryDAO;

    /**
     * Constructor initializes the product collections and loads products from database.
     */
    public StockManager() {
        productMap = new HashMap<>();
        productList = FXCollections.observableArrayList();
        inventoryDAO = new InventoryDAO();
        
        // Load initial products from database
        loadProductsFromDatabase();
    }

    /**
     * Loads all products from the database into the local collections
     */
    private void loadProductsFromDatabase() {
        try {
            List<Product> products = inventoryDAO.getAllProducts();
            
            for (Product product : products) {
                // Use barcode as key for the map (String type)
                productMap.put(product.getBarcode(), product);
            }
            
            productList.setAll(products);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a product by its barcode
     * @param barcode The product barcode to search for
     * @return The product if found, null otherwise
     */
    public Product getProduct(String barcode) {
        // First check our local cache
        if (productMap.containsKey(barcode)) {
            return productMap.get(barcode);
        }
        
        // If not found locally, try the database
        Product product = inventoryDAO.getProductByBarcode(barcode);
        if (product != null) {
            // Add to our local cache
            productMap.put(barcode, product);
            productList.add(product);
        }
        
        return product;
    }

    /**
     * Updates the stock level for a product
     * @param barcode The product barcode
     * @param quantityChange The quantity to add (positive) or subtract (negative)
     * @return true if update was successful, false otherwise
     */
    public boolean updateStock(String barcode, int quantityChange) {
        Product product = getProduct(barcode);
        if (product == null) {
            return false;
        }
        
        // Update the stock and save to database
        int newStock = product.getStockQuantity() + quantityChange;
        product.setStockQuantity(newStock);
        
        // Update in database
        boolean updated = inventoryDAO.updateProductStock(barcode, quantityChange);
        
        return updated;
    }

    /**
     * Adds a new product to inventory
     * @param product The product to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProduct(Product product) {
        if (product == null || productMap.containsKey(product.getBarcode())) {
            return false;
        }
        
        // Add to database
        boolean added = inventoryDAO.addProduct(product);
        
        if (added) {
            // Add to local collections if successful
            productMap.put(product.getBarcode(), product);
            productList.add(product);
        }
        
        return added;
    }

    /**
     * Updates product information
     * @param product The product with updated information
     * @return true if update was successful, false otherwise
     */
    public boolean updateProduct(Product product) {
        if (product == null || !productMap.containsKey(product.getBarcode())) {
            return false;
        }
        
        // Update in database
        boolean updated = inventoryDAO.updateProduct(product);
        
        if (updated) {
            // Update in local collections
            productMap.put(product.getBarcode(), product);
            
            // Update in the observable list
            for (int i = 0; i < productList.size(); i++) {
                if (productList.get(i).getBarcode().equals(product.getBarcode())) {
                    productList.set(i, product);
                    break;
                }
            }
        }
        
        return updated;
    }

    /**
     * Removes a product from inventory
     * @param barcode The barcode of the product to remove
     * @return true if removal was successful, false otherwise
     */
    public boolean removeProduct(String barcode) {
        Product product = getProduct(barcode);
        if (product == null) {
            return false;
        }
        
        // Remove from database
        boolean removed = inventoryDAO.deleteProduct(barcode);
        
        if (removed) {
            // Remove from local collections
            productMap.remove(barcode);
            productList.remove(product);
        }
        
        return removed;
    }

    /**
     * Gets all products as an observable list
     * @return Observable list of all products
     */
    public ObservableList<Product> getAllProducts() {
        return productList;
    }

    /**
     * Gets products that are below their reorder level
     * @return Observable list of products below reorder level
     */
    public ObservableList<Product> getLowStockProducts() {
        ObservableList<Product> lowStock = FXCollections.observableArrayList();
        
        for (Product product : productList) {
            if (product.getStockQuantity() <= product.getReorderLevel()) {
                lowStock.add(product);
            }
        }
        
        return lowStock;
    }

    /**
     * Refreshes the product data from the database
     */
    public void refreshProducts() {
        loadProductsFromDatabase();
    }

    /**
     * Searches for products by name or barcode
     * @param query The search term
     * @return Observable list of matching products
     */
    public ObservableList<Product> searchProducts(String query) {
        if (query == null || query.isEmpty()) {
            return productList;
        }
        
        ObservableList<Product> results = FXCollections.observableArrayList();
        String lowerQuery = query.toLowerCase();
        
        for (Product product : productList) {
            if (product.getBarcode().toLowerCase().contains(lowerQuery) || 
                product.getName().toLowerCase().contains(lowerQuery)) {
                results.add(product);
            }
        }
        
        return results;
    }
}
