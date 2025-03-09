-- MySQL Database creation script for JavaFX Inventory Management System

-- Drop database if it exists for a clean start
DROP DATABASE IF EXISTS inventory_management;

-- Create database with proper character encoding
CREATE DATABASE inventory_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the new database
USE inventory_management;

-- Create suppliers table
CREATE TABLE suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    notes TEXT,
    last_order_date TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (name)
) ENGINE=InnoDB;

-- Create products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) NOT NULL,
    sku VARCHAR(50),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    purchase_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    selling_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 0,
    discount DECIMAL(10,2) DEFAULT 0,
    supplier VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP NULL,
    last_purchase_date TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    pending_order_quantity INT NOT NULL DEFAULT 0,
    UNIQUE KEY (barcode),
    INDEX idx_product_name (name),
    INDEX idx_product_category (category),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier) 
        REFERENCES suppliers(name) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- Create purchase_orders table
CREATE TABLE purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_date TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    received_date TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_supplier FOREIGN KEY (supplier_id) 
        REFERENCES suppliers(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Create purchase_order_items table
CREATE TABLE purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) 
        REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) 
        REFERENCES products(barcode) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Create inventory_movements table
CREATE TABLE inventory_movements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    reference_info TEXT,
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    CONSTRAINT fk_movement_product FOREIGN KEY (product_id)
        REFERENCES products(barcode) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Create users table (optional - for user authentication)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (username)
) ENGINE=InnoDB;

-- Insert sample data for testing

-- Sample suppliers
INSERT INTO suppliers (name, contact_name, phone, email, address, notes) VALUES
('Distribuidora Láctea', 'Juan Pérez', '555-1234', 'juan@distlactea.com', 'Av. Principal 123', 'Proveedor de lácteos'),
('Frutas del Valle', 'María Gómez', '555-5678', 'maria@frutasdelvalle.com', 'Calle Secundaria 456', 'Proveedor de frutas y verduras'),
('Abarrotes Express', 'Carlos Rodríguez', '555-9012', 'carlos@abarrotesexpress.com', 'Blvd. Central 789', 'Proveedor de abarrotes generales');

-- Sample products
INSERT INTO products (barcode, sku, name, description, category, purchase_price, selling_price, stock_quantity, reorder_level, discount, supplier) VALUES
('7501055363513', 'LC-001', 'Leche Entera 1L', 'Leche entera en envase de cartón de 1 litro', 'Lácteos', 18.50, 24.90, 50, 10, 0, 'Distribuidora Láctea'),
('7501055363520', 'LC-002', 'Yogurt Natural 1Kg', 'Yogurt natural sin azúcar', 'Lácteos', 28.00, 35.50, 30, 5, 0, 'Distribuidora Láctea'),
('7503030212016', 'FR-001', 'Manzana Roja Kg', 'Manzana roja fresca por kilogramo', 'Frutas', 22.00, 32.90, 100, 15, 0, 'Frutas del Valle'),
('7503030212023', 'FR-002', 'Plátano Kg', 'Plátano fresco por kilogramo', 'Frutas', 12.50, 18.90, 80, 10, 0, 'Frutas del Valle'),
('7506195124452', 'AB-001', 'Azúcar 1Kg', 'Azúcar estándar en bolsa de 1kg', 'Abarrotes', 20.00, 26.50, 40, 10, 0, 'Abarrotes Express'),
('7506195124469', 'AB-002', 'Arroz 1Kg', 'Arroz blanco grano largo en bolsa de 1kg', 'Abarrotes', 18.00, 23.90, 35, 8, 0, 'Abarrotes Express');

-- Sample purchase order
INSERT INTO purchase_orders (supplier_id, order_date, expected_date, status, total_amount) VALUES
(1, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'ORDERED', 1850.00);

-- Sample purchase order items
INSERT INTO purchase_order_items (order_id, product_id, quantity, price, subtotal) VALUES
(1, '7501055363513', 50, 18.50, 925.00),
(1, '7501055363520', 33, 28.00, 924.00);

-- Sample inventory movements
INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_info) VALUES
('7501055363513', 'INITIAL_STOCK', 50, 'Initial inventory setup'),
('7501055363520', 'INITIAL_STOCK', 30, 'Initial inventory setup'),
('7503030212016', 'INITIAL_STOCK', 100, 'Initial inventory setup'),
('7503030212023', 'INITIAL_STOCK', 80, 'Initial inventory setup'),
('7506195124452', 'INITIAL_STOCK', 40, 'Initial inventory setup'),
('7506195124469', 'INITIAL_STOCK', 35, 'Initial inventory setup');
