package com.minimercado.javafxinventario.modules;

import java.util.ArrayList;
import java.util.List;

public class AccountingModule {
    private List<Transaction> transactions;
    private BalanceReport balanceReport;
    
    public AccountingModule() {
        transactions = new ArrayList<>();
        balanceReport = new BalanceReport();
    }
    
    public void recordTransaction(Transaction tx) {
        transactions.add(tx);
        if ("venta".equalsIgnoreCase(tx.getType())) {
            double tax = tx.getAmount() * 0.12;
            tx.setAmount(tx.getAmount() - tax);
        }
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    private double calculateTotalByType(String type) {
        double total = 0.0;
        for (Transaction tx : transactions) {
            if (tx.getType().equalsIgnoreCase(type)) {
                total += tx.getAmount();
            }
        }
        return total;
    }
    
    public String generateFinancialReport() {
        double totalSales = calculateTotalByType("venta");
        double totalPurchases = calculateTotalByType("compra");
        double totalExpenses = calculateTotalByType("gasto");
        double grossProfit = totalSales - totalPurchases;
        double netProfit = grossProfit - totalExpenses;
        
        String report = "Reporte Financiero:\n";
        report += "Ventas: " + totalSales + "\n";
        report += "Compras: " + totalPurchases + "\n";
        report += "Gastos: " + totalExpenses + "\n";
        report += "Utilidad Bruta: " + grossProfit + "\n";
        report += "Utilidad Neta: " + netProfit + "\n";
        report += generateDailyLedger();
        return report;
    }
    
    public String generateDailyLedger() {
        StringBuilder ledger = new StringBuilder();
        ledger.append("Libro Diario:\n");
        for (Transaction tx : transactions) {
            ledger.append("Tipo: ").append(tx.getType())
                  .append(", Monto: ").append(tx.getAmount())
                  .append(", Descripción: ").append(tx.getDescription())
                  .append("\n");
        }
        return ledger.toString();
    }
}
