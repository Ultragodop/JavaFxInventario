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
        String sql = "SELECT id, name, stock_quantity, reorder_level, selling_price FROM products WHERE stock_quantity < reorder_level";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.add(new InventoryStats(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("reorder_level"),
                    rs.getDouble("selling_price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    public List<InventoryStats> getNonReplenishProducts() {
        List<InventoryStats> stats = new ArrayList<>();
        String sql = "SELECT id, name, stock_quantity, reorder_level, selling_price FROM products WHERE stock_quantity >= reorder_level";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stats.add(new InventoryStats(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("reorder_level"),
                    rs.getDouble("selling_price")
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
        private int stockQuantity;
        private int reorderLevel;
        private double sellingPrice;
        
        public InventoryStats(String productId, String name, int stockQuantity, int reorderLevel, double sellingPrice) {
            this.productId = productId;
            this.name = name;
            this.stockQuantity = stockQuantity;
            this.reorderLevel = reorderLevel;
            this.sellingPrice = sellingPrice;
        }
        
        public String getProductId() { return productId; }
        public String getName() { return name; }
        public int getStockQuantity() { return stockQuantity; }
        public int getReorderLevel() { return reorderLevel; }
        public double getSellingPrice() { return sellingPrice; }
        
        @Override
        public String toString() {
            return "Product: " + productId + " - " + name + " | Stock: " + stockQuantity 
                + " | Threshold: " + reorderLevel + " | Price: " + sellingPrice;
        }
    }
}
