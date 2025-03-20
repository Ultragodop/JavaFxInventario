
-- Eliminar la base de datos existente
DROP DATABASE IF EXISTS inventory_db;

-- Crear nueva base de datos
CREATE DATABASE inventory_db;

-- Conectarse a la base de datos recién creada
\c inventory_db;

-- Desactivar restricciones de clave foránea temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- TABLAS PARA USUARIOS (UserDAO)
-- =============================================
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    admin_code VARCHAR(50) NULL
);

-- =============================================
-- TABLAS PARA INVENTARIO (InventoryDAO)
-- =============================================
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) UNIQUE NOT NULL,
    sku VARCHAR(30) NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    category VARCHAR(100) NULL,
    purchase_price DECIMAL(10, 2) DEFAULT 0.00,
    selling_price DECIMAL(10, 2) DEFAULT 0.00,
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 5,
    location VARCHAR(100) NULL,
    discount DECIMAL(5, 2) DEFAULT 0.00,
    active BOOLEAN DEFAULT TRUE,
    supplier VARCHAR(255) NULL,
    pending_order_quantity INT DEFAULT 0,
    last_purchase_date DATETIME NULL,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiration_date DATETIME NULL
);

CREATE TABLE suppliers (
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

CREATE TABLE product_suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_barcode VARCHAR(50) NOT NULL,
    supplier_id INT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    purchase_price DECIMAL(10, 2) DEFAULT 0.00,
    last_purchase_date DATETIME NULL,
    FOREIGN KEY (product_barcode) REFERENCES products(barcode) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE TABLE inventory_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    reference_info TEXT NULL,
    user_id VARCHAR(50) NULL,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

CREATE TABLE price_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(10, 2) NOT NULL,
    change_percent DECIMAL(5, 2) DEFAULT 0.00,
    user VARCHAR(50) NULL,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

CREATE TABLE stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    cambio INT NOT NULL,
    cambio_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

-- =============================================
-- TABLAS PARA ÓRDENES DE COMPRA (PurchasePaymentDAO)
-- =============================================
CREATE TABLE purchase_orders (
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

CREATE TABLE purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) DEFAULT 0.00,
    received_quantity INT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE CASCADE
);

CREATE TABLE purchase_payments (
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
-- TABLAS PARA VENTAS (SalesDAO)
-- =============================================
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    product_id VARCHAR(50) NULL,
    quantity INT DEFAULT 1,
    price_per_unit DECIMAL(10, 2) DEFAULT 0.00,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    notes TEXT NULL,
    customer_id INT NULL,
    FOREIGN KEY (product_id) REFERENCES products(barcode) ON DELETE SET NULL
);

CREATE TABLE sales_items (
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
CREATE TABLE employees (
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

CREATE TABLE employee_payments (
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
-- TABLAS PARA GASTOS
-- =============================================
CREATE TABLE expense_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    account_code VARCHAR(20) NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    receipt_number VARCHAR(100) NULL,
    supplier VARCHAR(100) NULL,
    receipt_image_path VARCHAR(255) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NULL,
    reconciled BOOLEAN DEFAULT FALSE,
    notes TEXT NULL,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT
);

-- =============================================
-- TABLAS PARA CONTABILIDAD (AccountingDAO)
-- =============================================
CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') NOT NULL,
    parent_account_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE TABLE journal_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entry_date DATE NOT NULL,
    reference_number VARCHAR(100) NULL,
    description TEXT NULL,
    is_posted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(50) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE journal_line_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id INT NOT NULL,
    account_id INT NOT NULL,
    description TEXT NULL,
    debit DECIMAL(10, 2) DEFAULT 0.00,
    credit DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE accounting_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date DATETIME NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT NULL,
    journal_entry_id INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE SET NULL
);

CREATE TABLE fiscal_periods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type ENUM('MONTH', 'QUARTER', 'YEAR') NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    closed_at DATETIME NULL,
    closed_by VARCHAR(50) NULL
);

-- =============================================
-- INSERTAR DATOS INICIALES
-- =============================================

-- Usuario administrador por defecto
INSERT INTO users (username, password, role, admin_code) VALUES
('admin', '$2a$10$dMl1i0O6RcomQGPrzKRgne8iBl1rOf2VGCgdC1U8EnO7LSy6XInFu', 'admin', 'ADMIN2023');

-- Inserta categorías de gastos básicas
INSERT INTO expense_categories (name, description, account_code) VALUES
('Alquileres', 'Pagos por alquiler de local', '6100'),
('Servicios Públicos', 'Electricidad, agua, gas, internet, etc.', '6200'),
('Salarios', 'Pagos a empleados', '6300'),
('Impuestos', 'Pagos de impuestos diversos', '6400'),
('Mantenimiento', 'Reparaciones y mantenimiento de local y equipos', '6500'),
('Papelería', 'Gastos de oficina y papelería', '6600'),
('Marketing', 'Gastos de publicidad y marketing', '6700'),
('Transporte', 'Gastos de transporte y logística', '6800'),
('Bancarios', 'Comisiones bancarias y financieras', '6900');

-- Cuentas contables básicas
INSERT INTO accounts (account_code, name, account_type, description) VALUES
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

-- Reactivar restricciones de clave foránea
SET FOREIGN_KEY_CHECKS = 1;

-- Datos de prueba para productos
INSERT INTO products (barcode, name, description, category, purchase_price, selling_price, stock_quantity, reorder_level, discount, active) VALUES
('7790070411365', 'Leche La Serenísima 1L', 'Leche entera La Serenísima 1 litro', 'Lácteos', 150.00, 200.00, 25, 10, 0.00, TRUE),
('7790070225551', 'Yogurt Ser Frutilla 190g', 'Yogurt Ser sabor frutilla 190g', 'Lácteos', 90.00, 130.00, 15, 5, 0.00, TRUE),
('7790387012389', 'Arroz Gallo Oro 1kg', 'Arroz Gallo Oro doble carolina 1kg', 'Alimentos secos', 180.00, 250.00, 30, 10, 0.00, TRUE),
('7790387008221', 'Fideos Matarazzo 500g', 'Fideos tallarín Matarazzo 500g', 'Alimentos secos', 120.00, 170.00, 20, 8, 0.00, TRUE),
('7790580600262', 'Aceite Natura 900ml', 'Aceite de girasol Natura 900ml', 'Aceites', 200.00, 280.00, 15, 5, 0.00, TRUE),
('7790580634206', 'Aceite Cocinero 1.5L', 'Aceite mezcla Cocinero 1.5 litros', 'Aceites', 320.00, 450.00, 10, 4, 0.00, TRUE),
('7790004009135', 'Coca Cola 2.25L', 'Gaseosa Coca Cola 2.25 litros', 'Bebidas', 250.00, 320.00, 40, 15, 0.00, TRUE),
('7790004000422', 'Sprite 2.25L', 'Gaseosa Sprite 2.25 litros', 'Bebidas', 230.00, 300.00, 30, 12, 0.00, TRUE);

-- Datos de prueba para proveedores
INSERT INTO suppliers (name, contact_name, phone, email, address, notes, active) VALUES
('Distribuidora Lácteos SA', 'Juan Pérez', '11-4567-8901', 'contacto@distlacteos.com', 'Av. Rivadavia 1234, CABA', 'Proveedor principal de lácteos', TRUE),
('Alimentos del Sur', 'María López', '11-2345-6789', 'ventas@alimentosdelsur.com', 'Calle 25 de Mayo 789, Quilmes', 'Productos secos y enlatados', TRUE),
('Bebidas Argentinas', 'Carlos Gómez', '11-9876-5432', 'pedidos@bebidasarg.com', 'Av. Corrientes 5678, CABA', 'Gaseosas y aguas minerales', TRUE),
('Importadora Mayorista', 'Ana Rodríguez', '11-5678-1234', 'compras@impormayorista.com', 'Calle San Martín 456, Avellaneda', 'Productos importados', TRUE);

-- Mensaje final
SELECT 'Base de datos recreada correctamente' AS message;
