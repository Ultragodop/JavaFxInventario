package com.minimercado.javafxinventario.modules;

import com.minimercado.javafxinventario.DAO.AccountingDAO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Clase que maneja el seguimiento y generación de reportes de balances financieros.
 * Permite rastrear ingresos, gastos y balance neto por período y categoría.
 */
public class BalanceReport {
    private static final Logger logger = Logger.getLogger(BalanceReport.class.getName());
    
    private double currentBalance;
    private double totalIncome;
    private double totalExpenses;
    private Map<LocalDate, Double> dailyBalances;
    private Map<String, Double> balanceByCategory;
    private List<Transaction> transactionHistory;
    private Map<String, Double> incomeByCategory;
    private Map<String, Double> expensesByCategory;
    private Map<LocalDate, Double> dailyIncome;
    private Map<LocalDate, Double> dailyExpenses;
    private LocalDate startDate;
    private LocalDate endDate;
    private AccountingDAO accountingDAO;
    private boolean isDirty; // Flag to track if data has changed since last save
    
    /**
     * Construye un nuevo reporte de balance para el período actual
     */
    public BalanceReport() {
        this(LocalDate.now().withDayOfMonth(1), LocalDate.now());
    }
    
    /**
     * Construye un nuevo reporte de balance para un período específico
     * 
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     */
    public BalanceReport(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentBalance = 0.0;
        this.totalIncome = 0.0;
        this.totalExpenses = 0.0;
        this.dailyBalances = new HashMap<>();
        this.balanceByCategory = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
        this.incomeByCategory = new HashMap<>();
        this.expensesByCategory = new HashMap<>();
        this.dailyIncome = new HashMap<>();
        this.dailyExpenses = new HashMap<>();
        this.isDirty = false;
        
        try {
            this.accountingDAO = new AccountingDAO();
            loadTransactionsFromDatabase();
        } catch (Exception e) {
            logger.warning("Error initializing AccountingDAO: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el balance con una nueva transacción
     * 
     * @param transaction La transacción a registrar
     */
    public void updateBalance(Transaction transaction) {
        if (transaction == null) return;
        
        // Evitar procesar transacciones reversadas
        if (transaction.isReversed()) return;
        
        double amount = transaction.getAmount();
        LocalDate transactionDate = transaction.getTimestamp().toLocalDate();
        String type = transaction.getType();
        
        // Actualizar balance general
        currentBalance += amount;
        
        // Actualizar ingresos o gastos según el tipo de transacción
        if (amount > 0) {
            totalIncome += amount;
            
            // Actualizar ingresos por categoría
            incomeByCategory.put(type, incomeByCategory.getOrDefault(type, 0.0) + amount);
            
            // Actualizar ingresos diarios
            dailyIncome.put(transactionDate, dailyIncome.getOrDefault(transactionDate, 0.0) + amount);
        } else {
            totalExpenses += Math.abs(amount);
            
            // Actualizar gastos por categoría
            expensesByCategory.put(type, expensesByCategory.getOrDefault(type, 0.0) + Math.abs(amount));
            
            // Actualizar gastos diarios
            dailyExpenses.put(transactionDate, dailyExpenses.getOrDefault(transactionDate, 0.0) + Math.abs(amount));
        }
        
        // Actualizar balance por categoría
        balanceByCategory.put(type, balanceByCategory.getOrDefault(type, 0.0) + amount);
        
        // Actualizar balance diario
        dailyBalances.put(transactionDate, dailyBalances.getOrDefault(transactionDate, 0.0) + amount);
        
        // Agregar a historial
        transactionHistory.add(transaction);
        
        isDirty = true;
    }
    
    /**
     * Obtiene el balance actual
     * 
     * @return El balance total actual
     */
    public double getCurrentBalance() {
        return currentBalance;
    }
    
    /**
     * Obtiene el total de ingresos registrados
     * 
     * @return Total de ingresos
     */
    public double getTotalIncome() {
        return totalIncome;
    }
    
    /**
     * Obtiene el total de gastos registrados
     * 
     * @return Total de gastos
     */
    public double getTotalExpenses() {
        return totalExpenses;
    }
    
    /**
     * Calcula el beneficio (ingresos menos gastos)
     * 
     * @return Monto del beneficio actual
     */
    public double getProfit() {
        return totalIncome - totalExpenses;
    }
    
    /**
     * Obtiene el balance para una fecha específica
     * 
     * @param date La fecha a consultar
     * @return El balance para esa fecha
     */
    public double getDailyBalance(LocalDate date) {
        return dailyBalances.getOrDefault(date, 0.0);
    }
    
    /**
     * Obtiene el balance para una categoría específica de transacción
     * 
     * @param category La categoría (tipo de transacción)
     * @return El balance para esa categoría
     */
    public double getCategoryBalance(String category) {
        return balanceByCategory.getOrDefault(category, 0.0);
    }
    
    /**
     * Obtiene el total de ingresos para una fecha específica
     * 
     * @param date La fecha a consultar
     * @return Los ingresos para esa fecha
     */
    public double getDailyIncome(LocalDate date) {
        return dailyIncome.getOrDefault(date, 0.0);
    }
    
    /**
     * Obtiene el total de gastos para una fecha específica
     * 
     * @param date La fecha a consultar
     * @return Los gastos para esa fecha
     */
    public double getDailyExpenses(LocalDate date) {
        return dailyExpenses.getOrDefault(date, 0.0);
    }
    
    /**
     * Obtiene los ingresos para una categoría específica
     * 
     * @param category La categoría a consultar
     * @return Los ingresos para esa categoría
     */
    public double getCategoryIncome(String category) {
        return incomeByCategory.getOrDefault(category, 0.0);
    }
    
    /**
     * Obtiene los gastos para una categoría específica
     * 
     * @param category La categoría a consultar
     * @return Los gastos para esa categoría
     */
    public double getCategoryExpenses(String category) {
        return expensesByCategory.getOrDefault(category, 0.0);
    }
    
    /**
     * Obtiene el histórico de transacciones
     * 
     * @return Lista de transacciones registradas
     */
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }
    
    /**
     * Obtiene las transacciones para una fecha específica
     * 
     * @param date La fecha a consultar
     * @return Lista de transacciones para esa fecha
     */
    public List<Transaction> getTransactionsForDate(LocalDate date) {
        return transactionHistory.stream()
                .filter(t -> t.getTimestamp().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las transacciones de una categoría específica
     * 
     * @param category La categoría a consultar
     * @return Lista de transacciones de esa categoría
     */
    public List<Transaction> getTransactionsForCategory(String category) {
        return transactionHistory.stream()
                .filter(t -> t.getType().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las transacciones para un período específico
     * 
     * @param start Fecha de inicio del período
     * @param end Fecha de fin del período
     * @return Lista de transacciones en ese período
     */
    public List<Transaction> getTransactionsForPeriod(LocalDate start, LocalDate end) {
        return transactionHistory.stream()
                .filter(t -> {
                    LocalDate transactionDate = t.getTimestamp().toLocalDate();
                    return !transactionDate.isBefore(start) && !transactionDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene los totales diarios para un período específico
     * 
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Mapa de fechas con sus balances correspondientes
     */
    public Map<LocalDate, Double> getDailyBalancesForPeriod(LocalDate start, LocalDate end) {
        return dailyBalances.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Calcula el balance semanal para una semana específica
     * 
     * @param weekStart Primer día de la semana
     * @return Balance total de la semana
     */
    public double getWeeklyBalance(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getBalanceForPeriod(weekStart, weekEnd);
    }
    
    /**
     * Calcula el balance mensual para un mes específico
     * 
     * @param year Año
     * @param month Mes (1-12)
     * @return Balance total del mes
     */
    public double getMonthlyBalance(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        return getBalanceForPeriod(monthStart, monthEnd);
    }
    
    /**
     * Calcula el balance para un período específico
     * 
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Balance total del período
     */
    public double getBalanceForPeriod(LocalDate start, LocalDate end) {
        return dailyBalances.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
    
    /**
     * Calcula los ingresos para un período específico
     * 
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Ingresos totales del período
     */
    public double getIncomeForPeriod(LocalDate start, LocalDate end) {
        return dailyIncome.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
    
    /**
     * Calcula los gastos para un período específico
     * 
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Gastos totales del período
     */
    public double getExpensesForPeriod(LocalDate start, LocalDate end) {
        return dailyExpenses.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(start) && !entry.getKey().isAfter(end))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
    
    /**
     * Calcula el beneficio para un período específico
     * 
     * @param start Fecha de inicio
     * @param end Fecha de fin
     * @return Beneficio del período (ingresos - gastos)
     */
    public double getProfitForPeriod(LocalDate start, LocalDate end) {
        double income = getIncomeForPeriod(start, end);
        double expenses = getExpensesForPeriod(start, end);
        return income - expenses;
    }
    
    /**
     * Reseta el reporte, borrando todos los datos acumulados
     */
    public void reset() {
        this.currentBalance = 0.0;
        this.totalIncome = 0.0;
        this.totalExpenses = 0.0;
        this.dailyBalances.clear();
        this.balanceByCategory.clear();
        this.transactionHistory.clear();
        this.incomeByCategory.clear();
        this.expensesByCategory.clear();
        this.dailyIncome.clear();
        this.dailyExpenses.clear();
        
        isDirty = true;
    }
    
    /**
     * Genera un resumen financiero del período actual
     * 
     * @return Texto con el resumen financiero
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        summary.append("REPORTE FINANCIERO\n");
        summary.append("==================\n\n");
        summary.append("Período: ")
               .append(startDate.format(dateFormatter))
               .append(" a ")
               .append(endDate.format(dateFormatter))
               .append("\n\n");
               
        summary.append("RESUMEN GENERAL\n");
        summary.append("----------------\n");
        summary.append(String.format("Ingresos Totales: $%.2f\n", totalIncome));
        summary.append(String.format("Gastos Totales: $%.2f\n", totalExpenses));
        summary.append(String.format("Beneficio Neto: $%.2f\n\n", getProfit()));
        summary.append(String.format("Balance Actual: $%.2f\n\n", currentBalance));
        
        summary.append("INGRESOS POR CATEGORÍA\n");
        summary.append("----------------------\n");
        incomeByCategory.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Ordenar por monto descendente
                .forEach(entry -> {
                    summary.append(String.format("%-20s $%.2f\n", entry.getKey(), entry.getValue()));
                });
        summary.append("\n");
        
        summary.append("GASTOS POR CATEGORÍA\n");
        summary.append("-------------------\n");
        expensesByCategory.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Ordenar por monto descendente
                .forEach(entry -> {
                    summary.append(String.format("%-20s $%.2f\n", entry.getKey(), entry.getValue()));
                });
        summary.append("\n");
        
        summary.append("TRANSACCIONES RECIENTES\n");
        summary.append("----------------------\n");
        transactionHistory.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Ordenar por fecha descendente
                .limit(10) // Solo las 10 más recientes
                .forEach(transaction -> {
                    summary.append(String.format("%s | %-15s | $%8.2f | %s\n",
                            transaction.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            transaction.getType(),
                            transaction.getAmount(),
                            transaction.getDescription()));
                });
        
        return summary.toString();
    }
    
    /**
     * Exporta los datos del reporte a formato CSV
     * 
     * @param filePath Ruta donde guardar el archivo
     * @return true si la exportación fue exitosa
     */
    public boolean exportToCSV(String filePath) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filePath)) {
            // Escribir encabezados
            writer.println("Fecha,Tipo,Monto,Descripción");
            
            // Escribir transacciones
            for (Transaction transaction : transactionHistory) {
                writer.println(String.format("%s,%s,%.2f,\"%s\"",
                        transaction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getDescription().replace("\"", "\"\"")));
            }
            
            return true;
        } catch (java.io.FileNotFoundException e) {
            logger.warning("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Analiza las tendencias de ingresos y gastos
     * 
     * @param periods Número de períodos anteriores para analizar
     * @param periodType Tipo de período: "day", "week", "month"
     * @return Mapa con las tendencias (porcentajes de cambio)
     */
    public Map<String, Double> analyzeTrends(int periods, String periodType) {
        Map<String, Double> trends = new HashMap<>();
        LocalDate currentPeriodStart;
        LocalDate currentPeriodEnd = LocalDate.now();
        LocalDate previousPeriodStart;
        LocalDate previousPeriodEnd;
        
        switch (periodType.toLowerCase()) {
            case "day":
                currentPeriodStart = currentPeriodEnd;
                previousPeriodEnd = currentPeriodStart.minusDays(1);
                previousPeriodStart = previousPeriodEnd;
                break;
            case "week":
                currentPeriodStart = currentPeriodEnd.minusDays(currentPeriodEnd.getDayOfWeek().getValue() - 1);
                previousPeriodEnd = currentPeriodStart.minusDays(1);
                previousPeriodStart = previousPeriodEnd.minusDays(6);
                break;
            case "month":
                currentPeriodStart = currentPeriodEnd.withDayOfMonth(1);
                previousPeriodEnd = currentPeriodStart.minusDays(1);
                previousPeriodStart = previousPeriodEnd.withDayOfMonth(1);
                break;
            default:
                return trends;
        }
        
        double currentIncome = getIncomeForPeriod(currentPeriodStart, currentPeriodEnd);
        double currentExpenses = getExpensesForPeriod(currentPeriodStart, currentPeriodEnd);
        double currentProfit = currentIncome - currentExpenses;
        
        double previousIncome = getIncomeForPeriod(previousPeriodStart, previousPeriodEnd);
        double previousExpenses = getExpensesForPeriod(previousPeriodStart, previousPeriodEnd);
        double previousProfit = previousIncome - previousExpenses;
        
        // Calcular cambios porcentuales
        double incomeChange = calculatePercentageChange(previousIncome, currentIncome);
        double expensesChange = calculatePercentageChange(previousExpenses, currentExpenses);
        double profitChange = calculatePercentageChange(previousProfit, currentProfit);
        
        trends.put("incomeChange", incomeChange);
        trends.put("expensesChange", expensesChange);
        trends.put("profitChange", profitChange);
        
        return trends;
    }
    
    private double calculatePercentageChange(double previous, double current) {
        if (previous == 0) return current > 0 ? 100 : 0;
        return ((current - previous) / previous) * 100;
    }
    
    /**
     * Carga transacciones desde la base de datos
     */
    private void loadTransactionsFromDatabase() {
        try {
            if (accountingDAO != null) {
                List<Transaction> transactions = accountingDAO.getTransactionsByDateRange(startDate, endDate);
                for (Transaction transaction : transactions) {
                    updateBalance(transaction);
                }
                isDirty = false;
            }
        } catch (Exception e) {
            logger.warning("Error loading transactions from database: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene las mejores y peores categorías de gastos
     * 
     * @param limit Número de categorías a obtener
     * @return Mapa con listas de las mejores y peores categorías
     */
    public Map<String, List<Map.Entry<String, Double>>> getTopAndBottomExpenseCategories(int limit) {
        Map<String, List<Map.Entry<String, Double>>> result = new HashMap<>();
        
        List<Map.Entry<String, Double>> sortedExpenses = new ArrayList<>(expensesByCategory.entrySet());
        sortedExpenses.sort(Map.Entry.comparingByValue());
        
        List<Map.Entry<String, Double>> lowestExpenses = new ArrayList<>(
            sortedExpenses.subList(0, Math.min(limit, sortedExpenses.size())));
            
        Collections.reverse(sortedExpenses); // Invertir para obtener gastos más altos
        List<Map.Entry<String, Double>> highestExpenses = new ArrayList<>(
            sortedExpenses.subList(0, Math.min(limit, sortedExpenses.size())));
        
        result.put("highest", highestExpenses);
        result.put("lowest", lowestExpenses);
        
        return result;
    }
    
    /**
     * Genera un informe de balance general con formato específico
     * @param includeTransactions Si se deben incluir transacciones individuales
     * @return String con el informe formateado
     */
    public String generateBalanceSheet(boolean includeTransactions) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        report.append("BALANCE GENERAL\n");
        report.append("==============\n\n");
        report.append("Generado el: ").append(LocalDate.now().format(dateFormatter)).append("\n");
        report.append("Período: ")
              .append(startDate.format(dateFormatter))
              .append(" a ")
              .append(endDate.format(dateFormatter))
              .append("\n\n");
        
        // Activos (dinero en caja, cuentas por cobrar)
        report.append("ACTIVOS\n");
        report.append("---------\n");
        report.append(String.format("Efectivo y Equivalentes:      $%.2f\n", currentBalance));
        report.append(String.format("Cuentas por Cobrar:           $%.2f\n", 0.0)); // Placeholder, se podría calcular
        report.append(String.format("TOTAL ACTIVOS:                $%.2f\n\n", currentBalance));
        
        // Pasivos (deudas, cuentas por pagar)
        report.append("PASIVOS\n");
        report.append("---------\n");
        report.append(String.format("Cuentas por Pagar:            $%.2f\n", 0.0)); // Placeholder
        report.append(String.format("TOTAL PASIVOS:                $%.2f\n\n", 0.0));
        
        // Patrimonio
        double patrimonio = currentBalance - 0.0; // Activos - Pasivos
        report.append("PATRIMONIO\n");
        report.append("---------\n");
        report.append(String.format("Capital:                      $%.2f\n", patrimonio));
        report.append(String.format("TOTAL PATRIMONIO:             $%.2f\n\n", patrimonio));
        
        // Estado de resultados resumido
        report.append("ESTADO DE RESULTADOS (Resumen)\n");
        report.append("------------------------------\n");
        report.append(String.format("Ingresos Totales:             $%.2f\n", totalIncome));
        report.append(String.format("Gastos Totales:               $%.2f\n", totalExpenses));
        report.append(String.format("Beneficio Neto:               $%.2f\n\n", getProfit()));
        
        if (includeTransactions) {
            report.append("DETALLE DE TRANSACCIONES\n");
            report.append("-----------------------\n");
            report.append(String.format("%-12s | %-20s | %10s | %s\n", 
                         "Fecha", "Categoría", "Monto", "Descripción"));
            report.append("---------------------------------------------------------------------------\n");
            
            transactionHistory.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .forEach(t -> {
                    report.append(String.format("%-12s | %-20s | %10.2f | %s\n",
                        t.getTimestamp().toLocalDate().format(dateFormatter),
                        t.getType(),
                        t.getAmount(),
                        t.getDescription()));
                });
        }
        
        return report.toString();
    }
    
    /**
     * Verifica si los datos del informe han sido modificados desde la última carga o guardado
     * @return true si hay cambios sin guardar
     */
    public boolean isDirty() {
        return isDirty;
    }
    
    /**
     * Marca el informe como guardado/sincronizado
     */
    public void markAsSaved() {
        this.isDirty = false;
    }
    
    /**
     * Obtiene la fecha de inicio del período
     * @return Fecha de inicio
     */
    public LocalDate getStartDate() {
        return startDate;
    }
    
    /**
     * Establece la fecha de inicio del período
     * @param startDate Nueva fecha de inicio
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        loadTransactionsFromDatabase(); // Recargar datos para el nuevo período
    }
    
    /**
     * Obtiene la fecha de fin del período
     * @return Fecha de fin
     */
    public LocalDate getEndDate() {
        return endDate;
    }
    
    /**
     * Establece la fecha de fin del período
     * @param endDate Nueva fecha de fin
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        loadTransactionsFromDatabase(); // Recargar datos para el nuevo período
    }
    
    /**
     * Establece el período de análisis
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     */
    public void setPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        reset();
        loadTransactionsFromDatabase();
    }
}
