package com.minimercado.javafxinventario.modules;

public class SalesModule {
    private POSController posController;
    private PaymentModule paymentModule;

    public SalesModule() {
        posController = new POSController();
        paymentModule = new PaymentModule();
    }
    
    public POSController getPosController() {
        return posController;
    }

    public PaymentModule getPaymentModule() {
        return paymentModule;
    }
}
