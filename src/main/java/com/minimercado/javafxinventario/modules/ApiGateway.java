package com.minimercado.javafxinventario.modules;

public class ApiGateway {
    private ApiGatewayHandler handler;
    
    public ApiGateway() {
        handler = new ApiGatewayHandler();
    }
    
    public void processRequest(String request) {
        handler.logRequest(request);
    }
    
    public void notify(String message) {
        handler.sendNotification(message);
    }
}
