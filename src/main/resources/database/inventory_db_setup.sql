-- Drop database if exists and create a new one
DROP DATABASE IF EXISTS inventory_db;
CREATE DATABASE inventory_db;
USE inventory_db;

-- Create products table matching the Product class structure
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

-- Create user for the application (if not using root)
CREATE USER IF NOT EXISTS 'inventoryDAO'@'localhost' IDENTIFIED BY '2007absalom';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventoryDAO'@'localhost';
FLUSH PRIVILEGES;
