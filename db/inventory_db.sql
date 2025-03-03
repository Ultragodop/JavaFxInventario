CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    stock INT NOT NULL,
    threshold INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

CREATE TABLE stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(50),
    change INT NOT NULL,
    change_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
