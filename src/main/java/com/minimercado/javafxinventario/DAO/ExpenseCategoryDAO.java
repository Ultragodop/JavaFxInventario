package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.ExpenseCategory;
import com.minimercado.javafxinventario.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones de categorías de gastos en la base de datos.
 */
public class ExpenseCategoryDAO {
    
    private static final Logger logger = Logger.getLogger(ExpenseCategoryDAO.class.getName());

    /**
     * Obtiene todas las categorías de gastos.
     * @return Lista de categorías
     */
    public static List<ExpenseCategory> getAllCategories() {
        List<ExpenseCategory> categories = new ArrayList<>();
        String query = "SELECT * FROM expense_categories ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ExpenseCategory category = mapResultSetToCategory(rs);
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener categorías de gastos", e);
        }
        return categories;
    }

    /**
     * Busca categorías de gastos por nombre o descripción.
     * @param searchTerm Término de búsqueda
     * @return Lista de categorías que coinciden con el término
     */
    public List<ExpenseCategory> searchCategories(String searchTerm) {
        List<ExpenseCategory> categories = new ArrayList<>();
        String query = "SELECT * FROM expense_categories WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExpenseCategory category = mapResultSetToCategory(rs);
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar categorías de gastos", e);
        }
        return categories;
    }

    /**
     * Obtiene una categoría por su ID.
     * @param id ID de la categoría
     * @return ExpenseCategory o null si no se encuentra
     */
    public ExpenseCategory getCategoryById(int id) {
        String query = "SELECT * FROM expense_categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener categoría por ID: " + id, e);
        }
        return null;
    }

    /**
     * Obtiene categorías por código de cuenta.
     * @param accountCode Código de cuenta contable
     * @return ExpenseCategory o null si no se encuentra
     */
    public ExpenseCategory getCategoryByAccountCode(String accountCode) {
        String query = "SELECT * FROM expense_categories WHERE account_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, accountCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener categoría por código contable: " + accountCode, e);
        }
        return null;
    }

    /**
     * Inserta una nueva categoría de gasto.
     * @param category Categoría a insertar
     * @return true si la operación fue exitosa
     */
    public boolean insertCategory(ExpenseCategory category) {
        String query = "INSERT INTO expense_categories (name, description, account_code, active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getAccountCode());
            stmt.setBoolean(4, category.isActive());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getInt(1));
                        logger.info("Categoría creada: " + category.getName() + " con ID: " + category.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al insertar categoría: " + category.getName(), e);
        }
        return false;
    }

    /**
     * Actualiza una categoría existente.
     * @param category Categoría con los datos actualizados
     * @return true si la operación fue exitosa
     */
    public boolean updateCategory(ExpenseCategory category) {
        String query = "UPDATE expense_categories SET name = ?, description = ?, account_code = ?, active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getAccountCode());
            stmt.setBoolean(4, category.isActive());
            stmt.setInt(5, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Categoría actualizada: " + category.getName() + " con ID: " + category.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar categoría con ID: " + category.getId(), e);
        }
        return false;
    }

    /**
     * Elimina una categoría de gasto.
     * @param id ID de la categoría a eliminar
     * @return true si la operación fue exitosa
     */
    public boolean deleteCategory(int id) {
        // Primero verificamos si la categoría está siendo utilizada en gastos
        if (isCategoryInUse(id)) {
            logger.warning("No se puede eliminar categoría ID: " + id + " porque está siendo utilizada en gastos");
            return false;
        }
        
        String query = "DELETE FROM expense_categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Categoría eliminada con ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al eliminar categoría con ID: " + id, e);
        }
        return false;
    }

    /**
     * Comprueba si una categoría está siendo utilizada en gastos.
     * @param categoryId ID de la categoría
     * @return true si está en uso, false en caso contrario
     */
    private boolean isCategoryInUse(int categoryId) {
        String query = "SELECT COUNT(*) FROM expenses WHERE category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar uso de categoría ID: " + categoryId, e);
        }
        return false;
    }

    /**
     * Obtiene una lista de categorías activas.
     * @return Lista de categorías activas
     */
    public List<ExpenseCategory> getActiveCategories() {
        List<ExpenseCategory> categories = new ArrayList<>();
        String query = "SELECT * FROM expense_categories WHERE active = TRUE ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                ExpenseCategory category = mapResultSetToCategory(rs);
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener categorías activas", e);
        }
        return categories;
    }

    /**
     * Actualiza el estado activo/inactivo de una categoría.
     * @param id ID de la categoría
     * @param active Estado a establecer (true: activo, false: inactivo)
     * @return true si la operación fue exitosa
     */
    public boolean updateCategoryActiveStatus(int id, boolean active) {
        String query = "UPDATE expense_categories SET active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setBoolean(1, active);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Estado de categoría ID: " + id + " actualizado a " + (active ? "activo" : "inactivo"));
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar estado de categoría ID: " + id, e);
        }
        return false;
    }

    /**
     * Mapea un ResultSet a un objeto ExpenseCategory.
     * @param rs ResultSet con datos de la categoría
     * @return Objeto ExpenseCategory
     */
    private static ExpenseCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        ExpenseCategory category = new ExpenseCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setAccountCode(rs.getString("account_code"));
        
        // Verificar si la columna active existe antes de intentar leerla
        try {
            category.setActive(rs.getBoolean("active"));
        } catch (SQLException e) {
            // Si la columna no existe, asumimos que está activa por defecto
            category.setActive(true);
        }
        
        return category;
    }
}
