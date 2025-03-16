package com.minimercado.javafxinventario.modules;

/**
 * Interface for listeners that need to be notified when financial data is updated
 */
public interface FinancialUpdateListener {
    /**
     * Called when financial data has been updated
     */
    void onFinancialDataUpdated();
}
