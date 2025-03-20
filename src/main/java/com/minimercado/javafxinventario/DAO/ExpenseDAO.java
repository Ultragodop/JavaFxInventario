package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Expense;
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
    private AccountingDAO accountingDAO = new AccountingDAO();

    /**
     * Registra un nuevo gasto en la base de datos y crea la respectiva transacción contable.
     * Utiliza transacciones de base de datos para garantizar la consistencia.
     *
     * @param expense Gasto a registrar
     * @return true si la operación fue exitosa, false en caso contrario
     */
    public boolean saveExpense(Expense expense) {
        Connection conn = null;
        try {
            // Verificar si el gasto ya existe para determinar si es inserción o actualización
            boolean isNewExpense = expense.getId() <= 0;
            
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            if (isNewExpense) {
                // Insertar nuevo gasto
                String insertSql = "INSERT INTO expenses (category_id, amount, expense_date, description, payment_method, " +
                                  "receipt_number, supplier, receipt_image_path, created_at, created_by, reconciled, notes, status) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    // Convertir categoría a categoryId si es necesario
                    int categoryId = 0;
                    if (expense.getCategoryId() > 0) {
                        categoryId = expense.getCategoryId();
                    } else if (expense.getCategory() != null && !expense.getCategory().isEmpty()) {
                        // Buscar categoryId por nombre o crear categoría si no existe
                        categoryId = getOrCreateCategoryId(conn, expense.getCategory());
                    }
                    
                    stmt.setInt(1, categoryId);
                    stmt.setDouble(2, expense.getAmount());
                    stmt.setDate(3, Date.valueOf(expense.getExpenseDate()));
                    stmt.setString(4, expense.getDescription());
                    stmt.setString(5, expense.getPaymentMethod());
                    stmt.setString(6, expense.getReceiptNumber());
                    
                    // Usar el nombre del proveedor adecuado (supplier o vendorName)
                    String supplierName = expense.getSupplier();
                    if (supplierName == null || supplierName.isEmpty()) {
                        supplierName = expense.getVendorName();
                    }
                    stmt.setString(7, supplierName);
                    
                    stmt.setString(8, expense.getReceiptImagePath());
                    stmt.setTimestamp(9, Timestamp.valueOf(expense.getCreatedAt()));
                    stmt.setString(10, expense.getCreatedBy());
                    stmt.setBoolean(11, expense.isReconciled() || expense.isTaxDeductible());
                    stmt.setString(12, expense.getNotes());
                    stmt.setString(13, expense.getStatus() != null ? expense.getStatus() : "PAID");
                    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("La creación del gasto falló, no se insertaron filas.");
                    }
                    
                    // Obtener ID generado
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            expense.setId(newId);
                        } else {
                            throw new SQLException("La creación del gasto falló, no se obtuvo el ID.");
                        }
                    }
                }
            } else {
                // Actualizar gasto existente
                String updateSql = "UPDATE expenses SET category_id = ?, amount = ?, expense_date = ?, description = ?, " +
                                   "payment_method = ?, receipt_number = ?, supplier = ?, receipt_image_path = ?, " +
                                   "reconciled = ?, notes = ?, status = ? WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    // Similar al caso de inserción, pero con el ID existente
                    int categoryId = 0;
                    if (expense.getCategoryId() > 0) {
                        categoryId = expense.getCategoryId();
                    } else if (expense.getCategory() != null && !expense.getCategory().isEmpty()) {
                        categoryId = getOrCreateCategoryId(conn, expense.getCategory());
                    }
                    
                    stmt.setInt(1, categoryId);
                    stmt.setDouble(2, expense.getAmount());
                    stmt.setDate(3, Date.valueOf(expense.getExpenseDate()));
                    stmt.setString(4, expense.getDescription());
                    stmt.setString(5, expense.getPaymentMethod());
                    stmt.setString(6, expense.getReceiptNumber());
                    
                    String supplierName = expense.getSupplier();
                    if (supplierName == null || supplierName.isEmpty()) {
                        supplierName = expense.getVendorName();
                    }
                    stmt.setString(7, supplierName);
                    
                    stmt.setString(8, expense.getReceiptImagePath());
                    stmt.setBoolean(9, expense.isReconciled() || expense.isTaxDeductible());
                    stmt.setString(10, expense.getNotes());
                    stmt.setString(11, expense.getStatus() != null ? expense.getStatus() : "PAID");
                    stmt.setInt(12, expense.getId());
                    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("La actualización del gasto falló, no se actualizaron filas.");
                    }
                }
            }
            
            // Crear transacción contable para el gasto
            // Determinar código de cuenta apropiado
            String accountCode = expense.getAccountCode();
            if (accountCode == null || accountCode.isEmpty()) {
                accountCode = "6000"; // Código por defecto
            }
            
            // Crear descripción detallada para la transacción
            String description = "Gasto: " + expense.getDescription();
            String supplier = expense.getVendorName() != null ? expense.getVendorName() : expense.getSupplier();
            if (supplier != null && !supplier.isEmpty()) {
                description += " - Proveedor: " + supplier;
            }
            if (expense.getReceiptNumber() != null && !expense.getReceiptNumber().isEmpty()) {
                description += " - Recibo: " + expense.getReceiptNumber();
            }
            
            // Registrar la transacción contable
            boolean transactionSuccess = accountingDAO.createExpenseTransaction(
                expense.getAmount(),
                "gasto",
                description,
                expense.getPaymentMethod(),
                expense.getExpenseDate().atStartOfDay(),
                accountCode,
                expense.getReceiptNumber() != null ? expense.getReceiptNumber() : "N/A"
            );
            
            if (!transactionSuccess) {
                throw new SQLException("La creación de la transacción contable falló");
            }
            
            // Si todo salió bien, confirmar la transacción
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            // Si ocurre un error, revertir la transacción
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al guardar gasto: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Restaurar autocommit y cerrar conexión
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create an expense with accounting transaction integration
     * 
     * @param expense The expense to create
     * @return true if successful, false otherwise
     */
    public boolean createExpenseWithTransaction(Expense expense) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // First insert the expense record
            String sql = "INSERT INTO expenses (id, category_id, amount, description, expense_date, " +
                         "payment_method, receipt_number, supplier, receipt_image_path, created_at, " +
                         "created_by, reconciled, notes, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Convert string ID to integer if needed
                try {
                    int intId = Integer.parseInt(String.valueOf(expense.getId()));
                    stmt.setInt(1, intId);
                } catch (NumberFormatException e) {
                    // If ID is not a valid integer, use a different approach
                    stmt.setObject(1, expense.getId());
                }
                
                // Obtener o crear el category_id basado en el nombre de categoría
                int categoryId = 0;
                if (expense.getCategoryId() > 0) {
                    categoryId = expense.getCategoryId();
                } else if (expense.getCategory() != null && !expense.getCategory().isEmpty()) {
                    // Aquí suponemos que tienes un método para buscar/crear categoryId
                    categoryId = getOrCreateCategoryId(conn, expense.getCategory());
                }
                stmt.setInt(2, categoryId);
                
                stmt.setDouble(3, expense.getAmount());
                stmt.setString(4, expense.getDescription());
                stmt.setDate(5, Date.valueOf(expense.getExpenseDate()));
                stmt.setString(6, expense.getPaymentMethod());
                stmt.setString(7, expense.getReceiptNumber());
                
                // El campo supplier debería recibir el nombre del proveedor (vendorName)
                stmt.setString(8, expense.getVendorName() != null ? 
                                  expense.getVendorName() : expense.getSupplier());
                
                stmt.setString(9, expense.getReceiptImagePath());
                stmt.setTimestamp(10, Timestamp.valueOf(expense.getCreatedAt()));
                stmt.setString(11, expense.getCreatedBy() != null ? 
                                  expense.getCreatedBy() : "Sistema");
                stmt.setBoolean(12, expense.isReconciled() || expense.isTaxDeductible());
                stmt.setString(13, expense.getNotes());
                stmt.setString(14, expense.getStatus() != null ? 
                                  expense.getStatus() : "PAID");
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating expense failed, no rows affected.");
                }
            }
            
            // Then create the associated accounting transaction
            boolean success = accountingDAO.createExpenseTransaction(
                expense.getAmount(),
                "gasto",
                expense.getCategory() + " - " + expense.getDescription(),
                expense.getPaymentMethod(),
                expense.getExpenseDate().atStartOfDay(),
                expense.getAccountCode() != null ? expense.getAccountCode() : "6000", // Usar código de cuenta si existe
                expense.getReceiptNumber() != null ? expense.getReceiptNumber() : "N/A"
            );
            
            if (!success) {
                throw new SQLException("Creating accounting transaction failed");
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error creating expense with transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtiene o crea un ID de categoría basado en el nombre
     * @param conn Conexión a la base de datos
     * @param categoryName Nombre de la categoría
     * @return ID de la categoría
     */
    private int getOrCreateCategoryId(Connection conn, String categoryName) throws SQLException {
        // Primero intentar encontrar la categoría por nombre
        String query = "SELECT id FROM expense_categories WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Si no existe, crear nueva categoría
        String insertQuery = "INSERT INTO expense_categories (name, description) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoryName);
            stmt.setString(2, "Categoría creada automáticamente");
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo crear la categoría, no se obtuvo ID");
                }
            }
        }
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

            // Add null checks before conversion
            if (startDate == null) startDate = LocalDate.now().minusMonths(1);
            if (endDate == null) endDate = LocalDate.now();

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
     * Obtiene todos los gastos de una categoría específica por nombre.
     * @param categoryName Nombre de la categoría
     * @return Lista de gastos
     */
    public List<Expense> getExpensesByCategory(String categoryName) {
        List<Expense> expenses = new ArrayList<>();
        String query = "SELECT e.*, c.name as category_name FROM expenses e " +
                "JOIN expense_categories c ON e.category_id = c.id " +
                "WHERE c.name = ? " +
                "ORDER BY e.expense_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, categoryName);
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


    /**
     * Create an accounting transaction for an expense.
     *
     * @param expense The expense object
     */
    private void createAccountingTransactionForExpense(Expense expense) {
        AccountingDAO accountingDAO = new AccountingDAO();

        // Make sure default accounts exist first
        accountingDAO.ensureDefaultAccountsExist();

        // Determine proper account code based on expense category
        String accountCode = "6000"; // Default expense account

        // Map expense category to the correct account code if possible
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT account_code FROM expense_categories WHERE id = ?")) {

            stmt.setInt(1, expense.getCategoryId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getString("account_code") != null) {
                    accountCode = rs.getString("account_code");
                } else {
                    // If category doesn't have an account code, use code based on category ID
                    // For example: category ID 1 -> account code 6001, category ID 2 -> account code 6002, etc.
                    accountCode = "60" + String.format("%02d", expense.getCategoryId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving category account code: " + e.getMessage());
            // Continue with default account code
        }

        // Create a proper description
        String description = "Gasto: " + expense.getDescription();
        if (expense.getSupplier() != null && !expense.getSupplier().trim().isEmpty()) {
            description += " - Proveedor: " + expense.getSupplier();
        }
        if (expense.getReceiptNumber() != null && !expense.getReceiptNumber().trim().isEmpty()) {
            description += " - Recibo: " + expense.getReceiptNumber();
        }

        // Convert expense date to LocalDateTime (at noon)
        LocalDateTime expenseDateTime = expense.getExpenseDate().atTime(12, 0);

        // Create the accounting transaction
        accountingDAO.createExpenseTransaction(
                expense.getAmount(),
                "gasto",
                description,
                expense.getPaymentMethod(),
                expenseDateTime,
                accountCode,
                expense.getReceiptNumber() != null ? expense.getReceiptNumber() : "N/A"
        );
    }

    /**
     * Obtiene todas las categorías de gastos.
     * @return Lista con los nombres de todas las categorías
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM expense_categories ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }

            // Add default categories if no data is found
            if (categories.isEmpty()) {
                categories.add("Alquileres");
                categories.add("Servicios");
                categories.add("Papelería");
                categories.add("Marketing");
                categories.add("Transporte");
                categories.add("Salarios");
                categories.add("Mantenimiento");
                categories.add("Otros");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener categorías de gastos: " + e.getMessage());
            e.printStackTrace();

            // Return default categories if there was an error
            categories.add("Alquileres");
            categories.add("Servicios");
            categories.add("Papelería");
            categories.add("Marketing");
            categories.add("Transporte");
            categories.add("Salarios");
            categories.add("Mantenimiento");
            categories.add("Otros");
        }

        return categories;
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

