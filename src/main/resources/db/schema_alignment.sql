-- Activar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- TABLA USERS (para UserDAO)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    admin_code VARCHAR(50) NULL
);

-- =============================================
-- TABLA PRODUCTS Y RELACIONADAS (para InventoryDAO)
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) UNIQUE NOT NULL,
    sku VARCHAR(30) NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category VARCHAR(100) NULL,
    purchase_price DECIMAL(10, 2) DEFAULT 0.00,
    selling_price DECIMAL(10, 2) DEFAULT 0.00,
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 0,
    location VARCHAR(100) NULL,
    discount DECIMAL(5, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    supplier VARCHAR(255) NULL,
    pending_order_quantity INT DEFAULT 0,
    last_purchase_date DATETIME NULL,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiration_date DATETIME NULL
);

-- Verificar si existen las columnas y añadirlas si no existen
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(100),
    IN p_column_name VARCHAR(100),
    IN p_column_definition VARCHAR(255)
)
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists
    FROM information_schema.columns 
    WHERE table_name = p_table_name AND column_name = p_column_name;
    
    IF column_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Asegurarse que todos los campos necesarios para products existen
CALL add_column_if_not_exists('products', 'pending_order_quantity', 'INT DEFAULT 0');
CALL add_column_if_not_exists('products', 'last_purchase_date', 'DATETIME NULL');
CALL add_column_if_not_exists('products', 'expiration_date', 'DATETIME NULL');
CALL add_column_if_not_exists('products', 'active', 'BOOLEAN DEFAULT TRUE');

-- =============================================
-- TABLA SUPPLIERS (para InventoryDAO)
-- =============================================
CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    notes TEXT NULL,
    last_order_date DATETIME NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Añadir columnas que podrían faltar
CALL add_column_if_not_exists('suppliers', 'active', 'BOOLEAN DEFAULT TRUE');
CALL add_column_if_not_exists('suppliers', 'last_order_date', 'DATETIME NULL');

-- =============================================
-- TABLA PRODUCT_SUPPLIERS (relación muchos a muchos)
-- =============================================
CREATE TABLE IF NOT EXISTS product_suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_barcode VARCHAR(50) NOT NULL,
    supplier_id INT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    purchase_price DECIMAL(10, 2) DEFAULT 0.00,
    last_purchase_date DATETIME NULL,
    FOREIGN KEY (product_barcode) REFERENCES products(barcode) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- =============================================
-- TABLA INVENTORY_MOVEMENTS (para rastreo de movimientos)
-- =============================================
CREATE TABLE IF NOT EXISTS inventory_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    reference_info TEXT NULL,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

-- Asegurar que los nombres de columnas son correctos
-- Renombramos si es necesario las columnas date y reference por los nombres correctos
DROP PROCEDURE IF EXISTS rename_column_if_exists;
DELIMITER //
CREATE PROCEDURE rename_column_if_exists(
    IN p_table_name VARCHAR(100),
    IN p_old_column_name VARCHAR(100),
    IN p_new_column_name VARCHAR(100),
    IN p_column_definition VARCHAR(255)
)
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists
    FROM information_schema.columns 
    WHERE table_name = p_table_name AND column_name = p_old_column_name;
    
    IF column_exists = 1 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' CHANGE COLUMN ', p_old_column_name, ' ', p_new_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL rename_column_if_exists('inventory_movements', 'date', 'movement_date', 'DATETIME DEFAULT CURRENT_TIMESTAMP');
CALL rename_column_if_exists('inventory_movements', 'reference', 'reference_info', 'TEXT NULL');

-- =============================================
-- TABLA PRICE_HISTORY (para historial de precios)
-- =============================================
CREATE TABLE IF NOT EXISTS price_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(10, 2) NOT NULL,
    change_percent DECIMAL(5, 2) DEFAULT 0.00,
    user VARCHAR(50) NULL,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

-- =============================================
-- TABLA PURCHASE_ORDERS (órdenes de compra)
-- =============================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    supplier_name VARCHAR(255) NULL,
    order_date DATETIME NOT NULL,
    expected_date DATETIME NULL,
    received_date DATETIME NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT NULL,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    total_paid DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- Verificar y corregir el nombre de la columna receive_date a received_date
CALL rename_column_if_exists('purchase_orders', 'receive_date', 'received_date', 'DATETIME NULL');

-- Asegurarse que la columna payment_status existe
CALL add_column_if_not_exists('purchase_orders', 'payment_status', 'VARCHAR(20) DEFAULT \'UNPAID\'');
CALL add_column_if_not_exists('purchase_orders', 'total_paid', 'DECIMAL(10, 2) DEFAULT 0.00');
CALL add_column_if_not_exists('purchase_orders', 'supplier_name', 'VARCHAR(255) NULL');

-- =============================================
-- TABLA PURCHASE_ORDER_ITEMS (detalles de orden)
-- =============================================
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

-- Asegurar que la columna subtotal existe
CALL add_column_if_not_exists('purchase_order_items', 'subtotal', 'DECIMAL(10, 2) DEFAULT 0.00');

-- =============================================
-- TABLA PURCHASE_PAYMENTS (pagos a proveedores)
-- =============================================
CREATE TABLE IF NOT EXISTS purchase_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    original_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    is_complete_payment BOOLEAN DEFAULT FALSE,
    reference_number VARCHAR(100) NULL,
    notes TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NULL,
    reconciled BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE
);

-- =============================================
-- TABLAS PARA VENTAS
-- =============================================
CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    product_id VARCHAR(50) NULL,
    quantity INT DEFAULT 1,
    price_per_unit DECIMAL(10, 2) DEFAULT 0.00,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    notes TEXT NULL,
    customer_id INT NULL
);

-- Opcional: Tabla para detalles de venta múltiples
CREATE TABLE IF NOT EXISTS sales_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

-- =============================================
-- TABLAS PARA EMPLEADOS (EmployeeDAO)
-- =============================================
CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_id VARCHAR(30) NOT NULL UNIQUE,
    position VARCHAR(100) NOT NULL,
    base_salary DECIMAL(10, 2) NOT NULL,
    hire_date DATE NOT NULL,
    contact_phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Tabla para los pagos a empleados
CREATE TABLE IF NOT EXISTS employee_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    period VARCHAR(50) NULL,
    description TEXT NULL,
    reference_number VARCHAR(100) NULL,
    reconciled BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- =============================================
-- TABLAS PARA CONTABILIDAD (AccountingDAO)
-- =============================================
CREATE TABLE IF NOT EXISTS accounting_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date DATETIME NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT NULL,
    journal_entry_id INT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') NOT NULL,
    parent_account_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (parent_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS journal_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entry_date DATE NOT NULL,
    reference_number VARCHAR(100) NULL,
    description TEXT NULL,
    is_posted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(50) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS journal_line_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id INT NOT NULL,
    account_id INT NOT NULL,
    description TEXT NULL,
    debit DECIMAL(10, 2) DEFAULT 0.00,
    credit DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Asegurarse que tenemos relaciones correctas entre transacciones y asientos contables
CALL add_column_if_not_exists('accounting_transactions', 'journal_entry_id', 'INT NULL');
ALTER TABLE accounting_transactions ADD CONSTRAINT IF NOT EXISTS fk_transaction_journal 
FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE SET NULL;

-- =============================================
-- INSERTAR CUENTAS CONTABLES BÁSICAS (si no existen)
-- =============================================
INSERT IGNORE INTO accounts (account_code, name, account_type, description) VALUES
('1000', 'Caja', 'ASSET', 'Efectivo disponible'),
('1100', 'Bancos', 'ASSET', 'Fondos en cuentas bancarias'),
('1200', 'Cuentas por Cobrar', 'ASSET', 'Créditos a clientes'),
('1300', 'Inventario', 'ASSET', 'Productos disponibles para venta'),
('2000', 'Cuentas por Pagar', 'LIABILITY', 'Deudas a proveedores'),
('2100', 'Impuestos por Pagar', 'LIABILITY', 'Impuestos pendientes de pago'),
('3000', 'Capital', 'EQUITY', 'Capital de la empresa'),
('4000', 'Ventas', 'REVENUE', 'Ingresos por ventas'),
('5000', 'Costo de Ventas', 'EXPENSE', 'Costo de productos vendidos'),
('6000', 'Gastos Operativos', 'EXPENSE', 'Gastos generales de operación'),
('6100', 'Salarios', 'EXPENSE', 'Pagos a empleados');

-- Restaurar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

-- Eliminar los procedimientos temporales
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS rename_column_if_exists;

-- Mensaje final
SELECT 'Script de alineación de base de datos ejecutado correctamente.' as mensaje;
