-- Tabla de relación muchos a muchos entre productos y proveedores
CREATE TABLE product_suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_barcode VARCHAR(50) NOT NULL,
    supplier_id INT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    purchase_price DECIMAL(10,2),
    last_purchase_date TIMESTAMP NULL,
    FOREIGN KEY (product_barcode) REFERENCES products(barcode) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    UNIQUE KEY (product_barcode, supplier_id)
);

-- Migrar datos existentes para mantener compatibilidad
INSERT INTO product_suppliers (product_barcode, supplier_id, is_primary, purchase_price)
SELECT p.barcode, s.id, TRUE, p.purchase_price
FROM products p
JOIN suppliers s ON p.supplier = s.name
WHERE p.supplier IS NOT NULL AND p.supplier != '';
