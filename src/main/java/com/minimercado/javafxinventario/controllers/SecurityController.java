package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.SecurityModule;

public class SecurityController {
    private SecurityModule securityModule = new SecurityModule();
    
    public boolean login(String username, String password) {
        return securityModule.login(username, password);
    }
    
    public boolean registerAdmin(String username, String password, String adminCode) {
        return securityModule.registerAdmin(username, password, adminCode);
    }
    
    // ...otros métodos de seguridad...
}
