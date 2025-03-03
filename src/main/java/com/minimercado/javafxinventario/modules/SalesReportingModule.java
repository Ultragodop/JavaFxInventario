package com.minimercado.javafxinventario.modules;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesReportingModule {
    // Configuración: cambiar según tu entorno
    private final String url = "jdbc:mysql://localhost:3306/sales_db";
    private final String user = "root";
    private final String password = "your_password";
    
    public List<SalesStats> getTopSellingProducts(String startDate, String endDate, int limit) {
        List<SalesStats> stats = new ArrayList<>();
        String sql = "SELECT product_id, SUM(quantity) as total_quantity FROM sales " +
                     "WHERE sale_date BETWEEN ? AND ? " +
                     "GROUP BY product_id ORDER BY total_quantity DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.add(new SalesStats(rs.getString("product_id"), rs.getInt("total_quantity")));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    public List<SalesStats> getLowSellingProducts(String startDate, String endDate, int limit) {
        List<SalesStats> stats = new ArrayList<>();
        String sql = "SELECT product_id, SUM(quantity) as total_quantity FROM sales " +
                     "WHERE sale_date BETWEEN ? AND ? " +
                     "GROUP BY product_id ORDER BY total_quantity ASC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.add(new SalesStats(rs.getString("product_id"), rs.getInt("total_quantity")));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static class SalesStats {
        private String productId;
        private int totalQuantity;
        
        public SalesStats(String productId, int totalQuantity) {
            this.productId = productId;
            this.totalQuantity = totalQuantity;
        }
        
        public String getProductId() { return productId; }
        public int getTotalQuantity() { return totalQuantity; }
        
        @Override
        public String toString() {
            return "Product: " + productId + " - Total Sold: " + totalQuantity;
        }
    }
}
