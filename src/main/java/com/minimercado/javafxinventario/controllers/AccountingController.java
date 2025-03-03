package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.Transaction;
import java.util.List;

public class AccountingController {
    private AccountingModule accountingModule = new AccountingModule();
    
    public void recordTransaction(Transaction tx) {
        accountingModule.recordTransaction(tx);
    }
    
    public List<Transaction> getTransactions() {
        return accountingModule.getTransactions();
    }
    
    public String getFinancialReport() {
        return accountingModule.generateFinancialReport();
    }
    
    // ...otros métodos de control...
}
