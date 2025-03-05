package com.minimercado.javafxinventario.DAO;

import java.sql.*;

public class UserDAO {
    private final String url = "jdbc:mysql://localhost:3306/users_db";
    private final String user = "UsersDAO";
    private final String password = "2007absalom";

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void addUser(String username, String hashedPassword, String role, String regCode) throws SQLException {
        // Se usan los nombres de columna correctos: userpasswd y rol
        String sql = "INSERT INTO users (username, userpasswd, rol, registration_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.setString(4, regCode);
            stmt.executeUpdate();
        }
    }
    
    public String getUserHashedPassword(String username) {
        // Se selecciona la columna userpasswd
        String sql = "SELECT userpasswd FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, username);
             ResultSet rs = stmt.executeQuery();
             if(rs.next()){
                return rs.getString("userpasswd");
             }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getUserRole(String username) {
        // Se selecciona la columna rol
        String sql = "SELECT rol FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url, this.user, this.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, username);
             ResultSet rs = stmt.executeQuery();
             if(rs.next()){
                return rs.getString("rol");
             }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
