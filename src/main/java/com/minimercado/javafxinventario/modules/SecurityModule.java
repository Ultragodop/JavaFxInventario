package com.minimercado.javafxinventario.modules;

public class SecurityModule {
    private UserAuthentication auth;
    
    public SecurityModule() {
        auth = new UserAuthentication();
    }
    
    public boolean login(String username, String password) {
        boolean result = auth.authenticate(username, password);
        System.out.println(result ? "Inicio de sesión exitoso para: " + username : "Inicio de sesión fallido para: " + username);
        return result;
    }
    
    public boolean registerAdmin(String username, String password, String adminCode) {
        return auth.registerAdmin(username, password, adminCode);
    }
}
