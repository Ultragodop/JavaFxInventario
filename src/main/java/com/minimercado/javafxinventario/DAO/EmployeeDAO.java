package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Employee;
import com.minimercado.javafxinventario.modules.EmployeePayment;
import com.minimercado.javafxinventario.utils.DatabaseConnection;
import com.minimercado.javafxinventario.modules.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la gestión de empleados y sus pagos.
 * Proporciona métodos para crear, buscar y gestionar empleados y sus pagos.
 */
public class EmployeeDAO {
    
    private static final Logger logger = Logger.getLogger(EmployeeDAO.class.getName());
    private final AccountingDAO accountingDAO = new AccountingDAO();
    
    /**
     * Obtiene todos los empleados de la base de datos
     * @return Lista de empleados
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employee employee = mapResultSetToEmployee(rs);
                employees.add(employee);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener empleados", e);
        }
        
        return employees;
    }
    
    /**
     * Obtiene empleados activos
     * @return Lista de empleados activos
     */
    public List<Employee> getActiveEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE active = TRUE ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employee employee = mapResultSetToEmployee(rs);
                employees.add(employee);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener empleados activos", e);
        }
        
        return employees;
    }
    
    /**
     * Busca empleados por nombre, apellido o documento
     * @param searchTerm Término de búsqueda
     * @return Lista de empleados que coinciden con el término
     */
    public List<Employee> searchEmployees(String searchTerm) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE first_name LIKE ? OR last_name LIKE ? OR document_id LIKE ? " +
                     "ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = mapResultSetToEmployee(rs);
                    employees.add(employee);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al buscar empleados", e);
        }
        
        return employees;
    }
    
    /**
     * Obtiene un empleado por su ID
     * @param id ID del empleado
     * @return El empleado o null si no se encuentra
     */
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener empleado por ID: " + id, e);
        }
        
        return null;
    }
    
    /**
     * Crea un nuevo empleado en la base de datos
     * @param employee El empleado a crear
     * @return true si la operación fue exitosa
     */
    public boolean createEmployee(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, document_id, position, base_salary, " +
                     "hire_date, contact_phone, email, address, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getDocumentId());
            stmt.setString(4, employee.getPosition());
            stmt.setDouble(5, employee.getBaseSalary());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setString(7, employee.getContactPhone());
            stmt.setString(8, employee.getEmail());
            stmt.setString(9, employee.getAddress());
            stmt.setBoolean(10, employee.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employee.setId(generatedKeys.getInt(1));
                        logger.info("Empleado creado: " + employee.getFirstName() + " " + 
                                  employee.getLastName() + " con ID: " + employee.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al crear empleado", e);
        }
        
        return false;
    }
    
    /**
     * Actualiza un empleado existente
     * @param employee El empleado a actualizar
     * @return true si la operación fue exitosa
     */
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, document_id = ?, position = ?, " +
                     "base_salary = ?, hire_date = ?, contact_phone = ?, email = ?, address = ?, active = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getDocumentId());
            stmt.setString(4, employee.getPosition());
            stmt.setDouble(5, employee.getBaseSalary());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setString(7, employee.getContactPhone());
            stmt.setString(8, employee.getEmail());
            stmt.setString(9, employee.getAddress());
            stmt.setBoolean(10, employee.isActive());
            stmt.setInt(11, employee.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Empleado actualizado: " + employee.getFirstName() + " " + 
                          employee.getLastName() + " con ID: " + employee.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar empleado", e);
        }
        
        return false;
    }
    
    /**
     * Elimina un empleado
     * @param id ID del empleado a eliminar
     * @return true si la operación fue exitosa
     */
    public boolean deleteEmployee(int id) {
        // Primero verificamos si el empleado tiene pagos asociados
        if (hasPayments(id)) {
            logger.warning("No se puede eliminar el empleado ID: " + id + " porque tiene pagos asociados");
            return false;
        }
        
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Empleado eliminado con ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al eliminar empleado", e);
        }
        
        return false;
    }
    
    /**
     * Verifica si un empleado tiene pagos asociados
     * @param employeeId ID del empleado
     * @return true si tiene pagos asociados
     */
    private boolean hasPayments(int employeeId) {
        String sql = "SELECT COUNT(*) FROM employee_payments WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar pagos de empleado", e);
        }
        
        return false;
    }
    
    /**
     * Crea un nuevo pago a empleado
     * @param payment El pago a registrar
     * @return true si la operación fue exitosa
     */
    public boolean createEmployeePayment(EmployeePayment payment) {
        String sql = "INSERT INTO employee_payments (employee_id, payment_date, amount, payment_type, " +
                     "payment_method, period, description, reference_number, reconciled) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, payment.getEmployeeId());
            stmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
            stmt.setDouble(3, payment.getAmount());
            stmt.setString(4, payment.getPaymentType());
            stmt.setString(5, payment.getPaymentMethod());
            stmt.setString(6, payment.getPeriod());
            stmt.setString(7, payment.getDescription());
            stmt.setString(8, payment.getReferenceNumber());
            stmt.setBoolean(9, payment.isReconciled());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setId(generatedKeys.getInt(1));
                        
                        // Registrar el pago en el sistema contable si está marcado para reconciliar
                        if (payment.isReconciled()) {
                            registerPaymentInAccounting(payment);
                        }
                        
                        logger.info("Pago a empleado registrado con ID: " + payment.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al crear pago a empleado", e);
        }
        
        return false;
    }
    
    /**
     * Inserts a new employee payment into the database.
     * This is an alias for createEmployeePayment for better API consistency.
     *
     * @param payment The EmployeePayment object to insert.
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean insertEmployeePayment(EmployeePayment payment) {
        return createEmployeePayment(payment);
    }
    
    /**
     * Obtiene los pagos de un empleado
     * @param employeeId ID del empleado
     * @return Lista de pagos del empleado
     */
    public List<EmployeePayment> getEmployeePayments(int employeeId) {
        List<EmployeePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM employee_payments WHERE employee_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeePayment payment = mapResultSetToEmployeePayment(rs);
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos de empleado", e);
        }
        
        return payments;
    }
    
    /**
     * Obtiene los pagos en un período específico
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de pagos en el período
     */
    public List<EmployeePayment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<EmployeePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM employee_payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeePayment payment = mapResultSetToEmployeePayment(rs);
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos por rango de fechas", e);
        }
        
        return payments;
    }
    
    /**
     * Obtiene pagos no reconciliados con contabilidad
     * @return Lista de pagos no reconciliados
     */
    public List<EmployeePayment> getUnreconciledPayments() {
        List<EmployeePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM employee_payments WHERE reconciled = FALSE ORDER BY payment_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                EmployeePayment payment = mapResultSetToEmployeePayment(rs);
                payments.add(payment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos no reconciliados", e);
        }
        
        return payments;
    }
    
    /**
     * Reconcilia pagos pendientes
     * @return Número de pagos reconciliados
     */
    public int reconcilePayments() {
        List<EmployeePayment> unreconciledPayments = getUnreconciledPayments();
        int count = 0;
        
        for (EmployeePayment payment : unreconciledPayments) {
            if (registerPaymentInAccounting(payment)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Registra un pago en el sistema contable y lo marca como reconciliado
     * @param payment El pago a registrar
     * @return true si la operación fue exitosa
     */
    public boolean registerPaymentInAccounting(EmployeePayment payment) {
        try {
            Employee employee = getEmployeeById(payment.getEmployeeId());
            String employeeName = (employee != null) ? (employee.getFirstName() + " " + employee.getLastName()) : "Desconocido";
            String description = "Pago a empleado: " + employeeName + " - " + payment.getPaymentType() + 
                                " - Período: " + payment.getPeriod();
            
            // Crear transacción contable
            Transaction tx = new Transaction("salario", -payment.getAmount(), description);
            tx.setTimestamp(payment.getPaymentDate().atStartOfDay());
            
            boolean success = accountingDAO.recordTransaction(tx);
            
            if (success) {
                // Marcar como reconciliado
                String updateSql = "UPDATE employee_payments SET reconciled = TRUE WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    
                    stmt.setInt(1, payment.getId());
                    stmt.executeUpdate();
                }
                
                logger.info("Pago ID: " + payment.getId() + " reconciliado con contabilidad");
                return true;
            } else {
                logger.warning("No se pudo registrar el pago ID: " + payment.getId() + " en contabilidad");
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al crear transacción contable para pago a empleado: " + payment.getId(), e);
            return false;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Employee.
     * @param rs ResultSet con datos del empleado
     * @return Objeto Employee
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setDocumentId(rs.getString("document_id"));
        employee.setPosition(rs.getString("position"));
        employee.setBaseSalary(rs.getDouble("base_salary"));
        employee.setHireDate(rs.getDate("hire_date").toLocalDate());
        employee.setContactPhone(rs.getString("contact_phone"));
        employee.setEmail(rs.getString("email"));
        employee.setAddress(rs.getString("address"));
        employee.setActive(rs.getBoolean("active"));
        return employee;
    }

    /**
     * Mapea un ResultSet a un objeto EmployeePayment.
     * @param rs ResultSet con datos del pago
     * @return Objeto EmployeePayment
     */
    private EmployeePayment mapResultSetToEmployeePayment(ResultSet rs) throws SQLException {
        EmployeePayment payment = new EmployeePayment();
        payment.setId(rs.getInt("id"));
        payment.setEmployeeId(rs.getInt("employee_id"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentType(rs.getString("payment_type"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setPeriod(rs.getString("period"));
        payment.setDescription(rs.getString("description"));
        payment.setReconciled(rs.getBoolean("reconciled"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        return payment;
    }

    /**
     * Inserts a new employee into the database.
     *
     * @param employee The Employee object to insert.
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean insertEmployee(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, document_id, position, base_salary, hire_date, contact_phone, email, address, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getDocumentId());
            stmt.setString(4, employee.getPosition());
            stmt.setDouble(5, employee.getBaseSalary());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            stmt.setString(7, employee.getContactPhone());
            stmt.setString(8, employee.getEmail());
            stmt.setString(9, employee.getAddress());
            stmt.setBoolean(10, employee.isActive());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene pagos de empleados según filtros aplicados
     * @param employeeId ID del empleado
     * @param startDate Fecha inicial (opcional)
     * @param endDate Fecha final (opcional)
     * @param paymentType Tipo de pago (opcional)
     * @param paymentMethod Método de pago (opcional)
     * @return Lista de pagos que coinciden con los filtros
     */
    public List<EmployeePayment> getEmployeePaymentsByFilters(int employeeId, LocalDate startDate, LocalDate endDate, String paymentType, String paymentMethod) {
        List<EmployeePayment> payments = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM employee_payments WHERE employee_id = ?");
        
        List<Object> params = new ArrayList<>();
        params.add(employeeId);
        
        // Añadir filtro de fecha si se proporcionan
        if (startDate != null) {
            sqlBuilder.append(" AND payment_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        
        if (endDate != null) {
            sqlBuilder.append(" AND payment_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        
        // Añadir filtro de tipo de pago si se proporciona
        if (paymentType != null && !paymentType.isEmpty()) {
            sqlBuilder.append(" AND payment_type = ?");
            params.add(paymentType);
        }
        
        // Añadir filtro de método de pago si se proporciona
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            sqlBuilder.append(" AND payment_method = ?");
            params.add(paymentMethod);
        }
        
        // Ordenar por fecha descendente (más reciente primero)
        sqlBuilder.append(" ORDER BY payment_date DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            // Configurar parámetros
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeePayment payment = mapResultSetToEmployeePayment(rs);
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos filtrados: " + e.getMessage(), e);
        }
        
        return payments;
    }
}
