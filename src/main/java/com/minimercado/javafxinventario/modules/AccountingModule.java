package com.minimercado.javafxinventario.modules;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccountingModule {
    private List<Transaction> transactions;
    private BalanceReport balanceReport;
    private List<String> auditLog;
    private static AccountingModule instance;
    
    // Singleton pattern para acceso global
    public static synchronized AccountingModule getInstance() {
        if (instance == null) {
            instance = new AccountingModule();
        }
        return instance;
    }
    
    public AccountingModule() {
        transactions = new ArrayList<>();
        balanceReport = new BalanceReport();
        auditLog = new ArrayList<>();
    }
    
    public void recordTransaction(Transaction tx) {
        tx.setTimestamp(LocalDateTime.now());
        transactions.add(tx);
        
        // Aplicar impuestos si es una venta
        if ("venta".equalsIgnoreCase(tx.getType())) {
            double tax = tx.getAmount() * 0.12;
            tx.setTaxAmount(tax);
            
            // Registrar en el log de auditoría
            addAuditEntry("Venta registrada: " + tx.getAmount() + " - Impuesto: " + tax + " - " + tx.getDescription());
        } else if ("egreso".equalsIgnoreCase(tx.getType()) || "gasto".equalsIgnoreCase(tx.getType())) {
            addAuditEntry("Egreso registrado: " + tx.getAmount() + " - " + tx.getDescription());
        }
        
        // Notificar al balance report
        balanceReport.updateBalance(tx);
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    // Obtener transacciones por período
    public List<Transaction> getTransactionsByPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        return transactions.stream().filter(tx -> {
            if (tx.getTimestamp() == null) return false;
            
            long days = ChronoUnit.DAYS.between(tx.getTimestamp(), now);
            switch (period.toLowerCase()) {
                case "diario": return days < 1;
                case "semanal": return days < 7;
                case "mensual": return days < 30;
                case "anual": return days < 365;
                default: return true;
            }
        }).collect(Collectors.toList());
    }
    
    // Método para revertir una transacción
    public boolean reverseTransaction(Transaction tx, String reason) {
        if (tx == null || !transactions.contains(tx)) {
            return false;
        }
        
        // Crear transacción inversa
        Transaction reverseTx = new Transaction(
            tx.getType() + "_reverso",
            -tx.getAmount(),
            "Reversión de: " + tx.getDescription() + " - Motivo: " + reason
        );
        
        recordTransaction(reverseTx);
        addAuditEntry("Transacción revertida: " + tx.getAmount() + " - Motivo: " + reason);
        return true;
    }
    
    private double calculateTotalByType(String type) {
        return transactions.stream()
            .filter(tx -> tx.getType().equalsIgnoreCase(type))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    // Método para calcular totales por tipo en un período específico
    public double calculateTotalByTypeAndPeriod(String type, String period) {
        return getTransactionsByPeriod(period).stream()
            .filter(tx -> tx.getType().equalsIgnoreCase(type))
            .mapToDouble(Transaction::getAmount)
            .sum();
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
        for (Transaction tx : getTransactionsByPeriod("diario")) {
            ledger.append("Tipo: ").append(tx.getType())
                  .append(", Monto: ").append(tx.getAmount())
                  .append(", Descripción: ").append(tx.getDescription())
                  .append(", Fecha: ").append(tx.getTimestamp())
                  .append("\n");
        }
        return ledger.toString();
    }
    
    // Generar informe detallado en Excel
    public XSSFWorkbook generateDetailedReport() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        // Hoja de transacciones
        Sheet transactionsSheet = workbook.createSheet("Transacciones");
        createTransactionsSheet(transactionsSheet);
        
        // Hoja de resumen financiero
        Sheet summarySheet = workbook.createSheet("Resumen Financiero");
        createSummarySheet(summarySheet);
        
        // Hoja de auditoría
        Sheet auditSheet = workbook.createSheet("Auditoría");
        createAuditSheet(auditSheet);
        
        return workbook;
    }
    
    private void createTransactionsSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tipo", "Monto", "Impuesto", "Total", "Descripción", "Fecha"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        int rowNum = 1;
        for (Transaction tx : transactions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tx.getType());
            row.createCell(1).setCellValue(tx.getAmount());
            row.createCell(2).setCellValue(tx.getTaxAmount());
            row.createCell(3).setCellValue(tx.getAmount() + tx.getTaxAmount());
            row.createCell(4).setCellValue(tx.getDescription());
            if (tx.getTimestamp() != null) {
                row.createCell(5).setCellValue(tx.getTimestamp().toString());
            }
        }
    }
    
    private void createSummarySheet(Sheet sheet) {
        // Crear resumen financiero para diferentes períodos
        String[] periods = {"Diario", "Semanal", "Mensual", "Anual", "Total"};
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Período");
        headerRow.createCell(1).setCellValue("Ventas");
        headerRow.createCell(2).setCellValue("Compras");
        headerRow.createCell(3).setCellValue("Gastos");
        headerRow.createCell(4).setCellValue("Utilidad Bruta");
        headerRow.createCell(5).setCellValue("Utilidad Neta");
        
        int rowNum = 1;
        for (String period : periods) {
            double sales = period.equals("Total") ? 
                calculateTotalByType("venta") : 
                calculateTotalByTypeAndPeriod("venta", period.toLowerCase());
                
            double purchases = period.equals("Total") ? 
                calculateTotalByType("compra") : 
                calculateTotalByTypeAndPeriod("compra", period.toLowerCase());
                
            double expenses = period.equals("Total") ? 
                calculateTotalByType("gasto") : 
                calculateTotalByTypeAndPeriod("gasto", period.toLowerCase());
                
            double grossProfit = sales - purchases;
            double netProfit = grossProfit - expenses;
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(period);
            row.createCell(1).setCellValue(sales);
            row.createCell(2).setCellValue(purchases);
            row.createCell(3).setCellValue(expenses);
            row.createCell(4).setCellValue(grossProfit);
            row.createCell(5).setCellValue(netProfit);
        }
    }
    
    private void createAuditSheet(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Entrada de Auditoría");
        
        int rowNum = 1;
        for (String entry : auditLog) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry);
        }
    }
    
    private void addAuditEntry(String entry) {
        String timestamp = LocalDateTime.now().toString();
        auditLog.add(timestamp + " - " + entry);
    }
    
    public String getAuditLog() {
        StringBuilder sb = new StringBuilder();
        for (String entry : auditLog) {
            sb.append(entry).append("\n");
        }
        return sb.toString();
    }
    
    // Método para conciliar transacciones con otro registro
    public List<Transaction> reconcileTransactions(List<Transaction> externalTransactions) {
        List<Transaction> discrepancies = new ArrayList<>();
        
        for (Transaction externalTx : externalTransactions) {
            boolean found = false;
            for (Transaction internalTx : transactions) {
                // Comparar transacciones basadas en timestamp y monto
                if (internalTx.getTimestamp() != null && 
                    externalTx.getTimestamp() != null &&
                    internalTx.getTimestamp().equals(externalTx.getTimestamp()) &&
                    Math.abs(internalTx.getAmount() - externalTx.getAmount()) < 0.001) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                discrepancies.add(externalTx);
            }
        }
        
        return discrepancies;
    }

    /**
     * Checks if a transaction is already recorded in the module.
     * This avoids duplicating transactions imported from database.
     * 
     * @param transaction The transaction to check
     * @return true if the transaction already exists, false otherwise
     */
    public boolean transactionExists(Transaction transaction) {
        // Verify by comparing fields since id might be different
        return transactions.stream().anyMatch(tx -> 
            tx.getTimestamp() != null && 
            transaction.getTimestamp() != null && 
            Math.abs(tx.getAmount() - transaction.getAmount()) < 0.001 &&
            tx.getType().equals(transaction.getType()) &&
            (tx.getDescription().equals(transaction.getDescription()) || 
             tx.getId().equals(transaction.getId()))
        );
    }
}
