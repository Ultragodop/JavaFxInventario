package com.minimercado.javafxinventario.modules;

/**
 * Interface for components that need to be notified of financial data updates
 */
public interface FinancialUpdateListener {
    
    /**
     * Called when financial data has been updated
     */
    void onFinancialDataUpdated();
}
