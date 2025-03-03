package com.minimercado.javafxinventario.modules;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryReportingModule {
    private final String url = "jdbc:mysql://localhost:3306/inventory_db";
    private final String user = "root";
    private final String password = "your_password";
    
    public List<InventoryStats> getMissingProducts() {
        List<InventoryStats> stats = new ArrayList<>();
        String sql = "SELECT id, name, stock, threshold, price FROM products WHERE stock < threshold";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.add(new InventoryStats(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("stock"),
                    rs.getInt("threshold"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    public List<InventoryStats> getNonReplenishProducts() {
        List<InventoryStats> stats = new ArrayList<>();
        String sql = "SELECT id, name, stock, threshold, price FROM products WHERE stock >= threshold";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.add(new InventoryStats(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("stock"),
                    rs.getInt("threshold"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    public static class InventoryStats {
        private String productId;
        private String name;
        private int stock;
        private int threshold;
        private double price;
        
        public InventoryStats(String productId, String name, int stock, int threshold, double price) {
            this.productId = productId;
            this.name = name;
            this.stock = stock;
            this.threshold = threshold;
            this.price = price;
        }
        
        public String getProductId() { return productId; }
        public String getName() { return name; }
        public int getStock() { return stock; }
        public int getThreshold() { return threshold; }
        public double getPrice() { return price; }
        
        @Override
        public String toString() {
            return "Product: " + productId + " - " + name + " | Stock: " + stock 
                + " | Threshold: " + threshold + " | Price: " + price;
        }
    }
}
