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
    public boolean insertPayment(PurchasePayment payment) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO purchase_payments "
                    + "(purchase_order_id, payment_date, amount, original_amount, payment_method, "
                    + "is_complete_payment, reference_number, notes, created_at, created_by, reconciled) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setInt(1, payment.getPurchaseOrderId());
                pstmt.setDate(2, java.sql.Date.valueOf(payment.getPaymentDate()));
                pstmt.setDouble(3, payment.getAmount());
                pstmt.setDouble(4, payment.getOriginalAmount());
                pstmt.setString(5, payment.getPaymentMethod());
                pstmt.setBoolean(6, payment.isCompletePayment());
                pstmt.setString(7, payment.getReferenceNumber());
                pstmt.setString(8, payment.getNotes());
                pstmt.setString(9, payment.getCreatedBy());
                pstmt.setBoolean(10, false); // Initially not reconciled
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Obtener el ID generado para el nuevo pago
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            payment.setId(rs.getInt(1));
                        }
                    }
                    
                    // Actualizar el estado de pago y monto pagado en la orden
                    double totalPaid = getTotalPaidForPurchaseOrder(conn, payment.getPurchaseOrderId());
                    double orderAmount = getOrderTotalAmount(conn, payment.getPurchaseOrderId());
                    
                    String newStatus;
                    if (payment.isCompletePayment() || Math.abs(totalPaid - orderAmount) < 0.01) {
                        newStatus = "PAID";
                    } else if (totalPaid > 0) {
                        newStatus = "PARTIALLY_PAID";
                    } else {
                        newStatus = "UNPAID";
                    }
                    
                    // Actualizar el estado de pago de la orden
                    updateOrderPaymentStatus(conn, payment.getPurchaseOrderId(), newStatus, totalPaid);
                    
                    conn.commit();
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            logger.log(Level.SEVERE, "Error al insertar pago", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing connection", e);
            }
        }
    }

    /**
     * Obtiene el monto total de una orden de compra.
     * 
     * @param conn Conexión a la base de datos
     * @param orderId ID de la orden de compra
     * @return Monto total de la orden
     * @throws SQLException si ocurre un error en la base de datos
     */
    private double getOrderTotalAmount(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT total_amount FROM purchase_orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        }
        return 0.0;
    }

    /**
     * Calcula el total pagado para una orden de compra.
     * 
     * @param conn Conexión a la base de datos
     * @param orderId ID de la orden de compra
     * @return Monto total pagado
     * @throws SQLException si ocurre un error en la base de datos
     */
    private double getTotalPaidForPurchaseOrder(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT SUM(amount) as total_paid FROM purchase_payments WHERE purchase_order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_paid");
                }
            }
        }
        return 0.0;
    }

    /**
     * Actualiza el estado de pago de una orden de compra.
     * 
     * @param conn Conexión a la base de datos
     * @param orderId ID de la orden de compra
     * @param status Nuevo estado de pago
     * @param totalPaid Total pagado hasta ahora
     * @throws SQLException si ocurre un error en la base de datos
     */
    private void updateOrderPaymentStatus(Connection conn, int orderId, String status, double totalPaid) throws SQLException {
        String sql = "UPDATE purchase_orders SET payment_status = ?, total_paid = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setDouble(2, totalPaid);
            stmt.setInt(3, orderId);
            stmt.executeUpdate();
        }
    }

    /**
     * Calcula el total pagado para una orden de compra (versión pública).
     * 
     * @param purchaseOrderId ID de la orden de compra
     * @return Monto total pagado
     */
    public double getTotalPaidForPurchaseOrder(int purchaseOrderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getTotalPaidForPurchaseOrder(conn, purchaseOrderId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al calcular total pagado", e);
            return 0.0;
        }
    }

    /**
     * Registra la recepción de una orden de compra
     * @param orderId ID de la orden de compra
     * @param receiveDate Fecha de recepción
     * @param notes Notas adicionales
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean receivePurchaseOrder(int orderId, Date receiveDate, String notes) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Inicia la transacción

            // Obtener los ítems de la orden de compra
            List<PurchaseOrder.Item> items = getPurchaseOrderItems(conn, orderId);

            if (items.isEmpty()) {
                logger.warning("No items found for purchase order: " + orderId);
                return false;
            }

            // Actualizar el estado de la orden a RECEIVED manteniendo el estado de pago
            String updateOrderSql = "UPDATE purchase_orders SET status = 'RECEIVED', received_date = ?, " +
                    "notes = CONCAT(IFNULL(notes, ''), '\n\nReceived: ', ?) " +
                    "WHERE id = ? AND (status = 'ORDERED' OR status = 'PARTIALLY_RECEIVED')";

            try (PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                stmt.setTimestamp(1, new java.sql.Timestamp(receiveDate.getTime()));
                stmt.setString(2, notes);
                stmt.setInt(3, orderId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    logger.warning("Order not updated: may not exist or not be in ORDERED status - Order ID: " + orderId);
                    conn.rollback();
                    return false;
                }

                // Actualizar inventario y registrar movimientos para cada ítem
                for (PurchaseOrder.Item item : items) {
                    // Actualizar stock
                    updateProductStock(conn, item.getProductId(), item.getQuantity());
                    
                    // Registrar movimiento de inventario
                    registerInventoryMovement(conn, item.getProductId(), item.getQuantity(), "Recepción de orden #" + orderId);
                }

                // Registramos el movimiento en el log de auditoría
                registerAuditEntry(conn, "Orden #" + orderId + " recibida en " + receiveDate + " con " + items.size() + " productos");

                conn.commit();
                success = true;
                logger.info("Purchase order " + orderId + " successfully received");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error receiving purchase order: " + orderId, e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing connection", e);
            }
        }
    }

    /**
     * Registra una entrada en el log de auditoría
     * @param conn Conexión a la base de datos
     * @param message Mensaje para el log
     */
    private void registerAuditEntry(Connection conn, String message) throws SQLException {
        String sql = "INSERT INTO audit_log (action_type, description, user_id, action_date) " +
                    "VALUES ('INVENTORY_UPDATE', ?, 'system', CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // No queremos que falle toda la operación si hay un problema con el log
            logger.warning("Could not write to audit log: " + e.getMessage());
        }
    }

    /**
     * Obtiene los ítems de una orden de compra.
     * 
     * @param conn Conexión a la base de datos
     * @param orderId ID de la orden de compra
     * @return Lista de ítems de la orden
     * @throws SQLException si ocurre un error en la base de datos
     */
    private List<PurchaseOrder.Item> getPurchaseOrderItems(Connection conn, int orderId) throws SQLException {
        List<PurchaseOrder.Item> items = new ArrayList<>();
        String sql = "SELECT product_id, quantity, price FROM purchase_order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    
                    items.add(new PurchaseOrder.Item(productId, quantity, price));
                }
            }
        }
        
        return items;
    }

    /**
     * Actualiza el stock de un producto
     * @param conn Conexión a la base de datos
     * @param productId ID del producto
     * @param quantity Cantidad a añadir al stock
     */
    private void updateProductStock(Connection conn, String productId, int quantity) throws SQLException {
        String updateSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE barcode = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra un movimiento de inventario
     * @param conn Conexión a la base de datos
     * @param productId ID del producto
     * @param quantity Cantidad
     * @param reference Referencia al movimiento
     */
    private void registerInventoryMovement(Connection conn, String productId, int quantity, String reference) throws SQLException {
        String insertSql = "INSERT INTO inventory_movements (product_id, movement_date, quantity, movement_type, reference_info) " +
                          "VALUES (?, CURRENT_TIMESTAMP, ?, 'PURCHASE_RECEIVE', ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, productId);
            stmt.setInt(2, quantity);
            stmt.setString(3, reference);
            stmt.executeUpdate();
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
                    // Recalcular el estado de pago
                    double totalPaid = getTotalPaidForPurchaseOrder(payment.getPurchaseOrderId());
                    double orderAmount = getOrderTotalAmount(payment.getPurchaseOrderId());
                    
                    String newStatus;
                    if (totalPaid > 0) {
                        if (Math.abs(totalPaid - orderAmount) < 0.01) {
                            newStatus = "PAID";
                        } else {
                            newStatus = "PARTIALLY_PAID";
                        }
                    } else {
                        newStatus = "UNPAID";
                    }
                    
                    // Actualizar el estado de la orden
                    try (Connection updateConn = DatabaseConnection.getConnection();
                         PreparedStatement updateStmt = updateConn.prepareStatement(
                            "UPDATE purchase_orders SET payment_status = ?, total_paid = ? WHERE id = ?")) {
                        
                        updateStmt.setString(1, newStatus);
                        updateStmt.setDouble(2, totalPaid);
                        updateStmt.setInt(3, payment.getPurchaseOrderId());
                        updateStmt.executeUpdate();
                    }
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
     * Obtiene el monto total de una orden de compra por ID de la orden.
     * Método de conveniencia que crea y cierra su propia conexión.
     * 
     * @param orderId ID de la orden de compra
     * @return Monto total de la orden
     */
    public double getOrderTotalAmount(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getOrderTotalAmount(conn, orderId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting order total amount", e);
            return 0.0;
        }
    }
}
