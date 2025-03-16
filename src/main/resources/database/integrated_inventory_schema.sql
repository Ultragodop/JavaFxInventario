-- =============================================
-- INTEGRATED INVENTORY DATABASE SCHEMA
-- =============================================
-- This script creates a complete inventory database schema 
-- by combining multiple source scripts into one definitive version.
-- =============================================

-- Drop database if exists and create a new one
DROP DATABASE IF EXISTS inventory_db;
CREATE DATABASE inventory_db;
USE inventory_db;

-- =============================================
-- BASE TABLES
-- =============================================

-- Create products table with comprehensive structure
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    selling_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 5,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    supplier VARCHAR(100),
    location VARCHAR(100) DEFAULT NULL,
    sku VARCHAR(50) DEFAULT NULL,
    expiration_date DATETIME DEFAULT NULL,
    last_purchase_date DATETIME DEFAULT NULL,
    active BOOLEAN DEFAULT TRUE,
    pending_order_quantity INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create sales table for transaction history
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    notes TEXT
);

-- Create sales_items table to track items in each sale
CREATE TABLE sales_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id),
    INDEX (sale_id),
    INDEX (product_id)
);

-- Create suppliers table
CREATE TABLE suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    notes TEXT,
    last_order_date DATETIME DEFAULT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Create purchase_orders table
CREATE TABLE purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    order_date DATETIME NOT NULL,
    expected_date DATETIME DEFAULT NULL,
    received_date DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Create purchase_order_items table
CREATE TABLE purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    received_quantity INT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES purchase_orders(id)
);

-- Create inventory_movements table
CREATE TABLE inventory_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reference_info TEXT,
    movement_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(50) DEFAULT NULL
);

-- Create stock_history table (from inventory_db.sql)
CREATE TABLE stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    cambio INT NOT NULL,
    cambio_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- INDEXES
-- =============================================

-- Create indexes for better performance
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_supplier ON products(supplier);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_movements_product ON inventory_movements(product_id);
CREATE INDEX idx_movements_date ON inventory_movements(movement_date);

-- =============================================
-- SAMPLE DATA
-- =============================================

-- Insert sample product data
INSERT INTO products (barcode, name, description, category, purchase_price, selling_price, stock_quantity, reorder_level, discount, supplier) VALUES
('123456789', 'Leche 1L', 'Leche entera 1 litro', 'Lácteos', 80.00, 120.00, 20, 5, 0.00, 'Distribuidora Láctea'),
('234567890', 'Pan baguette', 'Pan francés 250g', 'Panadería', 50.00, 80.00, 15, 3, 5.00, 'Panadería Local'),
('345678901', 'Queso cremoso 300g', 'Queso cremoso marca premium', 'Lácteos', 180.00, 250.00, 10, 3, 0.00, 'Distribuidora Láctea'),
('456789012', 'Gaseosa Cola 2L', 'Bebida cola 2 litros', 'Bebidas', 90.00, 150.00, 30, 10, 0.00, 'Distribuidora de Bebidas'),
('567890123', 'Arroz 1kg', 'Arroz blanco de grano largo', 'Alimentos secos', 70.00, 110.00, 25, 5, 0.00, 'Mayorista Alimentos'),
('678901234', 'Aceite de oliva 500ml', 'Aceite de oliva extra virgen', 'Aceites', 300.00, 450.00, 8, 3, 0.00, 'Importadora Gourmet'),
('789012345', 'Jabón de tocador', 'Jabón de tocador perfumado 90g', 'Higiene', 40.00, 75.00, 20, 5, 0.00, 'Distribuidora Limpieza'),
('890123456', 'Papel higiénico x4', 'Pack de 4 rollos doble hoja', 'Higiene', 150.00, 220.00, 15, 5, 0.00, 'Distribuidora Limpieza'),
('901234567', 'Detergente 750ml', 'Detergente líquido concentrado', 'Limpieza', 95.00, 160.00, 12, 4, 0.00, 'Distribuidora Limpieza'),
('012345678', 'Galletas surtidas 250g', 'Pack variado de galletas dulces', 'Galletitas', 65.00, 100.00, 18, 5, 10.00, 'Distribuidora Cookies');

-- Insert sample supplier data
INSERT INTO suppliers (name, contact_name, phone, email, address, notes) VALUES
('Distribuidora Láctea', 'Juan Pérez', '555-1234', 'contacto@distrileche.com', 'Calle 123, Ciudad', 'Proveedor principal de lácteos'),
('Panadería Local', 'María García', '555-5678', 'info@panaderia.com', 'Av. Principal 456, Ciudad', 'Entrega diaria por la mañana'),
('Distribuidora de Bebidas', 'Pedro López', '555-9012', 'ventas@distribebidas.com', 'Ruta 789, Ciudad', 'Entrega los lunes y jueves'),
('Mayorista Alimentos', 'Ana Martínez', '555-3456', 'pedidos@mayoralimentos.com', 'Calle Industrial 101, Ciudad', 'Pedidos mínimos de $5000'),
('Importadora Gourmet', 'Carlos Rodríguez', '555-7890', 'importaciones@gourmet.com', 'Av. Comercio 202, Ciudad', 'Productos importados premium'),
('Distribuidora Limpieza', 'Laura Sánchez', '555-2345', 'ventas@limpiezatotal.com', 'Calle Comercial 303, Ciudad', 'Descuentos por volumen'),
('Distribuidora Cookies', 'Roberto Gómez', '555-6789', 'ventas@distcookies.com', 'Av. Industrial 404, Ciudad', 'Especialistas en galletitas');

-- =============================================
-- APPLICATION USER
-- =============================================

-- Create user for the application (if not using root)
CREATE USER IF NOT EXISTS 'inventoryDAO'@'localhost' IDENTIFIED BY '2007absalom';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventoryDAO'@'localhost';
FLUSH PRIVILEGES;

-- =============================================
-- STORED PROCEDURES
-- =============================================

-- Procedure to get low stock products
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS GetLowStockProducts()
BEGIN
    SELECT * FROM products WHERE stock_quantity <= reorder_level AND active = TRUE;
END //
DELIMITER ;

-- Procedure to add inventory movement and update stock
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS RecordInventoryMovement(
    IN p_product_id VARCHAR(50), 
    IN p_movement_type VARCHAR(20),
    IN p_quantity INT,
    IN p_reference TEXT,
    IN p_user_id VARCHAR(50)
)
BEGIN
    -- Insert the movement record
    INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_info, user_id)
    VALUES (p_product_id, p_movement_type, p_quantity, p_reference, p_user_id);
    
    -- Update product stock
    IF p_movement_type = 'SALE' THEN
        UPDATE products SET stock_quantity = stock_quantity - ABS(p_quantity) WHERE barcode = p_product_id;
    ELSEIF p_movement_type = 'PURCHASE_RECEIVED' OR p_movement_type = 'ADDITION' THEN
        UPDATE products SET stock_quantity = stock_quantity + p_quantity WHERE barcode = p_product_id;
    ELSEIF p_movement_type = 'ADJUSTMENT' THEN
        UPDATE products SET stock_quantity = stock_quantity + p_quantity WHERE barcode = p_product_id;
    END IF;
    
    -- Record in stock history
    INSERT INTO stock_history (product_id, cambio) VALUES (p_product_id, p_quantity);
END //
DELIMITER ;

-- =============================================
-- END OF SCRIPT
-- =============================================
