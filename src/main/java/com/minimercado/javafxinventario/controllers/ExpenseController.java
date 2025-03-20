package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.AccountingDAO;
import com.minimercado.javafxinventario.DAO.ExpenseDAO;
import com.minimercado.javafxinventario.modules.AccountingModule;
import com.minimercado.javafxinventario.modules.Expense;
import com.minimercado.javafxinventario.modules.FinancialUpdateListener;
import com.minimercado.javafxinventario.modules.Transaction;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.awt.Desktop;

/**
 * Controller for expense management with transaction integration
 */
public class ExpenseController implements Initializable, FinancialUpdateListener {

    // Tabla de gastos
    @FXML private TableView<Expense> expenseTable; // Corregido: expensesTable → expenseTable
    @FXML private TableColumn<Expense, String> idColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, String> paymentMethodColumn;
    @FXML private TableColumn<Expense, String> vendorColumn;
    @FXML private TableColumn<Expense, String> statusColumn;

    // Formulario de gastos
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionArea; // Ya tiene @FXML
    @FXML private DatePicker expenseDatePicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField receiptNumberField;
    @FXML private ComboBox<String> accountCodeComboBox;
    @FXML private TextField vendorNameField;
    @FXML private CheckBox taxDeductibleCheckBox;
    @FXML private TextArea notesArea; // Ya tiene @FXML
    
    // Controles de filtro
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> categoryFilterComboBox; 
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    
    // Botones de acción
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;
    @FXML private Button exportButton;
    @FXML private Button attachReceiptButton;
    @FXML private Button viewReceiptButton;
    
    // Estado
    @FXML private Label statusLabel; // Ya tiene @FXML
    @FXML private Label totalExpensesLabel;
    
    // DAOs and data
    private ExpenseDAO expenseDAO;
    private AccountingDAO accountingDAO;
    private ObservableList<Expense> expensesList = FXCollections.observableArrayList();
    private Expense currentExpense;
    
    // For accounting integration
    private AccountingModule accountingModule;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize DAOs
        expenseDAO = new ExpenseDAO();
        accountingDAO = new AccountingDAO();
        
        // Initialize accounting module and register as listener
        accountingModule = AccountingModule.getInstance();
        accountingModule.addFinancialUpdateListener(this);
        
        // Setup table columns
        setupTableColumns();
        
        // Setup form fields
        setupFormFields();
        
        // Load default data
        loadExpenses();
        loadCategories();
        loadAccountCodes();
        
        // Setup date pickers with current date
        expenseDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        
        // Setup table selection listener
        expenseTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    displayExpenseDetails(newValue);
                }
            }
        );
        
        // Clear form fields initially
        handleClearForm();
        
        // Update expense totals
        updateExpenseTotals();
        
        // Setup button handlers
        setupButtonHandlers();
        
        // Set status message
        statusLabel.setText("Módulo de gastos iniciado");
    }
    
    /**
     * Sets up the table columns
     */
    private void setupTableColumns() {
        // Setup columns with property value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Format amount with currency symbol
        amountColumn.setCellValueFactory(param -> 
            new SimpleDoubleProperty(param.getValue().getAmount()).asObject());
        amountColumn.setCellFactory(column -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        vendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendorName"));
        
        // Format status with colors
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<Expense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PAID":
                            setStyle("-fx-text-fill: green;");
                            break;
                        case "PENDING":
                            setStyle("-fx-text-fill: orange;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: red;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Add action column with edit/delete buttons
        TableColumn<Expense, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    displayExpenseDetails(expense);
                });
                
                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    // Use this reference to handle the delete action
                    ExpenseController.this.handleDeleteExpense(expense);
                });
                
                deleteButton.setStyle("-fx-background-color: #ff6666;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        // Check if the column already exists
        boolean hasActionColumn = false;
        for (TableColumn<Expense, ?> column : expenseTable.getColumns()) {
            if ("Actions".equals(column.getText())) {
                hasActionColumn = true;
                break;
            }
        }
        
        if (!hasActionColumn) {
            expenseTable.getColumns().add(actionColumn);
        }
        
        // Set the items
        expenseTable.setItems(expensesList);
    }
    
    /**
     * Sets up form fields with initial values
     */
    private void setupFormFields() {
        // Setup payment method options
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia", "Cheque", "Otro"
        ));
        paymentMethodComboBox.setValue("Efectivo");
        
        // Setup status filter options
        categoryFilterComboBox.getItems().add("Todas las categorías");
        
        // Set up DatePicker formats
        StringConverter<LocalDate> dateConverter = new StringConverter<>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        
        expenseDatePicker.setConverter(dateConverter);
        startDatePicker.setConverter(dateConverter);
        endDatePicker.setConverter(dateConverter);
    }
    
    /**
     * Sets up button handlers
     */
    private void setupButtonHandlers() {
        saveButton.setOnAction(event -> handleSaveExpense());
        clearButton.setOnAction(event -> handleClearForm());
        deleteButton.setOnAction(event -> handleDeleteExpense(currentExpense));
        applyFilterButton.setOnAction(event -> loadExpenses());
        clearFilterButton.setOnAction(event -> clearFilters());
    }
    
    /**
     * Loads expenses from the database based on current filters
     */
    private void loadExpenses() {
        expensesList.clear();
        
        // Get the filter values - simplificado, eliminar verificaciones null innecesarias
        LocalDate startDate = startDatePicker.getValue() != null ? 
                             startDatePicker.getValue() : LocalDate.now().minusMonths(1);
        LocalDate endDate = endDatePicker.getValue() != null ? 
                           endDatePicker.getValue() : LocalDate.now();
        String categoryFilter = categoryFilterComboBox.getValue();
        
        try {
            List<Expense> expenses;
            
            // Make sure we have a valid category filter before using it
            if (categoryFilter != null && !categoryFilter.isEmpty() && 
                !categoryFilter.equals("Todas las categorías")) {
                // Filter by category
                expenses = expenseDAO.getExpensesByCategory(categoryFilter);
            } else {
                // Filter by date range (now with non-null dates)
                expenses = expenseDAO.getExpensesByDateRange(startDate, endDate);
            }
            
            expensesList.addAll(expenses);
            updateExpenseTotals();
            
            statusLabel.setText("Se cargaron " + expenses.size() + " gastos");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error cargando gastos: " + e.getMessage());
        }
    }
    
    /**
     * Loads expense categories
     */
    private void loadCategories() {
        try {
            List<String> categories = expenseDAO.getAllCategories();
            
            // Eliminar verificaciones null innecesarias
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            
            // Also add to filter combobox
            categoryFilterComboBox.getItems().clear();
            categoryFilterComboBox.getItems().add("Todas las categorías");
            categoryFilterComboBox.getItems().addAll(categories);
            categoryFilterComboBox.setValue("Todas las categorías");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error cargando categorías: " + e.getMessage());
        }
    }
    
    /**
     * Loads account codes from the accounting system
     */
    private void loadAccountCodes() {
        try {
            List<String> accountCodes = new ArrayList<>();
            // Try to get account codes for expenses from the accounting system
            accountingDAO.getAllAccounts().forEach(account -> {
                String accountType = (String) account.get("accountType");
                if ("EXPENSE".equals(accountType)) {
                    accountCodes.add((String) account.get("accountCode") + " - " + account.get("name"));
                }
            });
            
            // If no expense accounts found, add some default codes
            if (accountCodes.isEmpty()) {
                accountCodes.add("6000 - Gastos Operativos");
                accountCodes.add("6100 - Alquileres");
                accountCodes.add("6200 - Servicios Públicos");
                accountCodes.add("6300 - Salarios");
                accountCodes.add("6400 - Impuestos");
                accountCodes.add("6500 - Mantenimiento");
            }
            
            // Eliminar verificaciones null innecesarias
            accountCodeComboBox.setItems(FXCollections.observableArrayList(accountCodes));
            accountCodeComboBox.setValue(accountCodes.get(0));
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error cargando códigos de cuenta: " + e.getMessage());
            
            // Add default codes in case of error
            List<String> defaultCodes = List.of(
                "6000 - Gastos Operativos",
                "6100 - Alquileres", 
                "6200 - Servicios Públicos",
                "6300 - Salarios",
                "6400 - Impuestos"
            );
            accountCodeComboBox.setItems(FXCollections.observableArrayList(defaultCodes));
            accountCodeComboBox.setValue(defaultCodes.get(0));
        }
    }
    
    /**
     * Updates the expense totals display
     */
    private void updateExpenseTotals() {
        // Try to use the DAO's getTotalExpenses method if applicable
        try {
            // Eliminar verificaciones null innecesarias
            LocalDate startDate = startDatePicker.getValue() != null ? 
                                startDatePicker.getValue() : LocalDate.now().minusMonths(1);
            LocalDate endDate = endDatePicker.getValue() != null ? 
                              endDatePicker.getValue() : LocalDate.now();
                              
            double total = expenseDAO.getTotalExpenses(startDate, endDate);
            totalExpensesLabel.setText(String.format("Total: $%.2f", total));
        } catch (Exception e) {
            // Fall back to calculating from the list if the DAO method fails
            double total = 0;
            for (Expense expense : expensesList) {
                total += expense.getAmount();
            }
            totalExpensesLabel.setText(String.format("Total: $%.2f", total));
        }
    }
    
    /**
     * Displays the details of a selected expense in the form
     */
    private void displayExpenseDetails(Expense expense) {
        currentExpense = expense;
        
        // Eliminar verificaciones null innecesarias
        categoryComboBox.setValue(expense.getCategory());
        amountField.setText(String.format("%.2f", expense.getAmount()));
        descriptionArea.setText(expense.getDescription());
        expenseDatePicker.setValue(expense.getExpenseDate());
        paymentMethodComboBox.setValue(expense.getPaymentMethod());
        receiptNumberField.setText(expense.getReceiptNumber());
        
        // Find matching account code
        String accountCode = expense.getAccountCode();
        if (accountCode != null && !accountCode.isEmpty()) {
            for (String code : accountCodeComboBox.getItems()) {
                if (code.startsWith(accountCode)) {
                    accountCodeComboBox.setValue(code);
                    break;
                }
            }
        }
        
        vendorNameField.setText(expense.getVendorName());
        taxDeductibleCheckBox.setSelected(expense.isTaxDeductible());
        notesArea.setText(expense.getNotes());
        
        deleteButton.setDisable(false);
    }
    
    /**
     * Handles saving a new expense or updating an existing one
     */
    @FXML
    private void handleSaveExpense() {
        // Validate basic requirements
        if (descriptionArea.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Descripción requerida", "Por favor ingrese una descripción para el gasto.");
            return;
        }
        
        try {
            // Check if we're creating a new expense or updating an existing one
            boolean isNewExpense = (currentExpense == null || currentExpense.getId() <= 0);
            
            if (isNewExpense) {
                currentExpense = new Expense();
            }
            
            // Fill expense details from form - eliminar verificaciones null innecesarias
            currentExpense.setCategory(categoryComboBox.getValue());
            
            if (!amountField.getText().isEmpty()) {
                currentExpense.setAmount(Double.parseDouble(amountField.getText().replace(",", ".")));
            } else {
                currentExpense.setAmount(0); // Default amount
            }
            
            currentExpense.setDescription(descriptionArea.getText());
            currentExpense.setExpenseDate(expenseDatePicker.getValue() != null ? 
                                        expenseDatePicker.getValue() : LocalDate.now());
            currentExpense.setPaymentMethod(paymentMethodComboBox.getValue());
            currentExpense.setReceiptNumber(receiptNumberField.getText());
            
            // Extract account code if available
            if (accountCodeComboBox.getValue() != null) {
                String accountItem = accountCodeComboBox.getValue();
                String accountCode = accountItem.split(" ")[0];
                currentExpense.setAccountCode(accountCode);
            } else {
                currentExpense.setAccountCode("6000"); // Default expense account code
            }
            
            currentExpense.setVendorName(vendorNameField.getText());
            currentExpense.setTaxDeductible(taxDeductibleCheckBox.isSelected());
            currentExpense.setNotes(notesArea.getText());
            currentExpense.setStatus("PAID"); // Assuming expenses are paid when recorded
            
            // Update the timestamp
            if (isNewExpense) {
                currentExpense.setCreatedAt(LocalDate.now().atStartOfDay());
            }
            
            // Use the consolidated method to save the expense
            boolean success = expenseDAO.saveExpense(currentExpense);
            
            if (success) {
                statusLabel.setText(isNewExpense ? "Gasto creado correctamente" : "Gasto actualizado correctamente");
                
                // Reset form for new entry
                handleClearForm();
                
                // Refresh expense list
                loadExpenses();
            } else {
                statusLabel.setText("Error al " + (isNewExpense ? "crear" : "actualizar") + " el gasto");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "El monto debe ser un número válido", "Por favor ingrese una descripción para el gasto.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Se produjo un error al guardar el gasto: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
            e.printStackTrace();
        }
    }
    
    /**
     * Clears all form fields and resets the current expense
     */
    @FXML
    private void handleClearForm() {
        currentExpense = null;
        
        // Reset form fields - eliminar verificaciones null innecesarias
        categoryComboBox.setValue(null);
        amountField.clear();
        descriptionArea.clear();
        expenseDatePicker.setValue(LocalDate.now());
        paymentMethodComboBox.setValue("Efectivo");
        receiptNumberField.clear();
        
        // Handle empty collection check
        if (!accountCodeComboBox.getItems().isEmpty()) {
            accountCodeComboBox.setValue(accountCodeComboBox.getItems().get(0));
        }
        
        vendorNameField.clear();
        taxDeductibleCheckBox.setSelected(false);
        notesArea.clear();
        
        // Disable delete button when creating a new expense
        deleteButton.setDisable(true);
        statusLabel.setText("Nuevo gasto");
    }
    
    /**
     * Handles deleting an expense
     */
    @FXML
    private void handleDeleteExpense(Expense expense) {
        if (expense == null) {
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Eliminación");
        confirmation.setHeaderText("¿Está seguro que desea eliminar este gasto?");
        confirmation.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Use the numeric getId method which handles different ID formats
                boolean success = expenseDAO.deleteExpense(expense.getId());
                
                if (success) {
                    // Create a reversal transaction
                    Transaction reversal = expense.toTransaction();
                    reversal.setType("gasto_reverso");
                    reversal.setAmount(-reversal.getAmount()); // Reverse the amount
                    reversal.setDescription("Reverso: " + reversal.getDescription());
                    
                    // Record reversal in accounting module
                    accountingModule.recordTransaction(reversal);
                    
                    statusLabel.setText("Gasto eliminado correctamente");
                    
                    // Clear form and refresh list
                    handleClearForm();
                    loadExpenses();
                } else {
                    statusLabel.setText("Error al eliminar el gasto");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al eliminar el gasto: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the delete button action event
     * Delegates to the main handleDeleteExpense method using the currentExpense
     */
    @FXML
    public void handleDeleteExpense(ActionEvent actionEvent) {
        // Simply delegate to the existing method with the current expense
        handleDeleteExpense(currentExpense);
    }
    
    /**
     * Handles exporting expenses to a file
     */
    @FXML
    private void handleExportExpenses() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Gastos");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV", "*.csv"),
            new FileChooser.ExtensionFilter("Excel", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(expenseTable.getScene().getWindow());
        if (file != null) {
            try {
                // Code to export data to file would go here
                statusLabel.setText("Datos exportados a " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al exportar datos: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Validates the form before saving
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            errors.append("La categoría es obligatoria\n");
        }
        
        if (amountField.getText().isEmpty()) {
            errors.append("El monto es obligatorio\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                if (amount <= 0) {
                    errors.append("El monto debe ser mayor que cero\n");
                }
            } catch (NumberFormatException e) {
                errors.append("El monto debe ser un número válido\n");
            }
        }
        
        if (descriptionArea.getText().isEmpty()) {
            errors.append("La descripción es obligatoria\n");
        }
        
        if (expenseDatePicker.getValue() == null) {
            errors.append("La fecha es obligatoria\n");
        }
        
        if (paymentMethodComboBox.getValue() == null || paymentMethodComboBox.getValue().isEmpty()) {
            errors.append("El método de pago es obligatorio\n");
        }
        
        if (accountCodeComboBox.getValue() == null || accountCodeComboBox.getValue().isEmpty()) {
            errors.append("El código de cuenta es obligatorio\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", errors.toString(), "Por favor ingrese una descripción para el gasto.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Shows an alert dialog with consistent parameters
     */
    private void showAlert(Alert.AlertType type, String title, String message, String s) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Implementation of FinancialUpdateListener interface method
     * This is called when a financial update occurs in the system
     */
    @Override
    public void onFinancialDataUpdated() {
        // Refresh data when financial updates occur
        Platform.runLater(this::loadExpenses);
    }
    
    /**
     * Shows a dialog to manually create a new expense directly
     */
    @FXML
    private void handleQuickExpense() {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Registro Rápido de Gastos");
        dialog.setHeaderText("Ingrese los detalles del gasto");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create form fields
        ComboBox<String> quickCategoryCombo = new ComboBox<>();
        if (categoryComboBox != null) {
            quickCategoryCombo.setItems(categoryComboBox.getItems());
        }
        
        TextField quickAmountField = new TextField();
        TextField quickDescriptionField = new TextField();
        DatePicker quickDatePicker = new DatePicker(LocalDate.now());
        ComboBox<String> quickMethodCombo = new ComboBox<>();
        if (paymentMethodComboBox != null) {
            quickMethodCombo.setItems(paymentMethodComboBox.getItems());
        }
        quickMethodCombo.setValue("Efectivo");
        
        // Add fields to grid
        grid.add(new Label("Categoría:"), 0, 0);
        grid.add(quickCategoryCombo, 1, 0);
        grid.add(new Label("Monto:"), 0, 1);
        grid.add(quickAmountField, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(quickDescriptionField, 1, 2);
        grid.add(new Label("Fecha:"), 0, 3);
        grid.add(quickDatePicker, 1, 3);
        grid.add(new Label("Método de Pago:"), 0, 4);
        grid.add(quickMethodCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the category field by default
        Platform.runLater(quickCategoryCombo::requestFocus);
        
        // Convert the result to an expense object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Expense expense = new Expense();
                    expense.setCategory(quickCategoryCombo.getValue());
                    expense.setAmount(Double.parseDouble(quickAmountField.getText().replace(",", ".")));
                    expense.setDescription(quickDescriptionField.getText());
                    expense.setExpenseDate(quickDatePicker.getValue());
                    expense.setPaymentMethod(quickMethodCombo.getValue());
                    expense.setStatus("PAID");
                    expense.setAccountCode("6000"); // Default expense account
                    return expense;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error en los datos ingresados: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
                    return null;
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Expense> result = dialog.showAndWait();
        
        result.ifPresent(expense -> {
            try {
                // Save the expense with the consolidated method
                boolean success = expenseDAO.saveExpense(expense);
                
                if (success) {
                    if (statusLabel != null) {
                        statusLabel.setText("Gasto rápido registrado correctamente");
                    }
                    loadExpenses();
                } else {
                    if (statusLabel != null) {
                        statusLabel.setText("Error al registrar el gasto rápido");
                    }
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el gasto: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Handles expense filtering - this method is referenced in the FXML file
     */
    @FXML
    private void handleFilterExpenses() {
        loadExpenses();
    }
    
    /**
     * Clears all filters and reloads data
     */
    @FXML
    private void clearFilters() {
        // Eliminar verificaciones null innecesarias
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        categoryFilterComboBox.setValue("Todas las categorías");
        
        loadExpenses();
    }

    /**
     * Handles attaching a receipt image to an expense
     */
    @FXML
    private void handleAttachReceipt() {
        if (currentExpense == null) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Debe crear o seleccionar un gasto primero antes de adjuntar un recibo", "Por favor ingrese una descripción para el gasto.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Recibo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(expenseTable.getScene().getWindow());
        if (file != null) {
            try {
                // Set the receipt image path in the current expense
                currentExpense.setReceiptImagePath(file.getAbsolutePath());
                
                statusLabel.setText("Recibo adjuntado: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al adjuntar recibo: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles viewing a receipt image attached to an expense
     */
    @FXML
    private void handleViewReceipt() {
        if (currentExpense == null) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Debe seleccionar un gasto para ver su recibo", "Por favor ingrese una descripción para el gasto.");
            return;
        }
        
        String receiptPath = currentExpense.getReceiptImagePath();
        if (receiptPath == null || receiptPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Este gasto no tiene recibo adjunto", "Por favor ingrese una descripción para el gasto.");
            return;
        }
        
        try {
            // Create a file object
            File receiptFile = new File(receiptPath);
            
            // Check if file exists
            if (!receiptFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Error", "El archivo de recibo no se encuentra: " + receiptPath, "Por favor ingrese una descripción para el gasto.");
                return;
            }
            
            // Open the file with the default system application
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(receiptFile);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se puede abrir el archivo automáticamente en este sistema", "Por favor ingrese una descripción para el gasto.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al abrir el recibo: " + e.getMessage(), "Por favor ingrese una descripción para el gasto.");
            e.printStackTrace();
        }
    }
}
