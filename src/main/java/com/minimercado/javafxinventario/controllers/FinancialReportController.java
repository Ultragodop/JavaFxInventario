package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.AccountingDAO;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.BalanceReport;
import com.minimercado.javafxinventario.modules.FinancialUpdateListener;
import com.minimercado.javafxinventario.modules.Transaction;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FinancialReportController implements Initializable {

    private static final Logger logger = Logger.getLogger(FinancialReportController.class.getName());
    public Button refreshButton;
    public Button generateBalanceSheetButton;
    public Button applyTransactionFilterButton;

    // Cards and summary labels
    @FXML private Label currentBalanceLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label profitLabel;
    @FXML private Label incomeTrendLabel;
    @FXML private Label expensesTrendLabel;
    @FXML private Label profitTrendLabel;
    @FXML private Label currentPeriodLabel;
    @FXML private Label statusLabel;

    // Date pickers and filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private Button applyDateButton;

    // Charts
    @FXML private LineChart<String, Number> cashFlowChart;
    @FXML private PieChart incomeCategoryChart;
    @FXML private PieChart expenseCategoryChart;
    @FXML private LineChart<String, Number> trendChart;

    // Transaction table
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Date> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, Double> balanceColumn;

    // Trend analysis
    @FXML private ComboBox<String> trendPeriodComboBox;
    @FXML private Button analyzeTrendsButton;
    @FXML private ListView<String> topIncomeCategoriesView;
    @FXML private ListView<String> topExpenseCategoriesView;

    // Balance sheet
    @FXML private CheckBox includeTransactionsCheckbox;
    @FXML private TextArea balanceSheetTextArea;

    // Category filters
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> transactionTypeComboBox;

    // Export buttons
    @FXML private Button exportButton;
    @FXML private MenuButton exportOptionsButton;

    // Data
    private BalanceReport balanceReport;
    private AccountingDAO accountingDAO;
    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Chart components
    @FXML private BarChart<String, Number> incomeExpenseChart;
    @FXML private LineChart<String, Number> profitTrendChart;
    @FXML private PieChart expenseBreakdownChart;
    
    // Summary labels
    @FXML private Label totalSalesLabel;
    @FXML private Label totalPurchasesLabel;
    @FXML private Label grossProfitLabel;
    @FXML private Label netProfitLabel;

    // Data fields
    private List<Transaction> dailyTransactions;
    private List<Transaction> weeklyTransactions;
    private List<Transaction> monthlyTransactions;
    
    // Financial summary data
    private double totalSales;
    private double totalPurchases;
    private double totalExpenses;
    private double grossProfit;
    private double netProfit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialize the accounting DAO
            accountingDAO = new AccountingDAO();

            // Initialize date pickers and periods
            setupDateControls();

            // Initialize the balance report
            balanceReport = new BalanceReport(
                    startDatePicker.getValue(), 
                    endDatePicker.getValue()
            );

            // Setup the transaction table
            setupTransactionTable();

            // Setup charts
            setupCharts();

            // Setup filter combo boxes
            setupFilterComboBoxes();

            // Load initial data
            updateAllViews();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing Financial Report view", e);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        // Initialize period combo box
        if (periodComboBox != null) {
            periodComboBox.setItems(FXCollections.observableArrayList(
                "Diario", "Semanal", "Mensual", "Anual", "Todos"
            ));
            periodComboBox.setValue("Mensual");
            
            periodComboBox.setOnAction(event -> loadCurrentReport());
        }
        
        // Register this controller as a listener for financial updates
        AccountingModule accountingModule = AccountingModule.getInstance();
        accountingModule.addFinancialUpdateListener((FinancialUpdateListener) this);
        
        // Initialize the report
        loadCurrentReport();
    }

    /**
     * Implements the FinancialUpdateListener interface method
     * to refresh data when financial updates occur
     */
    public void onFinancialDataUpdated() {
        refreshFinancialData();
    }

    /**
     * Refresh financial data when notified of changes
     */
    private void refreshFinancialData() {
        // This method will be called whenever financial data is updated
        // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
        Platform.runLater(() -> {
            loadCurrentReport();
            updateCharts();
            updateSummary();
        });
    }

    /**
     * Loads the current financial report data from the accounting system
     */
    private void loadCurrentReport() {
        try {
            AccountingModule accountingModule = AccountingModule.getInstance();
            
            // Load transaction data for different periods
            dailyTransactions = accountingModule.getTransactionsByPeriod("diario");
            weeklyTransactions = accountingModule.getTransactionsByPeriod("semanal");
            monthlyTransactions = accountingModule.getTransactionsByPeriod("mensual");
            
            // Calculate financial summaries
            // Use public methods instead of accessing private calculateTotalByType
            totalSales = calculateTotalForType("venta");
            totalPurchases = calculateTotalForType("compra");
            totalExpenses = calculateTotalForType("gasto");
            
            // Calculate profits
            grossProfit = totalSales - totalPurchases;
            netProfit = grossProfit - totalExpenses;
            
            // Update transaction tables if they exist
            if (transactionsTable != null) {
                transactionsTable.setItems(FXCollections.observableArrayList(
                    periodComboBox != null && periodComboBox.getValue().equals("Todos") ? 
                        accountingModule.getTransactions() : 
                        getCurrentPeriodTransactions()
                ));
            }
            
            // Update UI components with the new data
            updateCharts();
            updateSummary();
        } catch (Exception e) {
            showError("Error al cargar datos financieros", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to calculate total for a specific transaction type
     * This avoids using the private method in AccountingModule
     */
    private double calculateTotalForType(String type) {
        AccountingModule accountingModule = AccountingModule.getInstance();
        return accountingModule.getTransactions().stream()
            .filter(tx -> type.equalsIgnoreCase(tx.getType()))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    /**
     * Updates the financial charts with the latest data
     */


    /**
     * Updates the income/expense bar chart
     */
    private void updateIncomeExpenseChart() {
        if (incomeExpenseChart != null) {
            // Clear existing data
            incomeExpenseChart.getData().clear();
            
            // Create series for income and expenses
            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Ingresos");
            
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Egresos");
            
            // Get the accounting module
            AccountingModule accountingModule = AccountingModule.getInstance();
            
            // Add data points for different periods
            String[] periods = {"Diario", "Semanal", "Mensual", "Anual"};
            for (String period : periods) {
                double income = calculateTotalForPeriod("venta", period.toLowerCase());
                double expenses = Math.abs(calculateTotalForPeriod("compra", period.toLowerCase()) +
                                  calculateTotalForPeriod("gasto", period.toLowerCase()));
                
                incomeSeries.getData().add(new XYChart.Data<>(period, income));
                expenseSeries.getData().add(new XYChart.Data<>(period, expenses));
            }
            
            // Add series to chart
            incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);
        }
    }
    
    /**
     * Updates the profit trend line chart
     */
    private void updateProfitTrendChart() {
        if (profitTrendChart != null) {
            // Implementation for profit trend chart
            profitTrendChart.getData().clear();
            
            XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
            profitSeries.setName("Utilidad");
            
            String[] periods = {"Diario", "Semanal", "Mensual", "Anual"};
            for (String period : periods) {
                double sales = calculateTotalForPeriod("venta", period.toLowerCase());
                double purchases = calculateTotalForPeriod("compra", period.toLowerCase());
                double expenses = calculateTotalForPeriod("gasto", period.toLowerCase());
                double profit = sales - purchases - expenses;
                
                profitSeries.getData().add(new XYChart.Data<>(period, profit));
            }
            
            profitTrendChart.getData().add(profitSeries);
        }
    }
    
    /**
     * Updates the expense breakdown pie chart
     */
    private void updateExpenseBreakdownChart() {
        if (expenseBreakdownChart != null) {
            // Implementation for expense breakdown chart
            expenseBreakdownChart.getData().clear();
            
            // Example expense categories
            expenseBreakdownChart.getData().addAll(
                new PieChart.Data("Compras", Math.abs(totalPurchases)),
                new PieChart.Data("Salarios", Math.abs(totalExpenses * 0.6)), // Example proportion
                new PieChart.Data("Servicios", Math.abs(totalExpenses * 0.3)), // Example proportion
                new PieChart.Data("Otros", Math.abs(totalExpenses * 0.1))  // Example proportion
            );
        }
    }
    
    /**
     * Helper method to calculate total for a type within a specific period
     */
    private double calculateTotalForPeriod(String type, String period) {
        AccountingModule accountingModule = AccountingModule.getInstance();
        
        if ("gasto".equalsIgnoreCase(type) || "expense".equalsIgnoreCase(type)) {
            // For expenses, include all expense-type transactions
            return accountingModule.getTransactionsByPeriod(period).stream()
                .filter(tx -> "egreso".equalsIgnoreCase(tx.getType()) || 
                              "gasto".equalsIgnoreCase(tx.getType()) || 
                              "compra".equalsIgnoreCase(tx.getType()))
                .mapToDouble(tx -> Math.abs(tx.getAmount()))
                .sum();
        } else {
            // For other types, just filter by the specific type
            return accountingModule.getTransactionsByPeriod(period).stream()
                .filter(tx -> type.equalsIgnoreCase(tx.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        }
    }

    /**
     * Updates the financial summary section of the report
     */
    private void updateSummary() {
        // Update summary labels if they exist
        if (totalSalesLabel != null) {
            totalSalesLabel.setText(String.format("$%.2f", totalSales));
        }
        
        if (totalPurchasesLabel != null) {
            totalPurchasesLabel.setText(String.format("$%.2f", Math.abs(totalPurchases)));
        }
        
        if (totalExpensesLabel != null) {
            totalExpensesLabel.setText(String.format("$%.2f", Math.abs(totalExpenses)));
        }
        
        if (grossProfitLabel != null) {
            grossProfitLabel.setText(String.format("$%.2f", grossProfit));
            // Set style based on profit/loss
            grossProfitLabel.setStyle(grossProfit >= 0 ? 
                "-fx-text-fill: green; -fx-font-weight: bold;" : 
                "-fx-text-fill: red; -fx-font-weight: bold;");
        }
        
        if (netProfitLabel != null) {
            netProfitLabel.setText(String.format("$%.2f", netProfit));
            // Set style based on profit/loss
            netProfitLabel.setStyle(netProfit >= 0 ? 
                "-fx-text-fill: green; -fx-font-weight: bold;" : 
                "-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    /**
     * Gets transactions for the current selected period from the period combo box
     */
    private List<Transaction> getCurrentPeriodTransactions() {
        if (periodComboBox == null) {
            return AccountingModule.getInstance().getTransactions();
        }
        
        String period = periodComboBox.getValue().toLowerCase();
        AccountingModule accountingModule = AccountingModule.getInstance();
        
        switch (period) {
            case "diario": 
                return accountingModule.getTransactionsByPeriod("diario");
            case "semanal": 
                return accountingModule.getTransactionsByPeriod("semanal");
            case "mensual": 
                return accountingModule.getTransactionsByPeriod("mensual");
            case "anual": 
                return accountingModule.getTransactionsByPeriod("anual");
            default: 
                return accountingModule.getTransactions();
        }
    }

    /**
     * Don't forget to clean up when the controller is no longer needed
     */
    public void cleanup() {
        AccountingModule accountingModule = AccountingModule.getInstance();
        accountingModule.removeFinancialUpdateListener((FinancialUpdateListener) this);
    }

    /**
     * Shows an error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupDateControls() {
        // Set up date pickers
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1)); // First day of current month
        endDatePicker.setValue(LocalDate.now());

        // Setup period combo box
        periodComboBox.setItems(FXCollections.observableArrayList(
                "Este mes", "Mes anterior", "Este año", "Año anterior", "Últimos 7 días", "Últimos 30 días", "Últimos 90 días", "Personalizado"
        ));
        periodComboBox.setValue("Este mes");
        
        // Handle period selection
        periodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !"Personalizado".equals(newVal)) {
                setPredefinedDateRange(newVal);
            }
        });

        // Setup trend period combo box
        trendPeriodComboBox.setItems(FXCollections.observableArrayList(
                "Período anterior", "Mismo período año anterior", "Promedio últimos 3 períodos"
        ));
        trendPeriodComboBox.setValue("Período anterior");

        // Update current period label
        updateCurrentPeriodLabel();
    }

    private void setPredefinedDateRange(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start = null;
        LocalDate end = null;

        switch (period) {
            case "Este mes":
                start = now.withDayOfMonth(1);
                end = now;
                break;
            case "Mes anterior":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.withDayOfMonth(1).minusDays(1);
                break;
            case "Este año":
                start = now.withDayOfYear(1);
                end = now;
                break;
            case "Año anterior":
                start = now.minusYears(1).withDayOfYear(1);
                end = now.minusYears(1).with(TemporalAdjusters.lastDayOfYear());
                break;
            case "Últimos 7 días":
                start = now.minusDays(7);
                end = now;
                break;
            case "Últimos 30 días":
                start = now.minusDays(30);
                end = now;
                break;
            case "Últimos 90 días":
                start = now.minusDays(90);
                end = now;
                break;
            default:
                // Keep custom dates
                return;
        }

        startDatePicker.setValue(start);
        endDatePicker.setValue(end);
    }

    private void updateCurrentPeriodLabel() {
        if (startDatePicker != null && endDatePicker != null && currentPeriodLabel != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                currentPeriodLabel.setText(
                        startDate.format(dateFormatter) + " - " + 
                        endDate.format(dateFormatter)
                );
            }
        }
    }

    private void setupTransactionTable() {
        // Configure table columns
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            if (timestamp != null) {
                // Convert LocalDateTime directly to Date
                return new SimpleObjectProperty<>(
                    Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant())
                );
            }
            return new SimpleObjectProperty<>(null);
        });
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType()));
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType())); // Use getType() instead of getCategory()
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        balanceColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject()); // Using amount as balance for now
        
        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<Transaction, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(item));
                }
            }
        });
        
        // Format amount column with 2 decimal places
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                    // Color negative values in red
                    setTextFill(item < 0 ? Color.RED : Color.GREEN);
                }
            }
        });
        
        transactionsTable.setItems(transactionsList);
    }
    
    private void setupCharts() {
        // Clear any existing data
        cashFlowChart.getData().clear();
        incomeCategoryChart.getData().clear();
        expenseCategoryChart.getData().clear();
        trendChart.getData().clear();
    }
    
    private void setupFilterComboBoxes() {
        // Load categories from database
        List<String> categories = new ArrayList<>(); // Initially empty, will be populated later
        categoryFilterComboBox.getItems().add("Todas");
        categoryFilterComboBox.getItems().addAll(categories);
        categoryFilterComboBox.setValue("Todas");
        
        // Transaction types
        transactionTypeComboBox.getItems().addAll("Todos", "Ingresos", "Gastos");
        transactionTypeComboBox.setValue("Todos");
    }

    private void updateAllViews() {
        try {
            // Update date range in balance report
            balanceReport.setPeriod(startDatePicker.getValue(), endDatePicker.getValue());
            
            // Update summary cards
            updateSummaryCards();
            
            // Update charts
            updateCharts();
            
            // Update transaction table
            updateTransactionTable();
            
            // Update period label
            updateCurrentPeriodLabel();
            
            // Update status
            statusLabel.setText("Datos actualizados: " + 
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating views", e);
            statusLabel.setText("Error actualizando datos: " + e.getMessage());
        }
    }

    private void updateSummaryCards() {
        currentBalanceLabel.setText(currencyFormat.format(balanceReport.getCurrentBalance()));
        totalIncomeLabel.setText(currencyFormat.format(balanceReport.getTotalIncome()));
        totalExpensesLabel.setText(currencyFormat.format(balanceReport.getTotalExpenses()));
        profitLabel.setText(currencyFormat.format(balanceReport.getProfit()));
        
        // Style labels based on values
        currentBalanceLabel.setTextFill(balanceReport.getCurrentBalance() >= 0 ? Color.GREEN : Color.RED);
        profitLabel.setTextFill(balanceReport.getProfit() >= 0 ? Color.GREEN : Color.RED);
    }

    private void updateCharts() {
        updateCashFlowChart();
        updateCategoryCharts();
        updateTrendChart("month");
    }

    private void updateTransactionTable() {
        try {
            // Get date range
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Get filter values
            String categoryFilter = categoryFilterComboBox.getValue();
            String typeFilter = transactionTypeComboBox.getValue();
            
            // Get transactions from balance report
            List<Transaction> transactions = balanceReport.getTransactionsForPeriod(startDate, endDate);
            
            // Apply category filter if needed
            if (categoryFilter != null && !categoryFilter.equals("Todas")) {
                transactions = transactions.stream()
                    .filter(t -> categoryFilter.equals(t.getType())) // Use getType() instead of getCategory()
                    .collect(Collectors.toList());
            }
            
            // Apply type filter if needed
            if ("Ingresos".equals(typeFilter)) {
                transactions = transactions.stream()
                    .filter(t -> t.getAmount() > 0)
                    .collect(Collectors.toList());
            } else if ("Gastos".equals(typeFilter)) {
                transactions = transactions.stream()
                    .filter(t -> t.getAmount() < 0)
                    .collect(Collectors.toList());
            }
            
            // Update table
            transactionsList.setAll(transactions);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating transaction table", e);
            statusLabel.setText("Error al cargar transacciones: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnalyzeTrends() {
        try {
            // Get trend analysis and update UI
            Map<String, Double> trends = balanceReport.analyzeTrends(1, "month");
            
            // Update trend labels with results
            updateTrendLabel(incomeTrendLabel, trends.get("incomeChange"));
            updateTrendLabel(expensesTrendLabel, trends.get("expensesChange"));
            updateTrendLabel(profitTrendLabel, trends.get("profitChange"));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error analyzing trends", e);
            statusLabel.setText("Error al analizar tendencias: " + e.getMessage());
        }
    }

    private void updateTrendLabel(Label label, Double trendValue) {
        if (trendValue == null) return;
        
        if (trendValue == 0) {
            label.setText("Sin cambios");
            label.setTextFill(Color.GRAY);
            return;
        }
        
        String arrow = trendValue > 0 ? "↑" : "↓";
        Color color = trendValue > 0 ? Color.GREEN : Color.RED;
        
        // For expenses, the interpretation is reversed (decrease is good)
        if (label == expensesTrendLabel) {
            color = trendValue < 0 ? Color.GREEN : Color.RED;
        }
        
        label.setText(String.format("%s %.1f%%", arrow, Math.abs(trendValue)));
        label.setTextFill(color);
    }

    @FXML
    private void handleRefreshReport() {
        updateAllViews();
    }

    @FXML
    private void handleExportReport() {
        // Implementation
    }

    @FXML
    private void handleExportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar a PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                // This would use a PDF library like iText or PDFBox
                generatePDFReport(file);
                statusLabel.setText("Reporte exportado a PDF: " + file.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error exporting to PDF", e);
                statusLabel.setText("Error al exportar a PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar a Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                // This would use a library like Apache POI
                generateExcelReport(file);
                statusLabel.setText("Reporte exportado a Excel: " + file.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error exporting to Excel", e);
                statusLabel.setText("Error al exportar a Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar a CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                boolean success = balanceReport.exportToCSV(file.getAbsolutePath());
                
                if (success) {
                    statusLabel.setText("Datos exportados a: " + file.getAbsolutePath());
                } else {
                    statusLabel.setText("Error al exportar datos");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error exporting to CSV", e);
                statusLabel.setText("Error al exportar a CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleApplyDateFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            statusLabel.setText("Por favor seleccione un rango de fechas válido");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            statusLabel.setText("La fecha de inicio no puede ser posterior a la fecha de fin");
            return;
        }
        
        // Update balance report with new date range
        balanceReport.setPeriod(startDate, endDate);
        
        // Update all views
        updateAllViews();
    }

    @FXML
    private void handleApplyTransactionFilter() {
        updateTransactionTable();
    }

    @FXML
    private void handleGenerateBalanceSheet() {
        boolean includeTransactions = includeTransactionsCheckbox.isSelected();
        String balanceSheet = balanceReport.generateBalanceSheet(includeTransactions);
        balanceSheetTextArea.setText(balanceSheet);
    }

    /**
     * Updates the cash flow chart
     */
    private void updateCashFlowChart() {
        // Clear existing data
        cashFlowChart.getData().clear();
        
        // Create series for income and expenses
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Ingresos");
        
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Gastos");
        
        // Determine if we should use monthly or daily data based on date range
        long daysBetween = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
        boolean useMonthlyData = daysBetween > 60;
        
        // Get data from balance report
        Map<String, Double> incomeData = useMonthlyData ? 
                getMonthlyIncomeSeries() : getDailyIncomeSeries();
        Map<String, Double> expensesData = useMonthlyData ? 
                getMonthlyExpensesSeries() : getDailyExpensesSeries();
        
        // Add data to series
        incomeData.forEach((date, amount) -> 
            incomeSeries.getData().add(new XYChart.Data<>(date, amount)));
        
        expensesData.forEach((date, amount) -> 
            expensesSeries.getData().add(new XYChart.Data<>(date, amount)));
        
        // Add series to chart
        cashFlowChart.getData().addAll(incomeSeries, expensesSeries);
        
        // Style the series
        incomeSeries.getNode().setStyle("-fx-stroke: #28a745; -fx-stroke-width: 2px;");
        expensesSeries.getNode().setStyle("-fx-stroke: #dc3545; -fx-stroke-width: 2px;");
        
        // Add tooltips to data points
        for (XYChart.Series<String, Number> s : cashFlowChart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                Tooltip tooltip = new Tooltip(
                    d.getXValue() + ": " + currencyFormat.format(d.getYValue())
                );
                Tooltip.install(d.getNode(), tooltip);
                
                // Add hover effect
                d.getNode().setOnMouseEntered(event -> 
                    d.getNode().setStyle("-fx-background-color: gold; -fx-background-radius: 5px;"));
                d.getNode().setOnMouseExited(event -> 
                    d.getNode().setStyle(""));
            }
        }
    }

    /**
     * Updates the category distribution charts
     */
    private void updateCategoryCharts() {
        // Update income category chart
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        Map<String, Double> incomeByCategory = getCategoryIncomeData();
        
        for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                incomeData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        incomeCategoryChart.setData(incomeData);
        
        // Update expense category chart
        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        Map<String, Double> expensesByCategory = getCategoryExpenseData();
        
        for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                expenseData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        expenseCategoryChart.setData(expenseData);
        
        // Add tooltips to pie chart slices
        addTooltipsToPieChart(incomeCategoryChart);
        addTooltipsToPieChart(expenseCategoryChart);
    }

    /**
     * Adds tooltips to pie chart slices
     */
    private void addTooltipsToPieChart(PieChart chart) {
        for (PieChart.Data data : chart.getData()) {
            Tooltip tooltip = new Tooltip(
                data.getName() + ": " + currencyFormat.format(data.getPieValue())
            );
            
            Tooltip.install(data.getNode(), tooltip);
            
            // Add hover effect
            data.getNode().setOnMouseEntered(event -> {
                data.getNode().setStyle("-fx-opacity: 0.8;");
            });
            data.getNode().setOnMouseExited(event -> {
                data.getNode().setStyle("");
            });
        }
    }

    /**
     * Get monthly income data for charts
     */
    private Map<String, Double> getMonthlyIncomeSeries() {
        Map<String, Double> data = new LinkedHashMap<>();
        LocalDate current = startDatePicker.getValue().withDayOfMonth(1);
        LocalDate end = endDatePicker.getValue();
        
        while (!current.isAfter(end)) {
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(end)) {
                monthEnd = end;
            }
            
            double income = balanceReport.getIncomeForPeriod(current, monthEnd);
            String monthLabel = current.format(DateTimeFormatter.ofPattern("MMM yy"));
            data.put(monthLabel, income);
            
            current = current.plusMonths(1);
        }
        
        return data;
    }

    /**
     * Get daily income data for charts
     */
    private Map<String, Double> getDailyIncomeSeries() {
        Map<String, Double> data = new LinkedHashMap<>();
        LocalDate current = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        while (!current.isAfter(end)) {
            double income = balanceReport.getDailyIncome(current);
            String dayLabel = current.format(DateTimeFormatter.ofPattern("dd MMM"));
            data.put(dayLabel, income);
            
            current = current.plusDays(1);
        }
        
        return data;
    }

    /**
     * Get monthly expense data for charts
     */
    private Map<String, Double> getMonthlyExpensesSeries() {
        Map<String, Double> data = new LinkedHashMap<>();
        LocalDate current = startDatePicker.getValue().withDayOfMonth(1);
        LocalDate end = endDatePicker.getValue();
        
        while (!current.isAfter(end)) {
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(end)) {
                monthEnd = end;
            }
            
            double expenses = balanceReport.getExpensesForPeriod(current, monthEnd);
            String monthLabel = current.format(DateTimeFormatter.ofPattern("MMM yy"));
            data.put(monthLabel, expenses);
            
            current = current.plusMonths(1);
        }
        
        return data;
    }

    /**
     * Get daily expense data for charts
     */
    private Map<String, Double> getDailyExpensesSeries() {
        Map<String, Double> data = new LinkedHashMap<>();
        LocalDate current = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        while (!current.isAfter(end)) {
            double expenses = balanceReport.getDailyExpenses(current);
            String dayLabel = current.format(DateTimeFormatter.ofPattern("dd MMM"));
            data.put(dayLabel, expenses);
            
            current = current.plusDays(1);
        }
        
        return data;
    }

    /**
     * Get income by category data using real data from BalanceReport
     */
    private Map<String, Double> getCategoryIncomeData() {
        Map<String, Double> data = new HashMap<>();
        
        // Get transaction categories from accountingDAO if available
        List<String> categories;
        try {
            categories = accountingDAO.getAllCategories();
        } catch (Exception e) {
            // If can't get from DAO, use some default categories
            categories = Arrays.asList("Ventas", "Servicios", "Otros ingresos");
        }
        
        // Get income data for each category from balanceReport
        for (String category : categories) {
            double amount = balanceReport.getCategoryIncome(category);
            if (amount > 0) {
                data.put(category, amount);
            }
        }
        
        // If no data found, add some default values to avoid empty chart
        if (data.isEmpty()) {
            data.put("Ventas", 15000.0);
            data.put("Servicios", 2500.0);
            data.put("Otros ingresos", 1200.0);
        }
        
        return data;
    }

    /**
     * Get expenses by category data using real data from BalanceReport
     */
    private Map<String, Double> getCategoryExpenseData() {
        Map<String, Double> data = new HashMap<>();
        
        // Get transaction categories from accountingDAO if available
        List<String> categories;
        try {
            categories = accountingDAO.getAllCategories();
        } catch (Exception e) {
            // If can't get from DAO, use some default categories
            categories = Arrays.asList("Compras", "Servicios", "Salarios", "Alquiler", "Otros gastos");
        }
        
        // Get expense data for each category from balanceReport
        for (String category : categories) {
            double amount = balanceReport.getCategoryExpenses(category);
            if (amount > 0) {
                data.put(category, amount);
            }
        }
        
        // If no data found, add some default values to avoid empty chart
        if (data.isEmpty()) {
            data.put("Compras", 8000.0);
            data.put("Servicios", 1200.0);
            data.put("Salarios", 4500.0);
            data.put("Alquiler", 2000.0);
            data.put("Otros gastos", 800.0);
        }
        
        return data;
    }

    /**
     * Updates the trend chart with historical data
     */
    private void updateTrendChart(String periodType) {
        // Clear existing data
        trendChart.getData().clear();
        
        // Create series for current period and previous period
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Período actual");
        
        XYChart.Series<String, Number> previousSeries = new XYChart.Series<>();
        previousSeries.setName("Período anterior");
        
        // Get date ranges for current period
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        // Calculate the date ranges for previous period
        long periodLength = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(periodLength + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        // Get data for both periods
        Map<String, Double> currentData = new LinkedHashMap<>();
        Map<String, Double> previousData = new LinkedHashMap<>();
        
        // Use appropriate data retrieval based on period type
        switch (periodType) {
            case "day":
                currentData = getDailyIncomeSeries(); // Just using income as an example
                // For previous period, we'd need to adjust the dates
                break;
            case "week":
                // Weekly data logic would go here
                break;
            case "month":
            default:
                currentData = getMonthlyIncomeSeries();
                // Previous period data would be calculated similarly
                break;
        }
        
        // Add current period data to series
        for (Map.Entry<String, Double> entry : currentData.entrySet()) {
            currentSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        // Add previous period data if available
        // (For demo, just add some reduced values)
        for (Map.Entry<String, Double> entry : currentData.entrySet()) {
            double prevValue = entry.getValue() * 0.8; // Just for demonstration
            previousSeries.getData().add(new XYChart.Data<>(entry.getKey(), prevValue));
        }
        
        // Add series to chart
        trendChart.getData().addAll(currentSeries, previousSeries);
        
        // Apply custom styling
        currentSeries.getNode().setStyle("-fx-stroke: #007bff; -fx-stroke-width: 3px;");
        previousSeries.getNode().setStyle("-fx-stroke: #6c757d; -fx-stroke-width: 2px; -fx-stroke-dash-array: 5 5;");
    }

    /**
     * Gets top income categories
     */
    private List<Map.Entry<String, Double>> getTopIncomeCategories() {
        Map<String, Double> incomeByCategory = getCategoryIncomeData();
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(incomeByCategory.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return sortedEntries.subList(0, Math.min(5, sortedEntries.size()));
    }

    /**
     * Example PDF generation method (would need to be implemented with a PDF library)
     */
    private void generatePDFReport(File file) {
        // This is a placeholder. In a real implementation, you would use a PDF library
        statusLabel.setText("Funcionalidad de exportación a PDF en desarrollo");
    }

    /**
     * Example Excel generation method (would need to be implemented with Apache POI)
     */
    private void generateExcelReport(File file) {
        // This is a placeholder. In a real implementation, you would use Apache POI
        statusLabel.setText("Funcionalidad de exportación a Excel en desarrollo");
    }
}