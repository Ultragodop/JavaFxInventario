package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.ExpenseDAO;
import com.minimercado.javafxinventario.modules.Expense;
import com.minimercado.javafxinventario.modules.ExpenseCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.minimercado.javafxinventario.DAO.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpenseController {

    // FXML annotated fields for UI components
    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, Double> amountColumn;
    @FXML private TableColumn<Expense, String> descriptionColumn;
    @FXML private TableColumn<Expense, String> categoryColumn;
    @FXML private TableColumn<Expense, String> paymentMethodColumn;
    
    @FXML private ComboBox<ExpenseCategory> categoryCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private ComboBox<String> periodFilterCombo;
    @FXML private ComboBox<ExpenseCategory> categoryFilterCombo;
    
    @FXML private DatePicker expenseDatePicker;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private TextField amountField;
    @FXML private TextField receiptNumberField;
    @FXML private TextField supplierField;
    
    @FXML private TextArea descriptionArea;
    @FXML private TextArea notesArea;
    
    @FXML private Label statusLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label filePathLabel;
    
    @FXML private Button attachReceiptButton;
    
    @FXML private PieChart categoryPieChart;
    
    // Non-FXML fields
    private ObservableList<ExpenseCategory> categoryList = FXCollections.observableArrayList();
    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();
    private Expense selectedExpense;
    private String receiptImagePath;
    
    // Data access object
    private ExpenseDAO expenseDAO = new ExpenseDAO();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        
        // Formato para la columna de fecha
        dateColumn.setCellFactory(column -> new TableCell<Expense, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        // Formato para la columna de monto
        amountColumn.setCellFactory(column -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        // Cargar categorías
        loadCategories();

        // Configurar combos
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito", "Transferencia", "Cheque", "Otro"));
        paymentMethodCombo.setValue("Efectivo");

        periodFilterCombo.setItems(FXCollections.observableArrayList(
                "Este mes", "Mes anterior", "Últimos 30 días", "Últimos 90 días", "Este año", "Personalizado"));
        periodFilterCombo.setValue("Este mes");
        
        // Configurar fechas por defecto
        expenseDatePicker.setValue(LocalDate.now());
        setupDefaultDates();
        
        // Listener para el combo de período
        periodFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setupDateRangeFromPeriod(newVal);
            }
        });
        
        // Listener para selección en la tabla
        expenseTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectExpense(newSelection));
        
        // Cargar gastos iniciales
        loadExpenses();
        updateChart();
    }

    /**
     * Carga las categorías de gastos.
     */
    private void loadCategories() {
        try {
            List<ExpenseCategory> categories = ExpenseCategoryDAO.getAllCategories();
            categoryList.setAll(categories);
            
            // Configurar combos de categorías
            categoryCombo.setItems(categoryList);
            if (!categoryList.isEmpty()) {
                categoryCombo.setValue(categoryList.get(0));
            }
            
            // Configurar combo de filtro por categoría
            ObservableList<ExpenseCategory> filterCategories = FXCollections.observableArrayList(categories);
            
            // Corregir la creación del objeto ExpenseCategory usando el constructor correcto
            // con tres argumentos String (nombre, descripción, color/icono)
            ExpenseCategory allCategories = new ExpenseCategory("Todas las categorías", "", "general");
            
            allCategories.setId(0);
            filterCategories.add(0, allCategories);
            categoryFilterCombo.setItems(filterCategories);
            categoryFilterCombo.setValue(allCategories);
            
        } catch (Exception e) {
            showErrorMessage("Error al cargar categorías", e.getMessage());
        }
    }

    /**
     * Configura las fechas por defecto para el filtro.
     */
    private void setupDefaultDates() {
        // Por defecto mostrar el mes actual
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        
        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(now);
    }

    /**
     * Configura el rango de fechas basado en el período seleccionado.
     * @param period Período seleccionado
     */
    private void setupDateRangeFromPeriod(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        
        switch (period) {
            case "Este mes":
                start = now.withDayOfMonth(1);
                end = now;
                break;
            case "Mes anterior":
                LocalDate firstDayOfPreviousMonth = now.minusMonths(1).withDayOfMonth(1);
                start = firstDayOfPreviousMonth;
                end = firstDayOfPreviousMonth.plusMonths(1).minusDays(1);
                break;
            case "Últimos 30 días":
                start = now.minusDays(30);
                end = now;
                break;
            case "Últimos 90 días":
                start = now.minusDays(90);
                end = now;
                break;
            case "Este año":
                start = now.withDayOfYear(1);
                end = now;
                break;
            case "Personalizado":
                // No cambiar las fechas si es personalizado
                return;
            default:
                start = now.withDayOfMonth(1);
                end = now;
                break;
        }
        
        startDatePicker.setValue(start);
        endDatePicker.setValue(end);
        
        // Cargar gastos con el nuevo rango
        loadExpenses();
    }

    /**
     * Carga los gastos según el rango de fechas seleccionado.
     */
    private void loadExpenses() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate == null || endDate == null) {
                setupDefaultDates();
                startDate = startDatePicker.getValue();
                endDate = endDatePicker.getValue();
            }
            
            List<Expense> expenses = expenseDAO.getExpensesByDateRange(startDate, endDate);
            
            // Aplicar filtro de categoría si es necesario
            ExpenseCategory selectedCategory = categoryFilterCombo.getValue();
            if (selectedCategory != null && selectedCategory.getId() != 0) {
                expenses.removeIf(e -> e.getCategoryId() != selectedCategory.getId());
            }
            
            expenseList.setAll(expenses);
            expenseTable.setItems(expenseList);
            
            // Actualizar total de gastos
            updateTotalExpensesLabel();
            
            statusLabel.setText("Se cargaron " + expenses.size() + " registros de gastos");
        } catch (Exception e) {
            showErrorMessage("Error al cargar gastos", e.getMessage());
        }
    }

    /**
     * Actualiza la etiqueta de total de gastos.
     */
    private void updateTotalExpensesLabel() {
        double total = expenseList.stream().mapToDouble(Expense::getAmount).sum();
        totalExpensesLabel.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Actualiza el gráfico de gastos.
     */
    private void updateChart() {
        try {
            // Agrupar gastos por categoría
            Map<String, Double> categoryTotals = new HashMap<>();
            
            for (Expense expense : expenseList) {
                String category = expense.getCategoryName();
                double amount = expense.getAmount();
                
                // Acumular el monto por categoría
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
            
            // Crear los datos para el gráfico
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            categoryPieChart.setData(pieChartData);
            categoryPieChart.setTitle("Gastos por Categoría");
            
        } catch (Exception e) {
            showErrorMessage("Error al actualizar gráfico", e.getMessage());
        }
    }

    /**
     * Selecciona un gasto y muestra sus detalles.
     * @param expense Gasto seleccionado
     */
    private void selectExpense(Expense expense) {
        selectedExpense = expense;
        
        if (expense != null) {
            // Cargar datos del gasto en el formulario
            for (ExpenseCategory category : categoryList) {
                if (category.getId() == expense.getCategoryId()) {
                    categoryCombo.setValue(category);
                    break;
                }
            }
            
            expenseDatePicker.setValue(expense.getExpenseDate());
            amountField.setText(String.valueOf(expense.getAmount()));
            descriptionArea.setText(expense.getDescription());
            paymentMethodCombo.setValue(expense.getPaymentMethod());
            receiptNumberField.setText(expense.getReceiptNumber());
            supplierField.setText(expense.getSupplier());
            notesArea.setText(expense.getNotes());
            
            // Actualizar ruta de imagen de recibo
            receiptImagePath = expense.getReceiptImagePath();
            if (receiptImagePath != null && !receiptImagePath.isEmpty()) {
                filePathLabel.setText(receiptImagePath.substring(receiptImagePath.lastIndexOf(File.separator) + 1));
            } else {
                filePathLabel.setText("No hay comprobante adjunto");
            }
            
            statusLabel.setText("Gasto seleccionado para edición");
        }
    }

    /**
     * Maneja el evento de filtrar gastos.
     */
    @FXML
    private void handleFilterExpenses() {
        loadExpenses();
        updateChart();
    }

    /**
     * Maneja el evento de guardar un gasto.
     */
    @FXML
    private void handleSaveExpense() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Crear o actualizar gasto
            Expense expense = (selectedExpense != null) ? selectedExpense : new Expense();
            
            expense.setCategoryId(categoryCombo.getValue().getId());
            expense.setAmount(Double.parseDouble(amountField.getText().trim()));
            expense.setExpenseDate(expenseDatePicker.getValue());
            expense.setDescription(descriptionArea.getText().trim());
            expense.setPaymentMethod(paymentMethodCombo.getValue());
            expense.setReceiptNumber(receiptNumberField.getText().trim());
            expense.setSupplier(supplierField.getText().trim());
            expense.setReceiptImagePath(receiptImagePath);
            expense.setNotes(notesArea.getText().trim());
            
            // Si es nuevo gasto, configurar creación
            if (selectedExpense == null) {
                expense.setCreatedAt(LocalDateTime.now());
                expense.setCreatedBy("Usuario"); // Esto debería venir del sistema de autenticación
                expense.setReconciled(false);
            }
            
            boolean success;
            if (selectedExpense == null) {
                success = expenseDAO.insertExpense(expense);
                statusLabel.setText("Gasto registrado exitosamente");
            } else {
                success = expenseDAO.updateExpense(expense);
                statusLabel.setText("Gasto actualizado exitosamente");
            }
            
            if (success) {
                clearForm();
                selectedExpense = null;
                loadExpenses();
                updateChart();
            } else {
                statusLabel.setText("Error al guardar el gasto");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: El monto debe ser un número válido");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento de eliminar un gasto.
     */
    @FXML
    private void handleDeleteExpense() {
        if (selectedExpense == null) {
            statusLabel.setText("Seleccione un gasto para eliminar");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este gasto?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = expenseDAO.deleteExpense(selectedExpense.getId());
            if (deleted) {
                statusLabel.setText("Gasto eliminado exitosamente");
                clearForm();
                selectedExpense = null;
                loadExpenses();
                updateChart();
            } else {
                statusLabel.setText("Error al eliminar el gasto");
            }
        }
    }

    /**
     * Maneja el evento de adjuntar un comprobante.
     */
    @FXML
    private void handleAttachReceipt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Comprobante");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        Stage stage = (Stage) attachReceiptButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Crear directorio para comprobantes si no existe
                Path receiptDirectory = Paths.get("receipts");
                if (!Files.exists(receiptDirectory)) {
                    Files.createDirectories(receiptDirectory);
                }
                
                // Generar un nombre único para el archivo
                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destinationPath = receiptDirectory.resolve(uniqueFileName);
                
                // Copiar el archivo al directorio de comprobantes
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Actualizar la ruta del comprobante
                receiptImagePath = destinationPath.toString();
                filePathLabel.setText(uniqueFileName);
                
                statusLabel.setText("Comprobante adjuntado: " + uniqueFileName);
            } catch (Exception e) {
                showErrorMessage("Error al adjuntar comprobante", e.getMessage());
            }
        }
    }

    /**
     * Maneja el evento de ver el comprobante adjunto.
     */
    @FXML
    private void handleViewReceipt() {
        if (receiptImagePath == null || receiptImagePath.isEmpty()) {
            statusLabel.setText("No hay comprobante adjunto");
            return;
        }
        
        try {
            File file = new File(receiptImagePath);
            if (file.exists()) {
                // Abrir el archivo con la aplicación predeterminada
                java.awt.Desktop.getDesktop().open(file);
            } else {
                statusLabel.setText("El archivo del comprobante no existe");
            }
        } catch (Exception e) {
            showErrorMessage("Error al abrir el comprobante", e.getMessage());
        }
    }

    /**
     * Maneja el evento de limpiar el formulario.
     */
    @FXML
    private void handleClearForm() {
        clearForm();
        selectedExpense = null;
        statusLabel.setText("Formulario limpio para nuevo gasto");
    }

    /**
     * Limpia el formulario.
     */
    private void clearForm() {
        if (!categoryList.isEmpty()) {
            categoryCombo.setValue(categoryList.get(0));
        }
        expenseDatePicker.setValue(LocalDate.now());
        amountField.clear();
        descriptionArea.clear();
        paymentMethodCombo.setValue("Efectivo");
        receiptNumberField.clear();
        supplierField.clear();
        notesArea.clear();
        filePathLabel.setText("No hay comprobante adjunto");
        receiptImagePath = null;
    }

    /**
     * Valida los campos requeridos del formulario.
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (categoryCombo.getValue() == null) {
            errorMessage.append("- Categoría es requerida\n");
        }
        
        if (expenseDatePicker.getValue() == null) {
            errorMessage.append("- Fecha es requerida\n");
        }
        
        if (amountField.getText().trim().isEmpty()) {
            errorMessage.append("- Monto es requerido\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    errorMessage.append("- El monto debe ser mayor que cero\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- El monto debe ser un número válido\n");
            }
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("- Descripción es requerida\n");
        }
        
        if (paymentMethodCombo.getValue() == null) {
            errorMessage.append("- Método de pago es requerido\n");
        }
        
        if (errorMessage.length() > 0) {
            showErrorMessage("Error de validación", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    /**
     * Muestra un mensaje de error.
     * @param title Título del mensaje
     * @param message Contenido del mensaje
     */
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
