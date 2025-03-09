package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.SalesModule;
import com.minimercado.javafxinventario.modules.POSController;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.InventoryModule;
import com.minimercado.javafxinventario.enums.PaymentMethod;

public class SalesController {
    private SalesModule salesModule = new SalesModule();
    
    // Permite acceder al controlador del POS a través del módulo de ventas.
    public POSController getPOSController() {
        return salesModule.getPosController();
    }
    
    // Métodos adicionales para pagos o lógica de ventas pueden agregarse aquí.
}
