-- Additional tables for enhanced inventory management

-- Alter products table to add new fields
ALTER TABLE products 
ADD COLUMN location VARCHAR(100) DEFAULT NULL,
ADD COLUMN sku VARCHAR(50) DEFAULT NULL,
ADD COLUMN expiration_date DATETIME DEFAULT NULL,
ADD COLUMN last_purchase_date DATETIME DEFAULT NULL,
ADD COLUMN active BOOLEAN DEFAULT TRUE,
ADD COLUMN pending_order_quantity INT DEFAULT 0;

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

-- Insert sample supplier data
INSERT INTO suppliers (name, contact_name, phone, email, address, notes) VALUES
('Distribuidora Láctea', 'Juan Pérez', '555-1234', 'contacto@distrileche.com', 'Calle 123, Ciudad', 'Proveedor principal de lácteos'),
('Panadería Local', 'María García', '555-5678', 'info@panaderia.com', 'Av. Principal 456, Ciudad', 'Entrega diaria por la mañana'),
('Distribuidora de Bebidas', 'Pedro López', '555-9012', 'ventas@distribebidas.com', 'Ruta 789, Ciudad', 'Entrega los lunes y jueves'),
('Mayorista Alimentos', 'Ana Martínez', '555-3456', 'pedidos@mayoralimentos.com', 'Calle Industrial 101, Ciudad', 'Pedidos mínimos de $5000'),
('Importadora Gourmet', 'Carlos Rodríguez', '555-7890', 'importaciones@gourmet.com', 'Av. Comercio 202, Ciudad', 'Productos importados premium'),
('Distribuidora Limpieza', 'Laura Sánchez', '555-2345', 'ventas@limpiezatotal.com', 'Calle Comercial 303, Ciudad', 'Descuentos por volumen');

-- Create index for faster barcode lookups
CREATE INDEX idx_products_barcode ON products(barcode);
