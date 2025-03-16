package com.minimercado.javafxinventario.controllers;

import com.minimercado.javafxinventario.DAO.ExpenseCategoryDAO;
import com.minimercado.javafxinventario.modules.ExpenseCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión de categorías de gastos.
 */
public class ExpenseCategoryController {

    @FXML private TableView<ExpenseCategory> categoriesTable;
    @FXML private TableColumn<ExpenseCategory, String> nameColumn;
    @FXML private TableColumn<ExpenseCategory, String> descriptionColumn;
    @FXML private TableColumn<ExpenseCategory, String> accountCodeColumn;
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField accountCodeField;
    
    @FXML private Label statusLabel;
    
    private ExpenseCategoryDAO categoryDAO = new ExpenseCategoryDAO();
    private ObservableList<ExpenseCategory> categoryList = FXCollections.observableArrayList();
    private ExpenseCategory selectedCategory;
    
    /**
     * Inicializa el controlador.
     */
    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        accountCodeColumn.setCellValueFactory(new PropertyValueFactory<>("accountCode"));
        
        // Cargar datos iniciales
        loadCategories();
        
        // Configurar listener para selección en la tabla
        categoriesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectCategory(newValue));
    }
    
    /**
     * Carga todas las categorías en la tabla.
     */
    private void loadCategories() {
        try {
            List<ExpenseCategory> categories = categoryDAO.getAllCategories();
            categoryList.setAll(categories);
            categoriesTable.setItems(categoryList);
            updateStatusLabel("Se cargaron " + categories.size() + " categorías");
        } catch (Exception e) {
            showErrorMessage("Error al cargar categorías", e.getMessage());
        }
    }
    
    /**
     * Selecciona una categoría y muestra sus detalles.
     * @param category Categoría seleccionada
     */
    private void selectCategory(ExpenseCategory category) {
        selectedCategory = category;
        
        if (category != null) {
            nameField.setText(category.getName());
            descriptionArea.setText(category.getDescription());
            accountCodeField.setText(category.getAccountCode());
            updateStatusLabel("Categoría seleccionada: " + category.getName());
        } else {
            clearForm();
        }
    }
    
    /**
     * Maneja el evento de guardar una categoría.
     */
    @FXML
    private void handleSaveCategory() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Crear o actualizar categoría
            ExpenseCategory category = (selectedCategory != null) ? selectedCategory : new ExpenseCategory();
            
            category.setName(nameField.getText().trim());
            category.setDescription(descriptionArea.getText().trim());
            category.setAccountCode(accountCodeField.getText().trim());
            
            boolean success;
            if (selectedCategory == null) {
                success = categoryDAO.insertCategory(category);
                updateStatusLabel("Categoría agregada: " + category.getName());
            } else {
                success = categoryDAO.updateCategory(category);
                updateStatusLabel("Categoría actualizada: " + category.getName());
            }
            
            if (success) {
                loadCategories();
                clearForm();
                selectedCategory = null;
            } else {
                updateStatusLabel("Error: No se pudo guardar la categoría");
            }
            
        } catch (Exception e) {
            showErrorMessage("Error al guardar categoría", e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de eliminar una categoría.
     */
    @FXML
    private void handleDeleteCategory() {
        if (selectedCategory == null) {
            updateStatusLabel("Seleccione una categoría para eliminar");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta categoría?");
        alert.setContentText("Esta acción no se puede deshacer y podría afectar a gastos existentes.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = categoryDAO.deleteCategory(selectedCategory.getId());
            if (deleted) {
                updateStatusLabel("Categoría eliminada: " + selectedCategory.getName());
                loadCategories();
                clearForm();
                selectedCategory = null;
            } else {
                updateStatusLabel("Error: No se pudo eliminar la categoría");
            }
        }
    }
    
    /**
     * Maneja el evento de limpiar el formulario.
     */
    @FXML
    private void handleClearForm() {
        clearForm();
        selectedCategory = null;
        updateStatusLabel("Formulario limpio para nueva categoría");
    }
    
    /**
     * Limpia el formulario.
     */
    private void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        accountCodeField.clear();
    }
    
    /**
     * Valida los campos requeridos del formulario.
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("- Nombre es requerido\n");
        }
        
        if (errorMessage.length() > 0) {
            showErrorMessage("Error de validación", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Actualiza el mensaje de estado.
     * @param message Mensaje a mostrar
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
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
