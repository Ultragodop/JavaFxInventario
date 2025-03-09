package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.Transaction;
import com.minimercado.javafxinventario.DAO.AccountingDAO;
import com.minimercado.javafxinventario.modules.AccountingEntry;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.FileOutputStream;
import javafx.util.StringConverter;

public class ContabilidadController {
    @FXML private Label lblTotalVentas;
    @FXML private Label lblMargenGanancia;
    @FXML private Label lblCostosOperativos;
    @FXML private ComboBox<String> filtroCombo;
    @FXML private TextField searchTransField;
    @FXML private TableView<Transaction> transaccionesTable;
    @FXML private TableColumn<Transaction, String> colTipo;
    @FXML private TableColumn<Transaction, String> colMonto;
    @FXML private TableColumn<Transaction, String> colDescripcion;
    @FXML private TableColumn<Transaction, String> colFecha;
    @FXML private TextArea auditArea;

    // Add FXML field declarations for the new Libro Mayor tab components
    @FXML private ComboBox<String> accountComboBox;
    @FXML private DatePicker ledgerStartDatePicker;
    @FXML private DatePicker ledgerEndDatePicker;
    @FXML private TableView<Map<String, Object>> journalEntriesTable;
    @FXML private TableColumn<Map<String, Object>, LocalDate> entryDateColumn;
    @FXML private TableColumn<Map<String, Object>, String> referenceColumn;
    @FXML private TableColumn<Map<String, Object>, String> entryDescriptionColumn;
    @FXML private TableColumn<Map<String, Object>, Double> entryDebitColumn;
    @FXML private TableColumn<Map<String, Object>, Double> entryCreditColumn;
    @FXML private Label journalStatusLabel;

    // Add FXML field declarations for the new Asiento Contable tab components
    @FXML private DatePicker entryDatePicker;
    @FXML private TextField referenceField;
    @FXML private TextField descriptionField;
    @FXML private VBox lineItemsContainer;
    @FXML private Label newEntryStatusLabel;

    // Se usa la instancia singleton
    private final AccountingModule accountingModule = AccountingModule.getInstance();
    private final ObservableList<Transaction> transaccionesList = FXCollections.observableArrayList();
    
    // Integration with the new AccountingDAO
    private final AccountingDAO accountingDAO = new AccountingDAO();

    @FXML
    public void initialize() {
        // Clear any existing items first
        filtroCombo.getItems().clear();
        
        // Create string converter to handle string values properly
        StringConverter<String> stringConverter = new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };
        
        // Set converter
        filtroCombo.setConverter(stringConverter);
        
        // Add items after setting converter
        filtroCombo.setItems(FXCollections.observableArrayList("Diario", "Semanal", "Mensual"));
        
        // Set default selection
        filtroCombo.setValue("Diario");
        
        // Setup table and listeners
        configurarColumnas();
        transaccionesTable.setItems(transaccionesList);
        filtroCombo.valueProperty().addListener((obs, oldVal, newVal) -> actualizarTransacciones(newVal));
        searchTransField.textProperty().addListener((obs, oldVal, newVal) -> filtrarTransaccionesDescripcion(newVal));
        
        // Initial data load
        actualizarTransacciones(filtroCombo.getValue());
        actualizarResumenFinanciero();
        actualizarAreaAuditoria();
        
        // Load transactions from DB using our new DAO
        cargarTransaccionesDesdeDB();

        // Initialize the date pickers with reasonable defaults
        if (ledgerStartDatePicker != null) {
            ledgerStartDatePicker.setValue(LocalDate.now().minusMonths(1));
        }
        if (ledgerEndDatePicker != null) {
            ledgerEndDatePicker.setValue(LocalDate.now());
        }
        if (entryDatePicker != null) {
            entryDatePicker.setValue(LocalDate.now());
        }
        
        // Set up account combo box
        setupAccountComboBox();
        
        // Set up journal entries table
        setupJournalEntriesTable();
    }

    // New method to load transactions from database
    private void cargarTransaccionesDesdeDB() {
        try {
            LocalDate startDate = LocalDate.now().minusDays(30); // Default to last 30 days
            LocalDate endDate = LocalDate.now();
            
            List<Transaction> dbTransactions = accountingDAO.getTransactionsByDateRange(startDate, endDate);
            
            // Merge with existing transactions if needed
            if (!dbTransactions.isEmpty()) {
                // Add to accounting module if not already there
                for (Transaction tx : dbTransactions) {
                    if (!accountingModule.transactionExists(tx)) {
                        accountingModule.getTransactions().add(tx);
                    }
                }
                
                actualizarResumenFinanciero();
                actualizarTransacciones(filtroCombo.getValue());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar transacciones desde DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarColumnas() {
        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        colMonto.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.2f", cell.getValue().getAmount())));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTimestamp().toString()));   
    }

    private void actualizarResumenFinanciero() {
        // Get financial summary from AccountingDAO for more accurate data
        try {
            LocalDate today = LocalDate.now();
            LocalDate firstOfMonth = today.withDayOfMonth(1);
            
            Map<String, Double> incomeStatement = accountingDAO.getIncomeStatement(firstOfMonth, today);
            double totalVentas = incomeStatement.get("totalRevenue");
            double costos = incomeStatement.get("totalExpenses");
            double margen = incomeStatement.get("netIncome");
            
            lblTotalVentas.setText(String.format("%.2f", totalVentas));
            lblCostosOperativos.setText(String.format("%.2f", costos));
            lblMargenGanancia.setText(String.format("%.2f", margen));
        } catch (Exception e) {
            System.err.println("Error obteniendo datos financieros: " + e.getMessage());
            // Fallback to original method if DB access fails
            double totalVentas = accountingModule.getTransactions().stream()
                    .filter(tx -> "venta".equalsIgnoreCase(tx.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double costos = accountingModule.getTransactions().stream()
                    .filter(tx -> "egreso".equalsIgnoreCase(tx.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            double margen = totalVentas - costos;
            lblTotalVentas.setText(String.format("%.2f", totalVentas));
            lblCostosOperativos.setText(String.format("%.2f", costos));
            lblMargenGanancia.setText(String.format("%.2f", margen));
        }
    }

    private void actualizarTransacciones(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            filtro = "Diario";
        }
        List<Transaction> filtradas = filtrarPorTiempo(accountingModule.getTransactions(), filtro);
        transaccionesList.setAll(filtradas);
        actualizarResumenFinanciero();
    }

    private List<Transaction> filtrarPorTiempo(List<Transaction> transacciones, String filtro) {
        LocalDateTime ahora = LocalDateTime.now();
        return transacciones.stream().filter(tx -> {
            long dias = ChronoUnit.DAYS.between(tx.getTimestamp(), ahora);
            switch (filtro) {
                case "Semanal": return dias < 7;
                case "Mensual": return dias < 30;
                case "Diario":
                default:        return dias < 1;
            }
        }).collect(Collectors.toList());
    }

    private void filtrarTransaccionesDescripcion(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            actualizarTransacciones(filtroCombo.getValue());
            return;
        }
        transaccionesList.setAll(
            accountingModule.getTransactions().stream()
            .filter(tx -> tx.getDescription().toLowerCase().contains(busqueda.toLowerCase()))
            .collect(Collectors.toList())
        );
    }

    private void actualizarAreaAuditoria() {
        String registroAuditoria = accountingModule.getAuditLog();
        auditArea.setText(registroAuditoria);
    }

    @FXML
    private void exportarCSV(ActionEvent event) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("transacciones.csv"))) {
            writer.println("Tipo,Monto,Descripción,Fecha/Hora");
            for (Transaction tx : accountingModule.getTransactions()) {
                writer.println(tx.getType() + "," +
                               tx.getAmount() + "," +
                               tx.getDescription() + "," +
                               tx.getTimestamp());
            }
            mostrarAlerta("Exportación CSV", "Reporte exportado a transacciones.csv");
        } catch (Exception e) {
            mostrarAlerta("Error CSV", e.getMessage());
        }
    }

    @FXML
    private void exportarPDF(ActionEvent event) {
        try {
            Document documento = new Document();
            PdfWriter.getInstance(documento, new java.io.FileOutputStream("transacciones.pdf"));
            documento.open();
            documento.add(new Paragraph("Reporte de Transacciones\n\n"));
            for (Transaction tx : accountingModule.getTransactions()) {
                documento.add(new Paragraph(
                    "Tipo: " + tx.getType() + " | Monto: " + tx.getAmount() +
                    " | Descripción: " + tx.getDescription() +
                    " | Fecha: " + tx.getTimestamp()
                ));
            }
            documento.close();
            mostrarAlerta("Exportación PDF", "Reporte exportado a transacciones.pdf");
        } catch (Exception e) {
            mostrarAlerta("Error PDF", e.getMessage());
        }
    }

    @FXML
    private void exportarExcel(ActionEvent event) {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Transacciones");
            Row cabecera = hoja.createRow(0);
            cabecera.createCell(0).setCellValue("Tipo");
            cabecera.createCell(1).setCellValue("Monto");
            cabecera.createCell(2).setCellValue("Descripción");
            cabecera.createCell(3).setCellValue("Fecha/Hora");

            int filaNum = 1;
            for (Transaction tx : accountingModule.getTransactions()) {
                Row fila = hoja.createRow(filaNum++);
                fila.createCell(0).setCellValue(tx.getType());
                fila.createCell(1).setCellValue(tx.getAmount());
                fila.createCell(2).setCellValue(tx.getDescription());
                fila.createCell(3).setCellValue(tx.getTimestamp().toString());
            }
            try (FileOutputStream fos = new FileOutputStream("transacciones.xlsx")) {
                libro.write(fos);
            }
            mostrarAlerta("Exportación Excel", "Reporte exportado a transacciones.xlsx");
        } catch (Exception e) {
            mostrarAlerta("Error Excel", e.getMessage());
        }
    }

    @FXML
    private void exportarReporteCompleto(ActionEvent event) {
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream("reporte_completo.xlsx")) {
            // Create workbook with multiple sheets using AccountingDAO for detailed reports
            XSSFWorkbook workbook = new XSSFWorkbook();
            
            // Add transactions sheet
            Sheet transSheet = workbook.createSheet("Transacciones");
            Row headerRow = transSheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Tipo");
            headerRow.createCell(2).setCellValue("Fecha");
            headerRow.createCell(3).setCellValue("Monto");
            headerRow.createCell(4).setCellValue("Descripción");
            
            List<Transaction> transactions = accountingDAO.getTransactions();
            for (int i = 0; i < transactions.size(); i++) {
                Transaction tx = transactions.get(i);
                Row row = transSheet.createRow(i + 1);
                row.createCell(0).setCellValue(tx.getId());
                row.createCell(1).setCellValue(tx.getType());
                row.createCell(2).setCellValue(tx.getTimestamp().toString());
                row.createCell(3).setCellValue(tx.getAmount());
                row.createCell(4).setCellValue(tx.getDescription());
            }
            
            // Add income statement sheet
            Sheet incomeSheet = workbook.createSheet("Estado de Resultados");
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();
            Map<String, Double> incomeData = accountingDAO.getIncomeStatement(startDate, endDate);
            
            Row incomeHeader = incomeSheet.createRow(0);
            incomeHeader.createCell(0).setCellValue("Estado de Resultados: " + 
                startDate.format(DateTimeFormatter.ISO_DATE) + " al " + 
                endDate.format(DateTimeFormatter.ISO_DATE));
            
            Row revenueRow = incomeSheet.createRow(2);
            revenueRow.createCell(0).setCellValue("Ingresos");
            revenueRow.createCell(1).setCellValue(incomeData.get("totalRevenue"));
            
            Row expensesRow = incomeSheet.createRow(3);
            expensesRow.createCell(0).setCellValue("Gastos");
            expensesRow.createCell(1).setCellValue(incomeData.get("totalExpenses"));
            
            Row netIncomeRow = incomeSheet.createRow(5);
            netIncomeRow.createCell(0).setCellValue("Ganancia Neta");
            netIncomeRow.createCell(1).setCellValue(incomeData.get("netIncome"));
            
            // Add accounts sheet with balances
            Sheet accountsSheet = workbook.createSheet("Cuentas");
            Row acctHeader = accountsSheet.createRow(0);
            acctHeader.createCell(0).setCellValue("Código");
            acctHeader.createCell(1).setCellValue("Nombre");
            acctHeader.createCell(2).setCellValue("Tipo");
            acctHeader.createCell(3).setCellValue("Saldo");
            
            List<Map<String, Object>> accounts = accountingDAO.getAllAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                Map<String, Object> account = accounts.get(i);
                Row row = accountsSheet.createRow(i + 1);
                String code = (String) account.get("accountCode");
                row.createCell(0).setCellValue(code);
                row.createCell(1).setCellValue((String) account.get("name"));
                row.createCell(2).setCellValue((String) account.get("accountType"));
                
                // Get account balance
                double balance = accountingDAO.getAccountBalance(code, endDate);
                row.createCell(3).setCellValue(balance);
            }
            
            workbook.write(fos);
            mostrarAlerta("Exportación Reporte", "Reporte completo exportado a reporte_completo.xlsx");
        } catch (Exception e) {
            mostrarAlerta("Error Reporte", e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/main-menu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAccountComboBox() {
        if (accountComboBox != null) {
            try {
                List<Map<String, Object>> accounts = accountingDAO.getAllAccounts();
                ObservableList<String> accountOptions = FXCollections.observableArrayList();
                
                for (Map<String, Object> account : accounts) {
                    String code = (String) account.get("accountCode");
                    String name = (String) account.get("name");
                    accountOptions.add(code + " - " + name);
                }
                
                accountComboBox.setItems(accountOptions);
                if (!accountOptions.isEmpty()) {
                    accountComboBox.setValue(accountOptions.get(0));
                }
            } catch (Exception e) {
                System.err.println("Error loading accounts: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setupJournalEntriesTable() {
        if (journalEntriesTable != null) {
            // Configure cell value factories
            entryDateColumn.setCellValueFactory(cellData -> 
                new SimpleObjectProperty<>((LocalDate) cellData.getValue().get("entryDate")));
            referenceColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty((String) cellData.getValue().get("reference")));
            entryDescriptionColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty((String) cellData.getValue().get("lineDescription")));
            entryDebitColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty((Double) cellData.getValue().get("debit")).asObject());
            entryCreditColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty((Double) cellData.getValue().get("credit")).asObject());
        }
    }

    // Add the missing event handler methods

    /**
     * Handler for searching ledger entries
     */
    @FXML
    private void handleSearchLedger() {
        if (accountComboBox == null || ledgerStartDatePicker == null || ledgerEndDatePicker == null) {
            System.err.println("UI components not initialized");
            return;
        }
        
        try {
            String selectedAccount = accountComboBox.getValue();
            if (selectedAccount == null || selectedAccount.isEmpty()) {
                journalStatusLabel.setText("Seleccione una cuenta");
                return;
            }
            
            LocalDate startDate = ledgerStartDatePicker.getValue();
            LocalDate endDate = ledgerEndDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                journalStatusLabel.setText("Seleccione fechas válidas");
                return;
            }
            
            // Extract account code from the selected value (format is "code - name")
            String accountCode = selectedAccount.split(" - ")[0];
            
            // Get ledger entries from DAO
            List<Map<String, Object>> entries = accountingDAO.getLedgerEntries(accountCode, startDate, endDate);
            
            // Set entries to table
            journalEntriesTable.setItems(FXCollections.observableArrayList(entries));
            
            if (entries.isEmpty()) {
                journalStatusLabel.setText("No se encontraron registros para el período seleccionado");
            } else {
                // Calculate balance
                double balance = accountingDAO.getAccountBalance(accountCode, endDate);
                journalStatusLabel.setText("Balance: " + String.format("%.2f", balance));
            }
        } catch (Exception e) {
            journalStatusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for adding a new line item to the accounting entry form
     */
    @FXML
    private void handleAddLineItem() {
        if (lineItemsContainer == null) {
            System.err.println("Line items container not initialized");
            return;
        }
        
        // Create a new line item row in the form
        lineItemsContainer.getChildren().add(createLineItemRow());
    }

    /**
     * Creates a new line item row for accounting entries
     */
    private GridPane createLineItemRow() {
        GridPane row = new GridPane();
        row.setHgap(10);
        row.setVgap(5);
        
        // Account combobox
        ComboBox<String> accountCombo = new ComboBox<>();
        accountCombo.setItems(accountComboBox.getItems());
        accountCombo.setPrefWidth(200);
        
        // Description field
        TextField descField = new TextField();
        descField.setPromptText("Descripción");
        descField.setPrefWidth(200);
        
        // Debit amount field
        TextField debitField = new TextField();
        debitField.setPromptText("Débito");
        debitField.setPrefWidth(100);
        
        // Credit amount field
        TextField creditField = new TextField();
        creditField.setPromptText("Crédito");
        creditField.setPrefWidth(100);
        
        // Remove button
        Button removeButton = new Button("X");
        removeButton.setStyle("-fx-background-color: #ff6666;");
        removeButton.setOnAction(e -> lineItemsContainer.getChildren().remove(row));
        
        row.add(accountCombo, 0, 0);
        row.add(descField, 1, 0);
        row.add(debitField, 2, 0);
        row.add(creditField, 3, 0);
        row.add(removeButton, 4, 0);
        
        return row;
    }

    /**
     * Handler for saving a new accounting entry
     */
    @FXML
    private void handleSaveEntry() {
        if (entryDatePicker == null || referenceField == null || descriptionField == null || lineItemsContainer == null) {
            System.err.println("UI components not initialized");
            return;
        }
        
        try {
            LocalDate entryDate = entryDatePicker.getValue();
            String reference = referenceField.getText().trim();
            String description = descriptionField.getText().trim();
            
            if (entryDate == null || reference.isEmpty() || description.isEmpty()) {
                newEntryStatusLabel.setText("Complete todos los campos obligatorios");
                return;
            }
            
            AccountingEntry entry = new AccountingEntry(entryDate, reference, description);
            entry.setCreatedBy("Usuario");
            
            double totalDebits = 0;
            double totalCredits = 0;
            
            // Process each line item
            for (int i = 0; i < lineItemsContainer.getChildren().size(); i++) {
                if (!(lineItemsContainer.getChildren().get(i) instanceof GridPane)) continue;
                
                GridPane row = (GridPane)lineItemsContainer.getChildren().get(i);
                
                ComboBox<?> accountCombo = null;
                TextField descField = null;
                TextField debitField = null;
                TextField creditField = null;
                
                // Find the proper components in the row
                for (javafx.scene.Node node : row.getChildren()) {
                    if (node instanceof ComboBox) {
                        accountCombo = (ComboBox<?>)node;
                    } else if (node instanceof TextField) {
                        if (node.getProperties().containsKey("type")) {
                            String type = (String)node.getProperties().get("type");
                            if ("description".equals(type)) {
                                descField = (TextField)node;
                            } else if ("debit".equals(type)) {
                                debitField = (TextField)node;
                            } else if ("credit".equals(type)) {
                                creditField = (TextField)node;
                            }
                        } else if (descField == null) {
                            descField = (TextField)node;
                        } else if (debitField == null) {
                            debitField = (TextField)node;
                        } else if (creditField == null) {
                            creditField = (TextField)node;
                        }
                    }
                }
                
                if (accountCombo == null || descField == null || debitField == null || creditField == null) {
                    continue; // Skip incomplete rows
                }
                
                String accountValue = accountCombo.getValue() != null ? accountCombo.getValue().toString() : null;
                if (accountValue == null || accountValue.isEmpty()) {
                    continue;
                }
                
                String accountCode = accountValue.split(" - ")[0];
                String lineDesc = descField.getText().trim();
                
                double debitAmount = 0;
                if (!debitField.getText().isEmpty()) {
                    debitAmount = Double.parseDouble(debitField.getText().replace(',', '.'));
                }
                
                double creditAmount = 0;
                if (!creditField.getText().isEmpty()) {
                    creditAmount = Double.parseDouble(creditField.getText().replace(',', '.'));
                }
                
                if (debitAmount > 0 || creditAmount > 0) {
                    entry.addLineItem(accountCode, lineDesc, debitAmount, creditAmount);
                    totalDebits += debitAmount;
                    totalCredits += creditAmount;
                }
            }
            
            // Check if entry is balanced
            if (Math.abs(totalDebits - totalCredits) > 0.01) {
                newEntryStatusLabel.setText("Error: Los débitos y créditos no están balanceados");
                return;
            }
            
            // Post the entry
            if (entry.post()) {
                newEntryStatusLabel.setText("Asiento contable creado con éxito");
                clearEntryForm();
            } else {
                newEntryStatusLabel.setText("Error al crear el asiento contable");
            }
        } catch (Exception e) {
            newEntryStatusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears the accounting entry form
     */
    private void clearEntryForm() {
        entryDatePicker.setValue(LocalDate.now());
        referenceField.clear();
        descriptionField.clear();
        lineItemsContainer.getChildren().clear();
        
        // Add a default empty row
        handleAddLineItem();
    }
}
