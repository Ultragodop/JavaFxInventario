package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Expense;
import com.minimercado.javafxinventario.modules.ExpenseCategory;
import com.minimercado.javafxinventario.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para operaciones de gastos generales en la base de datos.
 */
public class ExpenseDAO {
    
    private ExpenseCategoryDAO categoryDAO = new ExpenseCategoryDAO();
    
    /**
     * Registra un nuevo gasto en la base de datos.
     * @param expense Gasto a registrar
     * @return true si la operación fue exitosa
     */
    public boolean insertExpense(Expense expense) {
        String query = "INSERT INTO expenses (category_id, amount, expense_date, description, payment_method, " +
                      "receipt_number, supplier, receipt_image_path, created_at, created_by, reconciled, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, expense.getCategoryId());
            stmt.setDouble(2, expense.getAmount());
            stmt.setDate(3, Date.valueOf(expense.getExpenseDate()));
            stmt.setString(4, expense.getDescription());
            stmt.setString(5, expense.getPaymentMethod());
            stmt.setString(6, expense.getReceiptNumber());
            stmt.setString(7, expense.getSupplier());
            stmt.setString(8, expense.getReceiptImagePath());
            stmt.setTimestamp(9, Timestamp.valueOf(expense.getCreatedAt()));
            stmt.setString(10, expense.getCreatedBy());
            stmt.setBoolean(11, expense.isReconciled());
            stmt.setString(12, expense.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    expense.setId(generatedKeys.getInt(1));
                }
                
                // Crear transacción contable para el gasto
                createAccountingTransactionForExpense(expense);
                
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar gasto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene todos los gastos en un rango de fechas.
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de gastos
     */
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT e.*, c.name as category_name FROM expenses e " +
                      "JOIN expense_categories c ON e.category_id = c.id " +
                      "WHERE e.expense_date BETWEEN ? AND ? " +
                      "ORDER BY e.expense_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Expense expense = mapResultSetToExpense(rs);
                expenses.add(expense);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener gastos por rango de fechas: " + e.getMessage());
            e.printStackTrace();
        }
        return expenses;
    }

    /**
     * Obtiene todos los gastos de una categoría específica.
     * @param categoryId ID de la categoría
     * @return Lista de gastos
     */
    public List<Expense> getExpensesByCategory(int categoryId) {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT e.*, c.name as category_name FROM expenses e " +
                      "JOIN expense_categories c ON e.category_id = c.id " +
                      "WHERE e.category_id = ? " +
                      "ORDER BY e.expense_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Expense expense = mapResultSetToExpense(rs);
                expenses.add(expense);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener gastos por categoría: " + e.getMessage());
            e.printStackTrace();
        }
        return expenses;
    }

    /**
     * Obtiene un gasto por su ID.
     * @param id ID del gasto
     * @return Expense o null si no se encuentra
     */
    public Expense getExpenseById(int id) {
        String query = "SELECT e.*, c.name as category_name FROM expenses e " +
                      "JOIN expense_categories c ON e.category_id = c.id " +
                      "WHERE e.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToExpense(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener gasto por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Actualiza un gasto existente.
     * @param expense Gasto con los datos actualizados
     * @return true si la operación fue exitosa
     */
    public boolean updateExpense(Expense expense) {
        String query = "UPDATE expenses SET category_id = ?, amount = ?, expense_date = ?, description = ?, " +
                      "payment_method = ?, receipt_number = ?, supplier = ?, receipt_image_path = ?, " +
                      "reconciled = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, expense.getCategoryId());
            stmt.setDouble(2, expense.getAmount());
            stmt.setDate(3, Date.valueOf(expense.getExpenseDate()));
            stmt.setString(4, expense.getDescription());
            stmt.setString(5, expense.getPaymentMethod());
            stmt.setString(6, expense.getReceiptNumber());
            stmt.setString(7, expense.getSupplier());
            stmt.setString(8, expense.getReceiptImagePath());
            stmt.setBoolean(9, expense.isReconciled());
            stmt.setString(10, expense.getNotes());
            stmt.setInt(11, expense.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar gasto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Elimina un gasto.
     * @param id ID del gasto a eliminar
     * @return true si la operación fue exitosa
     */
    public boolean deleteExpense(int id) {
        String query = "DELETE FROM expenses WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar gasto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene un resumen de gastos por categoría en un rango de fechas.
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Mapa con categoría como clave y suma de gastos como valor
     */
    public Map<String, Double> getExpensesSummaryByCategory(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> summary = new HashMap<>();
        String query = "SELECT c.name, SUM(e.amount) as total FROM expenses e " +
                      "JOIN expense_categories c ON e.category_id = c.id " +
                      "WHERE e.expense_date BETWEEN ? AND ? " +
                      "GROUP BY c.name ORDER BY total DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String category = rs.getString("name");
                double total = rs.getDouble("total");
                summary.put(category, total);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener resumen de gastos: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Obtiene el total de gastos en un rango de fechas.
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Total de gastos
     */
    public double getTotalExpenses(LocalDate startDate, LocalDate endDate) {
        String query = "SELECT SUM(amount) as total FROM expenses WHERE expense_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de gastos: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Crea una transacción contable para un gasto.
     * @param expense El gasto a registrar contablemente
     */
    private void createAccountingTransactionForExpense(Expense expense) {
        try {
            // Obtener la categoría para saber el código contable
            ExpenseCategory category = categoryDAO.getCategoryById(expense.getCategoryId());
            if (category == null) return;
            
            String accountCode = category.getAccountCode();
            if (accountCode == null || accountCode.isEmpty()) {
                accountCode = "5500"; // Código genérico para gastos diversos
            }
            
            // Crear la transacción contable
            AccountingDAO accountingDAO = new AccountingDAO();
            accountingDAO.createExpenseTransaction(
                expense.getAmount(),
                "gasto",
                expense.getDescription(),
                expense.getPaymentMethod(),
                expense.getExpenseDate().atStartOfDay(),
                accountCode,
                expense.getReceiptNumber()
            );
            
            // Marcar el gasto como reconciliado
            String query = "UPDATE expenses SET reconciled = true WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, expense.getId());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error al crear transacción contable para gasto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mapea un ResultSet a un objeto Expense.
     * @param rs ResultSet con datos del gasto
     * @return Objeto Expense
     */
    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setCategoryId(rs.getInt("category_id"));
        expense.setCategoryName(rs.getString("category_name"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        expense.setDescription(rs.getString("description"));
        expense.setPaymentMethod(rs.getString("payment_method"));
        expense.setReceiptNumber(rs.getString("receipt_number"));
        expense.setSupplier(rs.getString("supplier"));
        expense.setReceiptImagePath(rs.getString("receipt_image_path"));
        expense.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        expense.setCreatedBy(rs.getString("created_by"));
        expense.setReconciled(rs.getBoolean("reconciled"));
        expense.setNotes(rs.getString("notes"));
        return expense;
    }
}
