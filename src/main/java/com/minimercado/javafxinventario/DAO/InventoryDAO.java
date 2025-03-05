package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private final String url = "jdbc:mysql://localhost:3306/inventory_db";
    private final String user = "InventoryDAO";
    private final String password = "2007absalom"; // Actualiza si es necesario

    public List<Product> getAllProducts() {
        List<Product> lista = new ArrayList<>();
        String sql = "SELECT id, productname, stock, threshold, price FROM products";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while(rs.next()){
                lista.add(new Product(
                    rs.getString("id"),
                    rs.getString("productname"),
                    rs.getInt("stock"),
                    rs.getInt("threshold"),
                    rs.getDouble("price")
                ));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    public Product getProduct(String id) {
        String sql = "SELECT id, productname, stock, threshold, price FROM products WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try(ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return new Product(
                        rs.getString("id"),
                        rs.getString("productname"),
                        rs.getInt("stock"),
                        rs.getInt("threshold"),
                        rs.getDouble("price")
                    );
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void addProduct(Product p) {
        String sql = "INSERT INTO products (id, productname, stock, threshold, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getId());
            stmt.setString(2, p.getName());
            stmt.setInt(3, p.getStock());
            stmt.setInt(4, p.getThreshold());
            stmt.setDouble(5, p.getPrice());
            stmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updateProduct(Product p) {
        String sql = "UPDATE products SET productname = ?, stock = ?, threshold = ?, price = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setInt(2, p.getStock());
            stmt.setInt(3, p.getThreshold());
            stmt.setDouble(4, p.getPrice());
            stmt.setString(5, p.getId());
            stmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeProduct(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> searchProducts(String query) {
        List<Product> lista = new ArrayList<>();
        String sql = "SELECT id, productname, stock, threshold, price FROM products WHERE id = ? OR productname LIKE ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, query);
            stmt.setString(2, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while(rs.next()){
                    lista.add(new Product(
                        rs.getString("id"),
                        rs.getString("productname"),
                        rs.getInt("stock"),
                        rs.getInt("threshold"),
                        rs.getDouble("price")
                    ));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
