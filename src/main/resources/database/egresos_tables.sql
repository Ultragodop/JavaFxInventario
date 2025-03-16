-- Script para crear las tablas del sistema de egresos

-- Tabla de empleados
CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    document_id TEXT NOT NULL UNIQUE,
    position TEXT,
    base_salary REAL NOT NULL DEFAULT 0,
    hire_date DATE NOT NULL,
    contact_phone TEXT,
    email TEXT,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT 1
);

-- Tabla de pagos a empleados
CREATE TABLE IF NOT EXISTS employee_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    payment_date DATE NOT NULL,
    amount REAL NOT NULL DEFAULT 0,
    payment_type TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    period TEXT,
    description TEXT,
    reconciled BOOLEAN NOT NULL DEFAULT 0,
    reference_number TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Tabla de categorías de gastos
CREATE TABLE IF NOT EXISTS expense_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    account_code TEXT
);

-- Tabla de gastos generales
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    amount REAL NOT NULL DEFAULT 0,
    expense_date DATE NOT NULL,
    description TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    receipt_number TEXT,
    supplier TEXT,
    receipt_image_path TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT,
    reconciled BOOLEAN NOT NULL DEFAULT 0,
    notes TEXT,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id)
);

-- Tabla de pagos de órdenes de compra
CREATE TABLE IF NOT EXISTS purchase_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    purchase_order_id INTEGER NOT NULL,
    payment_date DATE NOT NULL,
    amount REAL NOT NULL DEFAULT 0,
    original_amount REAL NOT NULL DEFAULT 0,
    payment_method TEXT NOT NULL,
    complete_payment BOOLEAN NOT NULL DEFAULT 0,
    reference_number TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT,
    reconciled BOOLEAN NOT NULL DEFAULT 0,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
);

-- Insertar categorías de gastos básicas
INSERT OR IGNORE INTO expense_categories (name, description, account_code) VALUES
('Alquileres', 'Pagos por alquiler de local', '5100'),
('Servicios Públicos', 'Electricidad, agua, gas, internet, etc.', '5200'),
('Salarios', 'Pagos a empleados', '5300'),
('Impuestos', 'Pagos de impuestos diversos', '5400'),
('Mantenimiento', 'Reparaciones y mantenimiento de local y equipos', '5500'),
('Papelería', 'Gastos de oficina y papelería', '5600'),
('Marketing', 'Gastos de publicidad y marketing', '5700'),
('Transporte', 'Gastos de transporte y logística', '5800'),
('Bancarios', 'Comisiones bancarias y financieras', '5900'),
('Otros', 'Gastos varios no categorizados', '6000');
