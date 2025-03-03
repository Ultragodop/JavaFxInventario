package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.ApiGateway;

public class ApiGatewayController {
    private ApiGateway apiGateway = new ApiGateway();
    
    public void processRequest(String request) {
        apiGateway.processRequest(request);
    }
    
    public void sendNotification(String message) {
        apiGateway.notify(message);
    }
    
    // ...otros métodos de control...
}
