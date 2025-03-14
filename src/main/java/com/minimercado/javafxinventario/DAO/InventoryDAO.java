package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Product;
import com.minimercado.javafxinventario.modules.Supplier;
import com.minimercado.javafxinventario.modules.PurchaseOrder;
import com.minimercado.javafxinventario.modules.InventoryMovement;
import com.minimercado.javafxinventario.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for inventory operations.
 * Provides methods to interact with the inventory data storage.
 */
public class InventoryDAO {
    
    /**
     * Gets a product from the database by its barcode
     * @param barcode The product barcode to search for
     * @return Product object if found, null otherwise
     */
    public Product getProductByBarcode(String barcode) {
        String sql = "SELECT * FROM products WHERE barcode = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product by barcode: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Searches for products by name or description
     * @param query The search term
     * @return List of matching Product objects
     */
    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        String searchQuery = "%" + query + "%";
        String sql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, searchQuery);
            stmt.setString(2, searchQuery);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Updates the stock quantity for a product
     * @param barcode The product barcode
     * @param quantityChange The amount to adjust stock (positive or negative)
     * @return true if update was successful, false otherwise
     */
    public boolean updateProductStock(String barcode, int quantityChange) {
        // If reducing stock, check if there's enough available first
        if (quantityChange < 0) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement("SELECT stock_quantity FROM products WHERE barcode = ?")) {
                
                checkStmt.setString(1, barcode);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    int currentStock = rs.getInt("stock_quantity");
                    if (currentStock + quantityChange < 0) {
                        System.err.println("Error: Stock insuficiente para " + barcode + ". Stock actual: " + currentStock);
                        return false;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error verificando stock: " + e.getMessage());
                return false;
            }
        }
        
        // Original update logic - only executed if there's enough stock
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ?, last_updated = NOW() WHERE barcode = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantityChange);
            stmt.setString(2, barcode);
            
            int rowsAffected = stmt.executeUpdate();
            
            // Record the movement in inventory_movements table for tracking
            if (rowsAffected > 0) {
                String movementType = quantityChange > 0 ? "ADDITION" : "SALE";
                String reference = quantityChange > 0 ? "Manual addition" : "Sale transaction";
                recordInventoryMovement(barcode, movementType, quantityChange, reference);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error updating product stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all products from the database
     * @return List of all Product objects
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // Fix the query to avoid using 'name' column that doesn't exist
        String sql = "SELECT * FROM products";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Maps a database result set to a Product object
     * @param rs The result set containing product data
     * @return A new Product object
     * @throws SQLException if there's an error accessing the result set
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setBarcode(rs.getString("barcode"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setCategory(rs.getString("category"));
        product.setPurchasePrice(rs.getDouble("purchase_price"));
        product.setSellingPrice(rs.getDouble("selling_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setDiscount(rs.getDouble("discount"));
        product.setSupplier(rs.getString("supplier"));
        product.setLastUpdated(rs.getTimestamp("last_updated"));
        
        // New fields (removed location mapping to resolve error)
        // product.setLocation(rs.getString("location"));
        product.setSku(rs.getString("sku"));
        Timestamp expDate = rs.getTimestamp("expiration_date");
        if (expDate != null) {
            product.setExpirationDate(new Date(expDate.getTime()));
        }
        Timestamp lastPurchase = rs.getTimestamp("last_purchase_date");
        if (lastPurchase != null) {
            product.setLastPurchaseDate(new Date(lastPurchase.getTime()));
        }
        product.setActive(rs.getBoolean("active"));
        product.setPendingOrderQuantity(rs.getInt("pending_order_quantity"));
        
        return product;
    }
    
    /**
     * Adds a new product to the database
     * @param product The Product object to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (barcode, name, description, category, purchase_price, " +
                    "selling_price, stock_quantity, reorder_level, discount, supplier) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setString(4, product.getCategory());
            stmt.setDouble(5, product.getPurchasePrice());
            stmt.setDouble(6, product.getSellingPrice());
            stmt.setInt(7, product.getStockQuantity());
            stmt.setInt(8, product.getReorderLevel());
            stmt.setDouble(9, product.getDiscount());
            stmt.setString(10, product.getSupplier());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates an existing product in the database
     * @param product The Product object with updated information
     * @return true if update was successful, false otherwise
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, category = ?, " +
                    "purchase_price = ?, selling_price = ?, stock_quantity = ?, " +
                    "reorder_level = ?, discount = ?, supplier = ? " +
                    "WHERE barcode = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPurchasePrice());
            stmt.setDouble(5, product.getSellingPrice());
            stmt.setInt(6, product.getStockQuantity());
            stmt.setInt(7, product.getReorderLevel());
            stmt.setDouble(8, product.getDiscount());
            stmt.setString(9, product.getSupplier());
            stmt.setString(10, product.getBarcode());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a product from the database
     * @param barcode The barcode of the product to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteProduct(String barcode) {
        String sql = "DELETE FROM products WHERE barcode = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Supplier Management
    
    /**
     * Add a new supplier to the database
     * @param supplier The supplier to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact_name, phone, email, address, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactName());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getAddress());
            stmt.setString(6, supplier.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates a supplier in the database
     * @param supplier The supplier object with updated information
     * @return true if update was successful, false otherwise
     */
    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_name = ?, phone = ?, " +
                     "email = ?, address = ?, notes = ?, active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactName());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getAddress());
            stmt.setString(6, supplier.getNotes());
            stmt.setBoolean(7, supplier.isActive());
            stmt.setInt(8, supplier.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a supplier from the database
     * @param supplierId The ID of the supplier to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all suppliers from the database
     * @return List of suppliers
     */
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContactName(rs.getString("contact_name"));
                supplier.setPhone(rs.getString("phone"));
                supplier.setEmail(rs.getString("email"));
                supplier.setAddress(rs.getString("address"));
                supplier.setNotes(rs.getString("notes"));
                supplier.setLastOrderDate(rs.getTimestamp("last_order_date"));
                
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            System.err.println("Error getting suppliers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return suppliers;
    }
    
    /**
     * Get supplier by ID
     * @param id The supplier ID
     * @return The Supplier object or null if not found
     */
    public Supplier getSupplier(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContactName(rs.getString("contact_name"));
                supplier.setPhone(rs.getString("phone"));
                supplier.setEmail(rs.getString("email"));
                supplier.setAddress(rs.getString("address"));
                supplier.setNotes(rs.getString("notes"));
                supplier.setLastOrderDate(rs.getTimestamp("last_order_date"));
                
                return supplier;
            }
        } catch (SQLException e) {
            System.err.println("Error getting supplier: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Purchase Orders
    
    /**
     * Create a new purchase order
     * @param order The purchase order to create
     * @return true if creation was successful, false otherwise
     */
    public boolean createPurchaseOrder(PurchaseOrder order) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert purchase order header
            String headerSql = "INSERT INTO purchase_orders (supplier_id, order_date, expected_date, status, total_amount) " +
                              "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getSupplierId());
                stmt.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
                stmt.setTimestamp(3, order.getExpectedDate() != null ? 
                                  new Timestamp(order.getExpectedDate().getTime()) : null);
                stmt.setString(4, order.getStatus());
                stmt.setDouble(5, order.getTotalAmount());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating purchase order failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating purchase order failed, no ID obtained.");
                    }
                }
            }
            
            // Insert purchase order items
            String itemSql = "INSERT INTO purchase_order_items (order_id, product_id, quantity, price, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (PurchaseOrder.Item item : order.getItems()) {
                    stmt.setInt(1, order.getId());
                    stmt.setString(2, item.getProductId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getPrice());
                    stmt.setDouble(5, item.getSubtotal());
                    stmt.addBatch();
                    
                    // Update pending order quantity for product
                    updatePendingOrderQuantity(conn, item.getProductId(), item.getQuantity());
                }
                stmt.executeBatch();
            }
            
            // Update supplier last order date
            String supplierSql = "UPDATE suppliers SET last_order_date = NOW() WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(supplierSql)) {
                stmt.setInt(1, order.getSupplierId());
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error creating purchase order: " + e.getMessage());
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
     * Update pending order quantity for a product
     * @param conn Database connection
     * @param productId Product ID
     * @param quantity Quantity to add to pending
     * @throws SQLException if an error occurs
     */
    private void updatePendingOrderQuantity(Connection conn, String productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET pending_order_quantity = pending_order_quantity + ? WHERE barcode = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Receive a purchase order (update status and add stock)
     * @param orderId Purchase order ID
     * @param receivedDate Date received
     * @param notes Receiving notes
     * @return true if received successfully, false otherwise
     */
    public boolean receivePurchaseOrder(int orderId, Date receivedDate, String notes) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Get purchase order items
            List<PurchaseOrder.Item> items = getPurchaseOrderItems(conn, orderId);
            
            // Update purchase order status
            String updateOrderSql = "UPDATE purchase_orders SET status = 'RECEIVED', received_date = ?, notes = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateOrderSql)) {
                stmt.setTimestamp(1, new Timestamp(receivedDate.getTime()));
                stmt.setString(2, notes);
                stmt.setInt(3, orderId);
                stmt.executeUpdate();
            }
            
            // Update product stock and pending quantities
            for (PurchaseOrder.Item item : items) {
                // Add to stock
                String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity + ?, " +
                                       "pending_order_quantity = pending_order_quantity - ?, " +
                                       "last_purchase_date = ? WHERE barcode = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getQuantity());
                    stmt.setTimestamp(3, new Timestamp(receivedDate.getTime()));
                    stmt.setString(4, item.getProductId());
                    stmt.executeUpdate();
                }
                
                // Record inventory movement
                recordInventoryMovement(conn, item.getProductId(), "PURCHASE_RECEIVED", item.getQuantity(), 
                                       "Purchase order #" + orderId + " received");
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error receiving purchase order: " + e.getMessage());
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
     * Get items for a purchase order
     * @param conn Database connection
     * @param orderId Purchase order ID
     * @return List of order items
     * @throws SQLException if an error occurs
     */
    private List<PurchaseOrder.Item> getPurchaseOrderItems(Connection conn, int orderId) throws SQLException {
        List<PurchaseOrder.Item> items = new ArrayList<>();
        String sql = "SELECT * FROM purchase_order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                PurchaseOrder.Item item = new PurchaseOrder.Item(
                    rs.getString("product_id"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                );
                items.add(item);
            }
        }
        
        return items;
    }
    
    // Inventory Movements
    
    /**
     * Record an inventory movement
     * @param conn Database connection
     * @param productId Product ID
     * @param type Movement type (PURCHASE, SALE, ADJUSTMENT, etc.)
     * @param quantity Quantity change (positive for increases, negative for decreases)
     * @param reference Reference information
     * @throws SQLException if an error occurs
     */
    private void recordInventoryMovement(Connection conn, String productId, String type, int quantity, String reference) throws SQLException {
        String sql = "INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_info, movement_date) " +
                    "VALUES (?, ?, ?, ?, NOW())";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            stmt.setString(2, type);
            stmt.setInt(3, quantity);
            stmt.setString(4, reference);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Record inventory movement (with new connection)
     * @param productId Product ID
     * @param type Movement type
     * @param quantity Quantity change
     * @param reference Reference information
     * @return true if recorded successfully, false otherwise
     */
    public boolean recordInventoryMovement(String productId, String type, int quantity, String reference) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            recordInventoryMovement(conn, productId, type, quantity, reference);
            return true;
        } catch (SQLException e) {
            System.err.println("Error recording inventory movement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get inventory movement history for a product
     * @param productId Product ID
     * @return List of inventory movements
     */
    public List<InventoryMovement> getInventoryMovements(String productId) {
        List<InventoryMovement> movements = new ArrayList<>();
        String sql = "SELECT * FROM inventory_movements WHERE product_id = ? ORDER BY movement_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                InventoryMovement movement = new InventoryMovement();
                movement.setId(rs.getInt("id"));
                movement.setProductId(rs.getString("product_id"));
                movement.setMovementType(rs.getString("movement_type"));
                movement.setQuantity(rs.getInt("quantity"));
                movement.setReference(rs.getString("reference_info"));
                movement.setDate(rs.getTimestamp("movement_date"));
                
                movements.add(movement);
            }
        } catch (SQLException e) {
            System.err.println("Error getting inventory movements: " + e.getMessage());
            e.printStackTrace();
        }
        
        return movements;
    }
    
    /**
     * Gets inventory movements for a specific product
     * @param productId The product ID to search for
     * @return List of inventory movements
     */
    public List<InventoryMovement> getMovementsByProduct(String productId) {
        List<InventoryMovement> movements = new ArrayList<>();
        
        // Implement database query or return empty list for now
        // This is a stub implementation
        
        return movements;
    }
    
    /**
     * Get products that are expiring soon
     * @param daysThreshold Number of days to consider "soon"
     * @return List of products expiring within the threshold
     */
    public List<Product> getExpiringProducts(int daysThreshold) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE expiration_date IS NOT NULL " +
                    "AND expiration_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, daysThreshold);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting expiring products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Get low stock products (products below reorder level)
     * @return List of low stock products
     */
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity <= reorder_level AND active = 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Generate a comparison of prices from different suppliers
     * @param productCategory Product category to compare
     * @return List of products with prices from different suppliers
     */
    public List<ProductSupplierPrice> getSupplierPriceComparison(String productCategory) {
        List<ProductSupplierPrice> comparison = new ArrayList<>();
        String sql = "SELECT p.name, p.category, s.name as supplier_name, p.purchase_price " +
                    "FROM products p JOIN suppliers s ON p.supplier = s.name " +
                    "WHERE p.category = ? ORDER BY p.name, p.purchase_price";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCategory);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ProductSupplierPrice psp = new ProductSupplierPrice(
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("supplier_name"),
                    rs.getDouble("purchase_price")
                );
                comparison.add(psp);
            }
        } catch (SQLException e) {
            System.err.println("Error getting supplier price comparison: " + e.getMessage());
            e.printStackTrace();
        }
        
        return comparison;
    }
    
    /**
     * Inner class for supplier price comparisons
     */
    public static class ProductSupplierPrice {
        private String productName;
        private String category;
        private String supplier;
        private double price;
        
        public ProductSupplierPrice(String productName, String category, String supplier, double price) {
            this.productName = productName;
            this.category = category;
            this.supplier = supplier;
            this.price = price;
        }
        
        // Getters
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public String getSupplier() { return supplier; }
        public double getPrice() { return price; }
    }

    /**
     * Get distinct categories from products
     * @return List of distinct categories
     */
    public List<String> getDistinctCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category <> '' ORDER BY category";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }

    /**
     * Gets products that have had no movement (sales) for a specified period
     * @param days Number of days to check for no movement (default 30)
     * @return List of products without movement
     */
    public List<Product> getProductsWithoutMovement(int days) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM products p WHERE p.active = 1 AND " +
                    "(p.barcode NOT IN (SELECT DISTINCT product_id FROM inventory_movements " +
                    "WHERE movement_type = 'SALE' AND movement_date >= DATE_SUB(NOW(), INTERVAL ? DAY)) " +
                    "OR p.barcode NOT IN (SELECT DISTINCT product_id FROM inventory_movements))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting products without movement: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Gets products without movement using default period of 30 days
     * @return List of products without movement in the last 30 days
     */
    public List<Product> getProductsWithoutMovement() {
        return getProductsWithoutMovement(30);
    }

    /**
     * Gets the last entry date (purchase or addition) for a product
     * @param barcode Product barcode
     * @return Date of last entry or null if no entry found
     */
    public Date getLastEntryDate(String barcode) {
        String sql = "SELECT MAX(movement_date) as last_entry FROM inventory_movements " +
                    "WHERE product_id = ? AND (movement_type = 'PURCHASE_RECEIVED' OR movement_type = 'ADDITION')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getTimestamp("last_entry");
            }
        } catch (SQLException e) {
            System.err.println("Error getting last entry date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets the last sale date for a product
     * @param barcode Product barcode
     * @return Date of last sale or null if no sale found
     */
    public Date getLastSaleDate(String barcode) {
        String sql = "SELECT MAX(movement_date) as last_sale FROM inventory_movements " +
                    "WHERE product_id = ? AND movement_type = 'SALE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getTimestamp("last_sale");
            }
        } catch (SQLException e) {
            System.err.println("Error getting last sale date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
