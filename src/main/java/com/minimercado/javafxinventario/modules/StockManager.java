package com.minimercado.javafxinventario.modules;

import java.util.HashMap;
import java.util.Map;

public class StockManager {
    private Map<String,  Product> products = new HashMap<>();
    
    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }
    public void removeProduct(Product product) {
        products.remove(product.getId());
    }
    
    public void updateStockProduct( String stockString, Product product) {
        // Parseamos y seteamos el nuevo stock
        int newStock = Integer.parseInt(stockString);
        product.setStock(newStock);
     
   
    }
   public void updateThresholdProduct( String thresholdString, Product product) {
        // Parseamos y seteamos el nuevo stock
        int newThreshold = Integer.parseInt(thresholdString);
        product.setThreshold(newThreshold);
     
   
    }
    public void updatePriceProduct( String priceString, Product product) {
        // Parseamos y seteamos el nuevo stock
        double newPrice = Double.parseDouble(priceString);
        product.setPrice(newPrice);
     
   
    }
    public void updateNameProduct( String name, Product product) {
        // Parseamos y seteamos el nuevo stock
        product.setName(name);
     
   
    }
    public Product getProduct(String productId) {
        return products.get(productId);
    }


    public void updateStock(String productId, int quantity) {
        Product p = products.get(productId);
        if (p != null) {
            p.setStock(p.getStock() + quantity);
            if (p.getStock() < p.getThreshold()) {
                System.out.println("Alerta: Stock bajo para " + p.getId());
            }
        }
    }
}
