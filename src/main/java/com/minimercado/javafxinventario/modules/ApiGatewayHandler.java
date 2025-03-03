package com.minimercado.javafxinventario.modules;

public class ApiGatewayHandler {
    public void logRequest(String request) {
        System.out.println("Log: " + request);
    }
    
    public void sendNotification(String message) {
        System.out.println("Notificación: " + message);
    }
}
