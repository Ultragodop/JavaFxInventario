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
import com.minimercado.javafxinventario.DAO.AccountingDAO;

public class AccountingModule {
    private List<Transaction> transactions;
    private BalanceReport balanceReport;
    private List<String> auditLog;
    private static AccountingModule instance;
    private AccountingDAO accountingDAO; // Add DAO reference
    private List<FinancialUpdateListener> updateListeners = new ArrayList<>(); // Add listener mechanism
    
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
        accountingDAO = new AccountingDAO(); // Initialize the DAO
    }
    
    /**
     * Records a transaction in the accounting system
     * @param transaction The transaction to record
     * @return true if the transaction was successfully recorded
     */
    public boolean recordTransaction(Transaction transaction) {
        try {
            // Set the timestamp if not already set
            if (transaction.getTimestamp() == null) {
                transaction.setTimestamp(LocalDateTime.now());
            }
            
            // Generate ID if not present
            if (transaction.getId() == null || transaction.getId().isEmpty()) {
                transaction.setId(generateTransactionId(transaction.getType()));
            }
            
            // Add to in-memory transactions list
            transactions.add(transaction);
            
            // Apply taxes if it's a sale
            if ("venta".equalsIgnoreCase(transaction.getType())) {
                double tax = transaction.getAmount() * 0.12;
                transaction.setTaxAmount(tax);
                
                // Register in audit log
                addAuditEntry("Venta registrada: " + transaction.getAmount() + 
                             " - Impuesto: " + tax + " - " + transaction.getDescription());
            } else if ("egreso".equalsIgnoreCase(transaction.getType()) || 
                       "gasto".equalsIgnoreCase(transaction.getType()) ||
                       "compra".equalsIgnoreCase(transaction.getType())) {
                addAuditEntry(transaction.getType() + " registrado: " + 
                             Math.abs(transaction.getAmount()) + " - " + transaction.getDescription());
            }
            
            // Add the transaction to the database
            boolean success = accountingDAO.recordTransaction(transaction);
            
            if (success) {
                // Update balance report
                balanceReport.updateBalance(transaction);
                
                // Add audit entry for database save
                addAuditEntry("Transacción guardada en la base de datos: " + transaction.getId());
                
                // Notify any registered listeners about the update
                notifyFinancialDataUpdated();
                
                return true;
            } else {
                addAuditEntry("ERROR: No se pudo guardar la transacción en la base de datos: " + transaction.getId());
                System.err.println("Error saving transaction to database: " + transaction.getId());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error recording transaction: " + e.getMessage());
            e.printStackTrace();
            addAuditEntry("ERROR: Excepción al registrar transacción: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a unique transaction ID
     * @param type Type of transaction
     * @return A unique transaction ID
     */
    private String generateTransactionId(String type) {
        String prefix = type.substring(0, Math.min(3, type.length())).toUpperCase();
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 1000);
        return prefix + "-" + timestamp + "-" + random;
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
    
    public double calculateTotalByType(String type) {
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
        
        // Include all expense types in total purchases and expenses calculations
        double totalPurchases = calculateTotalByType("compra");
        
        // Combine all expense types (egreso, gasto)
        double totalExpenses = calculateTotalByType("gasto") + calculateTotalByType("egreso");
        
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
    
    // Método para conciliar transacciones con otro registro con mejor validación
    public List<Transaction> reconcileTransactions(List<Transaction> externalTransactions) {
        if (externalTransactions == null || externalTransactions.isEmpty()) {
            return new ArrayList<>();
        }
        
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
        if (transaction == null) {
            return false;
        }
        
        // Verify by comparing fields since id might be different
        return transactions.stream().anyMatch(tx -> 
            tx.getTimestamp() != null && 
            transaction.getTimestamp() != null && 
            Math.abs(tx.getAmount() - transaction.getAmount()) < 0.001 &&
            tx.getType() != null &&
            transaction.getType() != null &&
            tx.getType().equals(transaction.getType()) &&
            ((tx.getDescription() != null && 
              transaction.getDescription() != null && 
              tx.getDescription().equals(transaction.getDescription())) || 
             (tx.getId() != null && 
              transaction.getId() != null && 
              tx.getId().equals(transaction.getId())))
        );
    }

    /**
     * Get or create the current balance report
     * @return The current balance report
     */
    private BalanceReport getOrCreateCurrentReport() {
        if (balanceReport == null) {
            balanceReport = new BalanceReport();
        }
        return balanceReport;
    }
    
    /**
     * Add a listener to be notified when financial data changes
     * @param listener The listener to add
     */
    public void addFinancialUpdateListener(FinancialUpdateListener listener) {
        if (!updateListeners.contains(listener)) {
            updateListeners.add(listener);
        }
    }

    /**
     * Remove a financial update listener
     * @param listener The listener to remove
     */
    public void removeFinancialUpdateListener(FinancialUpdateListener listener) {
        updateListeners.remove(listener);
    }

    /**
     * Notify all registered listeners that financial data has been updated
     */
    private void notifyFinancialDataUpdated() {
        for (FinancialUpdateListener listener : updateListeners) {
            listener.onFinancialDataUpdated();
        }
    }

    /**
     * Add a transaction to the module and notify listeners
     * 
     * @param transaction The transaction to add
     */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;
        
        // Add to transactions list if not already present
        if (!transactionExists(transaction)) {
            transactions.add(transaction);
            
            // Log the transaction
            addAuditEntry("Transaction added: " + transaction.getType() + 
                         " - Amount: " + transaction.getAmount() + 
                         " - Description: " + transaction.getDescription());
            
            // Notify listeners about the update
            notifyFinancialDataUpdated();
        }
    }
}
