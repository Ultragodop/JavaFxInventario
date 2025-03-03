package com.minimercado.javafxinventario.modules;

import java.util.ArrayList;

public class BalanceReport {
    public String generateReport(ArrayList<Transaction> transactions) {
        StringBuilder report = new StringBuilder();
        report.append("Balance Report:\n");
        report.append("Transacciones: ").append(transactions.size()).append("\n");
        return report.toString();
    }
}
