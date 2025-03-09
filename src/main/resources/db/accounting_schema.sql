-- Script for creating the accounting module database tables

-- Create accounts table (chart of accounts)
CREATE TABLE IF NOT EXISTS accounts (
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
CREATE TABLE IF NOT EXISTS journal_entries (
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
CREATE TABLE IF NOT EXISTS journal_line_items (
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
CREATE TABLE IF NOT EXISTS fiscal_periods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type ENUM('MONTH', 'QUARTER', 'YEAR') NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    closed_at TIMESTAMP,
    closed_by VARCHAR(50)
);

-- Create transactions table to link with inventory/sales transactions
CREATE TABLE IF NOT EXISTS accounting_transactions (
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

-- Insert some basic accounts
INSERT INTO accounts (account_code, name, account_type, description) VALUES
('1000', 'Cash', 'ASSET', 'Cash on hand'),
('1100', 'Accounts Receivable', 'ASSET', 'Money owed by customers'),
('1200', 'Inventory', 'ASSET', 'Merchandise inventory'),
('2000', 'Accounts Payable', 'LIABILITY', 'Money owed to suppliers'),
('3000', 'Owner\'s Equity', 'EQUITY', 'Owner\'s investment'),
('4000', 'Sales Revenue', 'REVENUE', 'Revenue from sales'),
('5000', 'Cost of Goods Sold', 'EXPENSE', 'Cost of products sold'),
('6000', 'Operating Expenses', 'EXPENSE', 'General operating expenses');

-- Create index for better performance
CREATE INDEX idx_journal_entries_date ON journal_entries (entry_date);
CREATE INDEX idx_accounting_transactions_id ON accounting_transactions (transaction_id);
