package com.minimercado.javafxinventario.modules;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesReportingModule {
    // Configuración: cambiar según tu entorno - usamos variables de entorno o propiedades del sistema
    private final String url;
    private final String user;
    private final String password;
    
    public SalesReportingModule() {
        // Obtener configuración de conexión desde propiedades del sistema o variables de entorno
        this.url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/sales_db");
        this.user = System.getProperty("db.user", "root");
        this.password = System.getProperty("db.password", "your_password");
    }
    
    // Constructor alternativo para pruebas o configuración específica
    public SalesReportingModule(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    // Método mejorado con manejo de errores y cierre adecuado de recursos
    public List<SalesStats> getTopSellingProducts(String startDate, String endDate, int limit) {
        List<SalesStats> stats = new ArrayList<>();
        String sql = "SELECT product_id, SUM(quantity) as total_quantity FROM sales " +
                     "WHERE sale_date BETWEEN ? AND ? " +
                     "GROUP BY product_id ORDER BY total_quantity DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(new SalesStats(rs.getString("product_id"), rs.getInt("total_quantity")));
                }
            }
        } catch(SQLException e) {
            logError("Error obteniendo productos más vendidos", e);
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

    // Método utilitario para obtener conexión
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    
    // Método para registrar errores
    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
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
