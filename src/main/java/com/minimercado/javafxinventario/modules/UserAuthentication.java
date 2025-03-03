package com.minimercado.javafxinventario.modules;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserAuthentication {
    private UserDAO userDAO = new UserDAO();
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    // Credencial requerida para registrarse como administrador
    private static final String REQUIRED_ADMIN_CODE = "ADMIN2023";
    
    public boolean authenticate(String username, String userpasswd) {
        String storedHash = userDAO.getUserHashedPassword(username);
        if (storedHash != null) {
            return encoder.matches(userpasswd, storedHash);
        }
        return false;
    }
    
    public boolean registerAdmin(String username, String userpasswd, String adminCode) {
        if (!REQUIRED_ADMIN_CODE.equals(adminCode)) {
            throw new IllegalArgumentException("Código de administrador inválido.");
        }
        if (userDAO.userExists(username)) {
            throw new IllegalArgumentException("El usuario ya existe.");
        }
        String hashed = encoder.encode(userpasswd);
        try {
            userDAO.addUser(username, hashed, "admin", adminCode);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
