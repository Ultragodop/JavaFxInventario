package com.minimercado.javafxinventario.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.minimercado.javafxinventario.enums.PaymentMethod;
import com.minimercado.javafxinventario.modules.ProductoVenta;

public class TransactionModule {
    private List<Transaction> transactions;
    private List<Transaction> pendingSyncTransactions; // Para modo offline
    private static TransactionModule instance;
    
    // Singleton pattern - fixed implementation
    public static synchronized TransactionModule getInstance() {
        if (instance == null) {
            instance = new TransactionModule();
        }
        return instance;
    }

    private TransactionModule() {
        transactions = new ArrayList<>();
        pendingSyncTransactions = new ArrayList<>();
    }
    
    // Método para registrar la transacción con validación
    public boolean logTransaction(List<?> saleItems, PaymentMethod method, double totalConDescuento, double discount) {
        // Validar la transacción antes de registrarla
        if (saleItems == null || saleItems.isEmpty()) {
            System.err.println("Error: No hay productos en la venta");
            return false;
        }
        
        if (totalConDescuento <= 0) {
            System.err.println("Error: El total de la venta debe ser mayor a cero");
            return false;
        }
        
        // Count total quantity of items
        int totalItems = 0;
        if (!saleItems.isEmpty() && saleItems.get(0) instanceof ProductoVenta) {
            
            totalItems = saleItems.stream()
                .map(item -> (ProductoVenta)item)
                .mapToInt(ProductoVenta::getCantidad)
                .sum();
        }
        
        // Create transaction record
        Transaction tx = new Transaction(
            "venta", 
            totalConDescuento, 
            "Venta registrada con método " + method + ", " + saleItems.size() + 
            " productos, " + totalItems + " unidades, descuento: " + discount
        );

        
        transactions.add(tx);
        System.out.println("Transacción registrada: " + tx);
        
        // Actualizar módulo contable
        AccountingModule.getInstance().recordTransaction(tx);
        
        return true;
    }

    
    // Método para registrar transacciones en modo offline
    public boolean logOfflineTransaction(List<?> saleItems, PaymentMethod method, double totalConDescuento, double discount) {
        if (saleItems == null || saleItems.isEmpty() || totalConDescuento <= 0) {
            return false;
        }
        
        // Count total quantity of items
        int totalItems = 0;
        if (!saleItems.isEmpty() && saleItems.get(0) instanceof ProductoVenta) {
            totalItems = saleItems.stream()
                .map(item -> (ProductoVenta)item)
                .mapToInt(ProductoVenta::getCantidad)
                .sum();
        }
        
        Transaction tx = new Transaction(
            "venta_offline", 
            totalConDescuento, 
            "Venta offline con método " + method + ", " + saleItems.size() + 
            " productos, " + totalItems + " unidades, descuento: " + discount
        );
        
        pendingSyncTransactions.add(tx);
        System.out.println("Transacción offline registrada: " + tx);
        return true;
    }
    
    // Método para sincronizar transacciones offline
    public boolean syncOfflineTransactions() {
        boolean allSynced = true;
        List<Transaction> synced = new ArrayList<>();
        
        for (Transaction tx : pendingSyncTransactions) {
            try {
                tx.setType("venta"); // Cambiar de venta_offline a venta normal
                transactions.add(tx);
                AccountingModule.getInstance().recordTransaction(tx);
                synced.add(tx);
            } catch (Exception e) {
                System.err.println("Error al sincronizar transacción: " + e.getMessage());
                allSynced = false;
            }
        }
        
        // Remover las transacciones sincronizadas exitosamente
        pendingSyncTransactions.removeAll(synced);
        return allSynced;
    }
    
    // Método para revertir una transacción
    public boolean reverseTransaction(String transactionId, String reason) {
        Transaction tx = findTransactionById(transactionId);
        if (tx == null || tx.isReversed()) {
            return false;
        }
        
        // Crear transacción de reversión
        double reversalAmount = -tx.getAmount();
        Transaction reversalTx = new Transaction(
            tx.getType() + "_reversal",
            reversalAmount,
            "Reversión de transacción " + tx.getId() + ": " + reason
        );
        
        // Marcar la transacción original como revertida
        tx.setReversed(true);
        tx.setReversalReason(reason);
        
        // Agregar la transacción de reversión
        transactions.add(reversalTx);
        
        // Actualizar el módulo contable
        AccountingModule.getInstance().recordTransaction(reversalTx);
        
        return true; // Añadir el retorno faltante
    }
    
    // Método mejorado para buscar transacciones por descripción
    public List<Transaction> searchTransactions(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>(transactions);
        }
        
        String queryLower = query.toLowerCase();
        
        return transactions.stream()
            .filter(tx -> 
                (tx.getDescription() != null && tx.getDescription().toLowerCase().contains(queryLower)) ||
                (tx.getType() != null && tx.getType().toLowerCase().contains(queryLower)) ||
                (tx.getId() != null && tx.getId().contains(query)))
            .collect(Collectors.toList());
    }
    
    // Método para obtener transacciones por fecha
    public List<Transaction> getTransactionsByDate(LocalDateTime start, LocalDateTime end) {
        return transactions.stream()
            .filter(tx -> {
                LocalDateTime txDate = tx.getTimestamp();
                return txDate != null && 
                       (txDate.isEqual(start) || txDate.isAfter(start)) && 
                       (txDate.isEqual(end) || txDate.isBefore(end));
            })
            .collect(Collectors.toList());
    }
    
    // Método para verificar si una transacción ya existe (evitar duplicados)
    public boolean transactionExists(Transaction tx) {
        return transactions.stream().anyMatch(t -> 
            t.getTimestamp() != null && 
            tx.getTimestamp() != null && 
            Math.abs(t.getAmount() - tx.getAmount()) < 0.001 &&
            t.getTimestamp().isEqual(tx.getTimestamp()) &&
            t.getType().equals(tx.getType())
        );
    }
    
    // Método para obtener las transacciones del módulo contable
    public List<Transaction> getAccountingTransactions() {
        return AccountingModule.getInstance().getTransactions();
    }
    
    // Método para mostrar las transacciones en el módulo contable
    public void displayAccountingTransactions() {
        List<Transaction> accountingTxs = getAccountingTransactions();
        System.out.println("======= Transacciones en Módulo Contable =======");
        if (accountingTxs.isEmpty()) {
            System.out.println("No hay transacciones registradas en el módulo contable");
        } else {
            accountingTxs.forEach(tx -> System.out.println(tx));
        }
        System.out.println("===============================================");
    }
    
    // Método para verificar la consistencia entre este módulo y el módulo contable
    public boolean verifyAccountingConsistency() {
        List<Transaction> accountingTxs = getAccountingTransactions();
        
        // Verificar que todas las transacciones estén en el módulo contable
        boolean allFound = true;
        for (Transaction tx : transactions) {
            if (!accountingTxs.stream().anyMatch(atx -> atx.getId().equals(tx.getId()))) {
                System.err.println("Transacción no encontrada en módulo contable: " + tx.getId());
                allFound = false;
            }
        }
        
        return allFound;
    }
    
    private Transaction findTransactionById(String id) {
        return transactions.stream()
            .filter(tx -> tx.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public List<Transaction> getPendingTransactions() {
        return pendingSyncTransactions;
    }
    
    // Método para validar integridad de las transacciones
    public List<String> validateTransactionIntegrity() {
        List<String> issues = new ArrayList<>();
        
        // Verificar montos negativos (excepto reversiones)
        transactions.stream()
            .filter(tx -> !tx.getType().contains("reversal") && tx.getAmount() < 0)
            .forEach(tx -> issues.add("Monto negativo en transacción no reversal: " + tx.getId()));
        
        // Verificar transacciones sin timestamp
        transactions.stream()
            .filter(tx -> tx.getTimestamp() == null)
            .forEach(tx -> issues.add("Transacción sin timestamp: " + tx.getId()));
        
        // Verificar duplicados
        for (int i = 0; i < transactions.size(); i++) {
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t1 = transactions.get(i);
                Transaction t2 = transactions.get(j);
                
                if (t1.getId().equals(t2.getId())) {
                    issues.add("Transacciones duplicadas encontradas con ID: " + t1.getId());
                }
            }
        }
        
        return issues;
    }
}
