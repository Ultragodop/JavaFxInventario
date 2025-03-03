CREATE DATABASE IF NOT EXISTS accounting_db;
USE accounting_db;

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP
);
