package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.AccountingDAO;
import com.minimercado.javafxinventario.modules.AccountingEntry;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.Transaction;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AccountingController implements Initializable {

    @FXML private TabPane mainTabPane;
    
    // Transactions tab components
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionIdColumn;
    @FXML private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML private TableColumn<Transaction, LocalDateTime> transactionDateColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button searchTransactionsButton;
    @FXML private Label transactionStatusLabel;
    
    // Journal Entries tab components
    @FXML private TableView<Map<String, Object>> journalEntriesTable;
    @FXML private TableColumn<Map<String, Object>, LocalDate> entryDateColumn;
    @FXML private TableColumn<Map<String, Object>, String> referenceColumn;
    @FXML private TableColumn<Map<String, Object>, String> entryDescriptionColumn;
    @FXML private TableColumn<Map<String, Object>, Double> entryDebitColumn;
    @FXML private TableColumn<Map<String, Object>, Double> entryCreditColumn;
    @FXML private ComboBox<String> accountComboBox;
    @FXML private DatePicker ledgerStartDatePicker;
    @FXML private DatePicker ledgerEndDatePicker;
    @FXML private Button searchLedgerButton;
    @FXML private Label journalStatusLabel;
    
    // Reports tab components
    @FXML private DatePicker reportStartDatePicker;
    @FXML private DatePicker reportEndDatePicker;
    @FXML private Button generateReportButton;
    @FXML private VBox reportResultsBox;
    @FXML private Label revenueValueLabel;
    @FXML private Label expensesValueLabel;
    @FXML private Label netIncomeValueLabel;
    
    // New Entry tab components
    @FXML private DatePicker entryDatePicker;
    @FXML private TextField referenceField;
    @FXML private TextField descriptionField;
    @FXML private VBox lineItemsContainer;
    @FXML private Button addLineItemButton;
    @FXML private Button saveEntryButton;
    @FXML private Label newEntryStatusLabel;
    
    private AccountingDAO accountingDAO = new AccountingDAO();
    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();
    private Map<String, String> accountMap = new HashMap<>(); // For account code to name mapping
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the transactions table
        setupTransactionsTable();
        
        // Set up the journal entries table
        setupJournalEntriesTable();
        
        // Initialize date pickers
        initDatePickers();
        
        // Load accounts into the combo box
        loadAccounts();
        
        // Set default values
        setDefaultDateRanges();
        
        // Load initial data
        loadTransactions();
    }
    
    private void setupTransactionsTable() {
        transactionIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        transactionTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        transactionDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTimestamp()));
        transactionAmountColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        transactionDescriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        // Format the date column
        transactionDateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        
        // Format the amount column with 2 decimal places
        transactionAmountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    // Color negative values in red
                    if (item < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
        
        transactionsTable.setItems(transactionsList);
    }
    
    private void setupJournalEntriesTable() {
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
            
        // Format the debit and credit columns with 2 decimal places
        entryDebitColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        
        entryCreditColumn.setCellFactory(column -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }
    
    private void initDatePickers() {
        // Set converters for date pickers
        StringConverter<LocalDate> dateConverter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }
            
            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        };
        
        startDatePicker.setConverter(dateConverter);
        endDatePicker.setConverter(dateConverter);
        ledgerStartDatePicker.setConverter(dateConverter);
        ledgerEndDatePicker.setConverter(dateConverter);
        reportStartDatePicker.setConverter(dateConverter);
        reportEndDatePicker.setConverter(dateConverter);
        entryDatePicker.setConverter(dateConverter);
        
        // Set date picker prompts
        startDatePicker.setPromptText("Fecha inicio");
        endDatePicker.setPromptText("Fecha fin");
        ledgerStartDatePicker.setPromptText("Fecha inicio");
        ledgerEndDatePicker.setPromptText("Fecha fin");
        reportStartDatePicker.setPromptText("Fecha inicio");
        reportEndDatePicker.setPromptText("Fecha fin");
        entryDatePicker.setPromptText("Fecha de asiento");
    }
    
    private void loadAccounts() {
        try {
            List<Map<String, Object>> accounts = accountingDAO.getAllAccounts();
            ObservableList<String> accountCodes = FXCollections.observableArrayList();
            
            for (Map<String, Object> account : accounts) {
                String code = (String) account.get("accountCode");
                String name = (String) account.get("name");
                String displayText = code + " - " + name;
                
                accountCodes.add(displayText);
                accountMap.put(displayText, code);
            }
            
            accountComboBox.setItems(accountCodes);
            
            // Select the first account by default if available
            if (!accountCodes.isEmpty()) {
                accountComboBox.getSelectionModel().selectFirst();
            }
            
        } catch (Exception e) {
            journalStatusLabel.setText("Error al cargar cuentas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setDefaultDateRanges() {
        // Set default date ranges to current month
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        
        startDatePicker.setValue(firstOfMonth);
        endDatePicker.setValue(now);
        
        ledgerStartDatePicker.setValue(firstOfMonth);
        ledgerEndDatePicker.setValue(now);
        
        reportStartDatePicker.setValue(firstOfMonth);
        reportEndDatePicker.setValue(now);
        
        entryDatePicker.setValue(now);
    }
    
    private void loadTransactions() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                transactionStatusLabel.setText("Por favor seleccione un rango de fechas válido");
                return;
            }
            
            List<Transaction> transactions = accountingDAO.getTransactionsByDateRange(startDate, endDate);
            transactionsList.setAll(transactions);
            
            transactionStatusLabel.setText("Se encontraron " + transactions.size() + " transacciones");
            
        } catch (Exception e) {
            transactionStatusLabel.setText("Error al cargar transacciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearchTransactions() {
        loadTransactions();
    }
    
    @FXML
    private void handleSearchLedger() {
        try {
            LocalDate startDate = ledgerStartDatePicker.getValue();
            LocalDate endDate = ledgerEndDatePicker.getValue();
            String selectedAccountItem = accountComboBox.getValue();
            
            if (startDate == null || endDate == null || selectedAccountItem == null) {
                journalStatusLabel.setText("Por favor complete todos los campos");
                return;
            }
            
            String accountCode = accountMap.get(selectedAccountItem);
            if (accountCode == null) {
                journalStatusLabel.setText("Cuenta no válida");
                return;
            }
            
            List<Map<String, Object>> entries = accountingDAO.getLedgerEntries(accountCode, startDate, endDate);
            journalEntriesTable.setItems(FXCollections.observableArrayList(entries));
            
            // Calculate and display the balance
            double balance = accountingDAO.getAccountBalance(accountCode, endDate);
            journalStatusLabel.setText("Balance de la cuenta: " + String.format("%.2f", balance));
            
        } catch (Exception e) {
            journalStatusLabel.setText("Error al buscar asientos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGenerateReport() {
        try {
            LocalDate startDate = reportStartDatePicker.getValue();
            LocalDate endDate = reportEndDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                journalStatusLabel.setText("Por favor seleccione un rango de fechas válido");
                return;
            }
            
            Map<String, Double> incomeStatement = accountingDAO.getIncomeStatement(startDate, endDate);
            
            double revenue = incomeStatement.get("totalRevenue");
            double expenses = incomeStatement.get("totalExpenses");
            double netIncome = incomeStatement.get("netIncome");
            
            // Update the UI labels
            revenueValueLabel.setText(String.format("%.2f", revenue));
            expensesValueLabel.setText(String.format("%.2f", expenses));
            netIncomeValueLabel.setText(String.format("%.2f", netIncome));
            
            // Style the net income based on whether it's positive or negative
            if (netIncome >= 0) {
                netIncomeValueLabel.setStyle("-fx-text-fill: green;");
            } else {
                netIncomeValueLabel.setStyle("-fx-text-fill: red;");
            }
            
            // Show the report results box
            reportResultsBox.setVisible(true);
            
        } catch (Exception e) {
            journalStatusLabel.setText("Error al generar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddLineItem() {
        // Create a new line item row in the form
        GridPane lineItemRow = createLineItemRow();
        lineItemsContainer.getChildren().add(lineItemRow);
    }
    
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
    
    @FXML
    private void handleSaveEntry() {
        try {
            LocalDate entryDate = entryDatePicker.getValue();
            String reference = referenceField.getText().trim();
            String description = descriptionField.getText().trim();
            
            if (entryDate == null || reference.isEmpty() || description.isEmpty()) {
                newEntryStatusLabel.setText("Por favor complete los campos obligatorios");
                return;
            }
            
            AccountingEntry entry = new AccountingEntry(entryDate, reference, description);
            entry.setCreatedBy("Usuario");
            
            double totalDebits = 0;
            double totalCredits = 0;
            
            // Process each line item
            for (int i = 0; i < lineItemsContainer.getChildren().size(); i++) {
                GridPane row = (GridPane) lineItemsContainer.getChildren().get(i);
                
                ComboBox<String> accountCombo = (ComboBox<String>) row.getChildren().get(0);
                TextField descField = (TextField) row.getChildren().get(1);
                TextField debitField = (TextField) row.getChildren().get(2);
                TextField creditField = (TextField) row.getChildren().get(3);
                
                String accountDisplay = accountCombo.getValue();
                String lineDescription = descField.getText().trim();
                
                if (accountDisplay == null || lineDescription.isEmpty()) {
                    continue; // Skip incomplete line items
                }
                
                String accountCode = accountMap.get(accountDisplay);
                
                double debitAmount = 0;
                double creditAmount = 0;
                
                if (!debitField.getText().trim().isEmpty()) {
                    debitAmount = Double.parseDouble(debitField.getText().trim().replace(",", "."));
                }
                
                if (!creditField.getText().trim().isEmpty()) {
                    creditAmount = Double.parseDouble(creditField.getText().trim().replace(",", "."));
                }
                
                if (debitAmount > 0 || creditAmount > 0) {
                    entry.addLineItem(accountCode, lineDescription, debitAmount, creditAmount);
                    totalDebits += debitAmount;
                    totalCredits += creditAmount;
                }
            }
            
            // Validate the entry
            if (entry.getLineItems().isEmpty()) {
                newEntryStatusLabel.setText("Ingrese al menos una línea de asiento");
                return;
            }
            
            if (Math.abs(totalDebits - totalCredits) > 0.01) {
                newEntryStatusLabel.setText("Error: Los débitos y créditos no están balanceados");
                return;
            }
            
            // Save the entry (this would typically call the DAO)
            // For now, just post the entry
            boolean posted = entry.post();
            
            if (posted) {
                newEntryStatusLabel.setText("Asiento creado y publicado correctamente");
                clearNewEntryForm();
            } else {
                newEntryStatusLabel.setText("Error al publicar el asiento: no está balanceado");
            }
            
        } catch (NumberFormatException e) {
            newEntryStatusLabel.setText("Error en formato de números: " + e.getMessage());
        } catch (Exception e) {
            newEntryStatusLabel.setText("Error al guardar asiento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearNewEntryForm() {
        entryDatePicker.setValue(LocalDate.now());
        referenceField.clear();
        descriptionField.clear();
        lineItemsContainer.getChildren().clear();
        
        // Add an initial empty line item
        handleAddLineItem();
    }

    /**
     * Handler for creating a new transaction
     */
    @FXML
    private void handleCreateTransaction() {
        try {
            // Create a dialog to input transaction details
            Dialog<Transaction> dialog = new Dialog<>();
            dialog.setTitle("Nueva Transacción");
            dialog.setHeaderText("Ingrese los detalles de la transacción");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            ComboBox<String> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll("venta", "compra", "gasto", "ingreso");
            typeCombo.setValue("venta");
            
            TextField amountField = new TextField();
            amountField.setPromptText("Monto");
            
            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Descripción");
            
            grid.add(new Label("Tipo:"), 0, 0);
            grid.add(typeCombo, 1, 0);
            grid.add(new Label("Monto:"), 0, 1);
            grid.add(amountField, 1, 1);
            grid.add(new Label("Descripción:"), 0, 2);
            grid.add(descriptionField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on amount field by default
            Platform.runLater(() -> amountField.requestFocus());
            
            // Convert the result to a transaction when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String type = typeCombo.getValue();
                        double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                        
                        // Adjust amount sign based on transaction type
                        if (type.equals("compra") || type.equals("gasto")) {
                            amount = -Math.abs(amount); // Ensure it's negative
                        } else {
                            amount = Math.abs(amount); // Ensure it's positive
                        }
                        
                        String description = descriptionField.getText();
                        
                        // Create and return the transaction
                        Transaction transaction = new Transaction();
                        transaction.setType(type);
                        transaction.setAmount(amount);
                        transaction.setDescription(description);
                        transaction.setTimestamp(LocalDateTime.now());
                        
                        return transaction;
                    } catch (NumberFormatException e) {
                        // Show error alert
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Error en el formato del monto");
                        alert.setContentText("Por favor ingrese un número válido para el monto.");
                        alert.showAndWait();
                        return null;
                    }
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<Transaction> result = dialog.showAndWait();
            
            result.ifPresent(transaction -> {
                try {
                    // Save the transaction to the database
                    boolean saved = accountingDAO.insertTransaction(transaction);
                    
                    if (saved) {
                        // Add to the list and refresh the view
                        transactionsList.add(transaction);
                        
                        // Notify the AccountingModule about the new transaction
                        AccountingModule.getInstance().addTransaction(transaction);
                        
                        // Show success message
                        journalStatusLabel.setText("Transacción guardada correctamente");
                        
                        // Refresh data
                        loadTransactions();
                    } else {
                        journalStatusLabel.setText("Error al guardar la transacción");
                    }
                } catch (Exception e) {
                    journalStatusLabel.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            journalStatusLabel.setText("Error al crear transacción: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to financial reports view
     */
    @FXML
    private void goToFinancialReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/minimercado/javafxinventario/views/financial-reports-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la vista de reportes financieros: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
