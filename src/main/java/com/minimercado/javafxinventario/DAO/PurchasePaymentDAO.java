package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.PurchaseOrder;
import com.minimercado.javafxinventario.modules.PurchasePayment;
import com.minimercado.javafxinventario.modules.Transaction;
import com.minimercado.javafxinventario.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la gestión de pagos de órdenes de compra.
 * Proporciona métodos para crear, buscar y gestionar pagos a proveedores.
 */
public class PurchasePaymentDAO {
    
    private static final Logger logger = Logger.getLogger(PurchasePaymentDAO.class.getName());
    private final AccountingDAO accountingDAO = new AccountingDAO();
    
    /**
     * Registra un nuevo pago para una orden de compra
     * @param payment El objeto de pago con la información completa
     * @return true si se creó exitosamente, false en caso contrario
     */
    public boolean createPayment(PurchasePayment payment) {
        String sql = "INSERT INTO purchase_payments (purchase_order_id, payment_date, amount, original_amount, " +
                    "payment_method, is_complete_payment, reference_number, notes, created_at, created_by, reconciled) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, payment.getPurchaseOrderId());
            stmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
            stmt.setDouble(3, payment.getAmount());
            stmt.setDouble(4, payment.getOriginalAmount());
            stmt.setString(5, payment.getPaymentMethod());
            stmt.setBoolean(6, payment.isCompletePayment());
            stmt.setString(7, payment.getReferenceNumber());
            stmt.setString(8, payment.getNotes());
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(10, payment.getCreatedBy());
            stmt.setBoolean(11, payment.isReconciled());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setId(generatedKeys.getInt(1));
                    
                    // Registrar el pago en el sistema contable si está marcado para reconciliar
                    if (payment.isReconciled()) {
                        registerPaymentInAccounting(payment);
                    }
                    
                    // Actualizar el estado de la orden si es un pago completo
                    if (payment.isCompletePayment()) {
                        updatePurchaseOrderPaymentStatus(payment.getPurchaseOrderId(), true);
                    }
                    
                    return true;
                } else {
                    logger.warning("No se pudo obtener el ID del pago insertado");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al crear pago", e);
            return false;
        }
    }
    
    /**
     * Actualiza el estado de pago de una orden de compra
     * @param purchaseOrderId ID de la orden de compra
     * @param isPaid true si está pagada, false si no
     * @return true si se actualizó correctamente, false en caso contrario
     */
    private boolean updatePurchaseOrderPaymentStatus(int purchaseOrderId, boolean isPaid) {
        String sql = "UPDATE purchase_orders SET payment_status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, isPaid ? "PAID" : "UNPAID");
            stmt.setInt(2, purchaseOrderId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar estado de pago de la orden", e);
            return false;
        }
    }
    
    /**
     * Obtiene todos los pagos asociados a una orden de compra
     * @param purchaseOrderId ID de la orden de compra
     * @return Lista de pagos
     */
    public List<PurchasePayment> getPaymentsByPurchaseOrder(int purchaseOrderId) {
        List<PurchasePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM purchase_payments WHERE purchase_order_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, purchaseOrderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchasePayment payment = mapResultSetToPayment(rs);
                    payments.add(payment);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos por orden de compra", e);
        }
        
        return payments;
    }
    
    /**
     * Calcula el total pagado para una orden de compra
     * @param purchaseOrderId ID de la orden de compra
     * @return Monto total pagado
     */
    public double getTotalPaidForPurchaseOrder(int purchaseOrderId) {
        String sql = "SELECT SUM(amount) as total_paid FROM purchase_payments WHERE purchase_order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, purchaseOrderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_paid");
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al calcular total pagado", e);
        }
        
        return 0.0;
    }
    
    /**
     * Verifica si una orden está completamente pagada
     * @param purchaseOrderId ID de la orden de compra
     * @param totalAmount Monto total de la orden
     * @return true si está completamente pagada, false en caso contrario
     */
    public boolean isPurchaseOrderFullyPaid(int purchaseOrderId, double totalAmount) {
        double totalPaid = getTotalPaidForPurchaseOrder(purchaseOrderId);
        return Math.abs(totalPaid - totalAmount) < 0.01; // Comparación con tolerancia para errores de redondeo
    }
    
    /**
     * Elimina un pago
     * @param paymentId ID del pago a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deletePayment(int paymentId) {
        // Primero obtener la información del pago para actualizar la orden si es necesario
        PurchasePayment payment = getPaymentById(paymentId);
        if (payment == null) {
            return false;
        }
        
        String sql = "DELETE FROM purchase_payments WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, paymentId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Si era un pago completo, actualizar el estado de la orden
                if (payment.isCompletePayment()) {
                    updatePurchaseOrderPaymentStatus(payment.getPurchaseOrderId(), false);
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al eliminar pago", e);
            return false;
        }
    }
    
    /**
     * Obtiene un pago por su ID
     * @param paymentId ID del pago
     * @return Objeto PurchasePayment o null si no se encuentra
     */
    public PurchasePayment getPaymentById(int paymentId) {
        String sql = "SELECT * FROM purchase_payments WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, paymentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pago por ID", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene pagos no reconciliados para sincronizar con contabilidad
     * @return Lista de pagos no reconciliados
     */
    public List<PurchasePayment> getUnreconciledPayments() {
        List<PurchasePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM purchase_payments WHERE reconciled = false ORDER BY payment_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                PurchasePayment payment = mapResultSetToPayment(rs);
                payments.add(payment);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos no reconciliados", e);
        }
        
        return payments;
    }
    
    /**
     * Reconcilia pagos pendientes con el sistema contable
     * @return Número de pagos reconciliados exitosamente
     */
    public int reconcilePayments() {
        List<PurchasePayment> unreconciledPayments = getUnreconciledPayments();
        int reconcileCount = 0;
        
        for (PurchasePayment payment : unreconciledPayments) {
            boolean success = registerPaymentInAccounting(payment);
            if (success) {
                reconcileCount++;
            }
        }
        
        return reconcileCount;
    }
    
    /**
     * Registra un pago en el sistema contable y lo marca como reconciliado
     * @param payment Pago a registrar
     * @return true si se registró correctamente, false en caso contrario
     */
    public boolean registerPaymentInAccounting(PurchasePayment payment) {
        try {
            // Obtener información de la orden para la descripción
            PurchaseOrder order = getPurchaseOrderDetails(payment.getPurchaseOrderId());
            String description = "Pago a proveedor: " + 
                                (order != null ? order.getSupplierName() : "Desconocido") + 
                                " - Orden #" + payment.getPurchaseOrderId();
            
            // Usar el DAO de contabilidad para registrar la transacción
            Transaction tx = new Transaction("compra", -payment.getAmount(), description);
            tx.setTimestamp(payment.getPaymentDate().atStartOfDay());
            
            boolean success = accountingDAO.recordTransaction(tx);
            
            if (success) {
                // Marcar el pago como reconciliado
                String query = "UPDATE purchase_payments SET reconciled = true WHERE id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, payment.getId());
                    stmt.executeUpdate();
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al crear transacción contable para pago a proveedor", e);
            return false;
        }
    }
    
    /**
     * Obtiene detalles básicos de una orden de compra
     * @param purchaseOrderId ID de la orden
     * @return Objeto PurchaseOrder con datos básicos, o null si no existe
     */
    private PurchaseOrder getPurchaseOrderDetails(int purchaseOrderId) {
        String sql = "SELECT id, supplier_id, supplier_name, total_amount, status " +
                    "FROM purchase_orders WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, purchaseOrderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrder order = new PurchaseOrder();
                    order.setId(rs.getInt("id"));
                    order.setSupplierId(rs.getInt("supplier_id"));
                    order.setSupplierName(rs.getString("supplier_name"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setStatus(rs.getString("status"));
                    return order;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener detalles de orden de compra", e);
        }
        
        return null;
    }
    
    /**
     * Obtiene pagos del período especificado
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de pagos en el período
     */
    public List<PurchasePayment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<PurchasePayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM purchase_payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener pagos por rango de fechas", e);
        }
        
        return payments;
    }

    /**
     * Mapea un ResultSet a un objeto PurchasePayment.
     * @param rs ResultSet con datos del pago
     * @return Objeto PurchasePayment
     */
    private PurchasePayment mapResultSetToPayment(ResultSet rs) throws SQLException {
        PurchasePayment payment = new PurchasePayment();
        payment.setId(rs.getInt("id"));
        payment.setPurchaseOrderId(rs.getInt("purchase_order_id"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setAmount(rs.getDouble("amount"));
        payment.setOriginalAmount(rs.getDouble("original_amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setCompletePayment(rs.getBoolean("is_complete_payment"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setNotes(rs.getString("notes"));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        payment.setCreatedBy(rs.getString("created_by"));
        payment.setReconciled(rs.getBoolean("reconciled"));
        return payment;
    }

    /**
     * Inserta un nuevo pago en la base de datos.
     * 
     * @param payment El objeto PurchasePayment a insertar
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean insertPayment(PurchasePayment payment) {
        String sql = "INSERT INTO purchase_payments "
                + "(purchase_order_id, payment_date, amount, original_amount, payment_method, "
                + "complete_payment, reference_number, notes, created_at, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, payment.getPurchaseOrderId());
            pstmt.setDate(2, java.sql.Date.valueOf(payment.getPaymentDate()));
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setDouble(4, payment.getOriginalAmount());
            pstmt.setString(5, payment.getPaymentMethod());
            pstmt.setBoolean(6, payment.isCompletePayment());
            pstmt.setString(7, payment.getReferenceNumber());
            pstmt.setString(8, payment.getNotes());
            pstmt.setString(9, payment.getCreatedBy());
            
            int rowsAffected = pstmt.executeUpdate();
            
            // Si el pago es completo, actualizar el estado de la orden de compra
            if (payment.isCompletePayment()) {
                updateOrderStatus(payment.getPurchaseOrderId(), "PAGADA");
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar pago: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza el estado de una orden de compra.
     * 
     * @param purchaseOrderId ID de la orden de compra
     * @param status Nuevo estado a aplicar
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    private boolean updateOrderStatus(int purchaseOrderId, String status) {
        String sql = "UPDATE purchase_orders SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, purchaseOrderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de orden: " + e.getMessage());
            return false;
        }
    }
}
