package com.minimercado.javafxinventario.modules;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages financial balances and provides reporting on financial status.
 * Updates balances based on transactions processed through the accounting module.
 */
public class BalanceReport {

    private double currentBalance;
    private double totalIncome;
    private double totalExpenses;
    private Map<LocalDate, Double> dailyBalances;
    private Map<String, Double> balanceByCategory;
    
    public BalanceReport() {
        currentBalance = 0.0;
        totalIncome = 0.0;
        totalExpenses = 0.0;
        dailyBalances = new HashMap<>();
        balanceByCategory = new HashMap<>();
    }
    
    /**
     * Update balances based on a transaction
     * 
     * @param transaction The transaction to process
     */
    public void updateBalance(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        
        double amount = transaction.getAmount();
        String type = transaction.getType();
        
        // Update current balance based on transaction type
        if (type.startsWith("venta") || type.equals("ingreso")) {
            currentBalance += amount;
            totalIncome += amount;
        } else if (type.equals("gasto") || type.equals("egreso") || type.equals("compra")) {
            currentBalance -= amount;
            totalExpenses += amount;
        } else if (type.contains("reversal")) {
            // For reversals, we do the opposite action
            if (type.startsWith("venta") || type.startsWith("ingreso")) {
                currentBalance -= amount;
                totalIncome -= amount;
            } else if (type.startsWith("gasto") || type.startsWith("egreso") || type.startsWith("compra")) {
                currentBalance += amount;
                totalExpenses -= amount;
            }
        }
        
        // Update daily balance
        if (transaction.getTimestamp() != null) {
            LocalDate date = transaction.getTimestamp().toLocalDate();
            dailyBalances.put(date, dailyBalances.getOrDefault(date, 0.0) + 
                             (type.contains("reversal") ? -amount : amount));
        }
        
        // Update balance by category (using transaction type as category)
        balanceByCategory.put(type, balanceByCategory.getOrDefault(type, 0.0) + amount);
    }
    
    /**
     * Get the current overall balance
     * 
     * @return The current balance
     */
    public double getCurrentBalance() {
        return currentBalance;
    }
    
    /**
     * Get total income recorded
     * 
     * @return Total income amount
     */
    public double getTotalIncome() {
        return totalIncome;
    }
    
    /**
     * Get total expenses recorded
     * 
     * @return Total expenses amount
     */
    public double getTotalExpenses() {
        return totalExpenses;
    }
    
    /**
     * Get profit (income minus expenses)
     * 
     * @return Current profit amount
     */
    public double getProfit() {
        return totalIncome - totalExpenses;
    }
    
    /**
     * Get balance for a specific date
     * 
     * @param date The date to check
     * @return The balance for that date
     */
    public double getDailyBalance(LocalDate date) {
        return dailyBalances.getOrDefault(date, 0.0);
    }
    
    /**
     * Get balance for a specific transaction category
     * 
     * @param category The category (transaction type)
     * @return The balance for that category
     */
    public double getCategoryBalance(String category) {
        return balanceByCategory.getOrDefault(category, 0.0);
    }
    
    /**
     * Reset all balances to zero
     */
    public void reset() {
        currentBalance = 0.0;
        totalIncome = 0.0;
        totalExpenses = 0.0;
        dailyBalances.clear();
        balanceByCategory.clear();
    }
    
    /**
     * Generate a summary of the current financial status
     * 
     * @return A summary string
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Balance Report Summary:\n");
        summary.append("Current Balance: ").append(String.format("%.2f", currentBalance)).append("\n");
        summary.append("Total Income: ").append(String.format("%.2f", totalIncome)).append("\n");
        summary.append("Total Expenses: ").append(String.format("%.2f", totalExpenses)).append("\n");
        summary.append("Profit: ").append(String.format("%.2f", getProfit())).append("\n");
        
        return summary.toString();
    }
}
