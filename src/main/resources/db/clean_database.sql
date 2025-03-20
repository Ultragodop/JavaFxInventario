-- ¡ADVERTENCIA! ESTE SCRIPT ELIMINARÁ TODOS LOS DATOS DE LA BASE DE DATOS
-- HAGA UNA COPIA DE SEGURIDAD ANTES DE EJECUTAR
-- Solo ejecute esto en un entorno de desarrollo o pruebas
-- o cuando realmente desee purgar todos los datos

-- Desactivar restricciones de clave foránea temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar tablas principales
TRUNCATE TABLE journal_line_items;
TRUNCATE TABLE journal_entries;
TRUNCATE TABLE accounting_transactions;
TRUNCATE TABLE inventory_movements;
TRUNCATE TABLE sales_items;
TRUNCATE TABLE sales;
TRUNCATE TABLE purchase_payments;
TRUNCATE TABLE purchase_order_items;
TRUNCATE TABLE purchase_orders;
TRUNCATE TABLE employee_payments;
TRUNCATE TABLE employees;
TRUNCATE TABLE product_suppliers;
TRUNCATE TABLE price_history;
TRUNCATE TABLE products;
TRUNCATE TABLE suppliers;
TRUNCATE TABLE accounts;
TRUNCATE TABLE users;

-- Reiniciar contadores de autoincremento
ALTER TABLE journal_entries AUTO_INCREMENT = 1;
ALTER TABLE accounting_transactions AUTO_INCREMENT = 1;
ALTER TABLE inventory_movements AUTO_INCREMENT = 1;
ALTER TABLE sales AUTO_INCREMENT = 1;
ALTER TABLE sales_items AUTO_INCREMENT = 1;
ALTER TABLE purchase_payments AUTO_INCREMENT = 1;
ALTER TABLE purchase_orders AUTO_INCREMENT = 1;
ALTER TABLE purchase_order_items AUTO_INCREMENT = 1;
ALTER TABLE employee_payments AUTO_INCREMENT = 1;
ALTER TABLE employees AUTO_INCREMENT = 1;
ALTER TABLE product_suppliers AUTO_INCREMENT = 1;
ALTER TABLE price_history AUTO_INCREMENT = 1;
ALTER TABLE suppliers AUTO_INCREMENT = 1;

-- Reactivar restricciones de clave foránea
SET FOREIGN_KEY_CHECKS = 1;

-- Insertar datos iniciales necesarios para el funcionamiento del sistema

-- Reinsertar cuentas contables básicas
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

-- Reinsertar usuario admin por defecto
INSERT INTO users (username, password, role, admin_code) VALUES
('admin', '$2a$10$dMl1i0O6RcomQGPrzKRgne8iBl1rOf2VGCgdC1U8EnO7LSy6XInFu', 'admin', 'ADMIN2023');

-- Mensaje de finalización
SELECT 'Base de datos limpiada exitosamente. Todos los datos han sido eliminados.' AS mensaje;
