-- Script to completely reset the accounting schema by dropping and recreating all tables

-- Disable foreign key checks to avoid constraint issues during drop process
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS accounting_transactions;
DROP TABLE IF EXISTS journal_line_items;
DROP TABLE IF EXISTS journal_entries;
DROP TABLE IF EXISTS fiscal_periods;
DROP TABLE IF EXISTS accounts;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Now recreate all tables in proper order

-- Create accounts table (chart of accounts)
CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') NOT NULL,
    parent_account_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_account_id) REFERENCES accounts(id)
);

-- Create journal entries table
CREATE TABLE journal_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entry_date DATE NOT NULL,
    reference_number VARCHAR(50),
    description VARCHAR(255),
    is_posted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create journal line items table
CREATE TABLE journal_line_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id INT NOT NULL,
    account_id INT NOT NULL,
    description VARCHAR(255),
    debit DECIMAL(15, 2) DEFAULT 0.00,
    credit DECIMAL(15, 2) DEFAULT 0.00,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Create fiscal periods table
CREATE TABLE fiscal_periods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type ENUM('MONTH', 'QUARTER', 'YEAR') NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    closed_at TIMESTAMP,
    closed_by VARCHAR(50)
);

-- Create transactions table to link with inventory/sales transactions
CREATE TABLE accounting_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL, -- UUID from Transaction module
    journal_entry_id INT,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id)
);

-- Insert all basic accounts
-- Basic asset accounts
INSERT INTO accounts (account_code, name, account_type, description) VALUES
('1000', 'Cash', 'ASSET', 'Cash on hand'),
('1010', 'Cash Register', 'ASSET', 'Cash in registers'),
('1100', 'Accounts Receivable', 'ASSET', 'Money owed by customers'),
('1200', 'Inventory', 'ASSET', 'Merchandise inventory'),

-- Payment method specific accounts
('1300', 'Credit Card Receivables', 'ASSET', 'Money to be received from credit card companies'),
('1310', 'Debit Card Receivables', 'ASSET', 'Money to be received from debit card transactions'),
('1320', 'Bank Transfer Receivables', 'ASSET', 'Money to be received from bank transfers'),
('1330', 'Digital Wallet Receivables', 'ASSET', 'Money to be received from digital wallet providers'),

-- Liability accounts
('2000', 'Accounts Payable', 'LIABILITY', 'Money owed to suppliers'),
('2100', 'Sales Tax Payable', 'LIABILITY', 'Sales tax collected but not yet paid to government'),
('2200', 'Credit Card Processing Fees', 'LIABILITY', 'Fees owed to credit card processors'),

-- Equity accounts
('3000', 'Owner''s Equity', 'EQUITY', 'Owner''s investment'),
('3100', 'Retained Earnings', 'EQUITY', 'Accumulated earnings'),

-- Revenue accounts
('4000', 'Sales Revenue', 'REVENUE', 'Revenue from sales'),
('4100', 'Sales Discounts', 'REVENUE', 'Discounts given to customers'),
('4200', 'Sales Returns', 'REVENUE', 'Returns from customers'),

-- Expense accounts
('5000', 'Cost of Goods Sold', 'EXPENSE', 'Cost of products sold'),
('6000', 'Operating Expenses', 'EXPENSE', 'General operating expenses'),
('6100', 'Payment Processing Fees', 'EXPENSE', 'Fees for processing various payment methods');

-- Create indexes for better performance
CREATE INDEX idx_journal_entries_date ON journal_entries (entry_date);
CREATE INDEX idx_accounting_transactions_id ON accounting_transactions (transaction_id);

-- Print confirmation message
SELECT 'Accounting schema successfully reset!' AS Message;
