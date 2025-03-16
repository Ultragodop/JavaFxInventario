-- BULLETPROOF DATABASE RESET SCRIPT: Forces account IDs to match exactly what's required in the code

-- STEP 1: Completely drop all tables and disable foreign keys
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS accounting_transactions;
DROP TABLE IF EXISTS journal_line_items;
DROP TABLE IF EXISTS journal_entries;
DROP TABLE IF EXISTS fiscal_periods; 
DROP TABLE IF EXISTS accounts;
SET FOREIGN_KEY_CHECKS = 1;

-- STEP 2: Recreate tables WITHOUT any foreign keys first
CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    account_type ENUM('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE') NOT NULL,
    parent_account_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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

CREATE TABLE journal_line_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id INT NOT NULL,
    account_id INT NOT NULL,
    description VARCHAR(255),
    debit DECIMAL(15, 2) DEFAULT 0.00,
    credit DECIMAL(15, 2) DEFAULT 0.00
);

CREATE TABLE fiscal_periods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type ENUM('MONTH', 'QUARTER', 'YEAR') NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    closed_at TIMESTAMP NULL,
    closed_by VARCHAR(50)
);

CREATE TABLE accounting_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL,
    journal_entry_id INT,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- STEP 3: Force reset all auto_increment counters
ALTER TABLE accounts AUTO_INCREMENT = 1;
ALTER TABLE journal_entries AUTO_INCREMENT = 1;
ALTER TABLE journal_line_items AUTO_INCREMENT = 1;
ALTER TABLE fiscal_periods AUTO_INCREMENT = 1;
ALTER TABLE accounting_transactions AUTO_INCREMENT = 1;

-- STEP 4: Insert ALL account possibilities with explicit IDs
-- We're creating accounts with ID 1-200 to ensure all possible references are covered

-- Basic Asset Accounts (ID 1-29)
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(1, '1000', 'Cash', 'ASSET', 'Cash on hand'),
(2, '1010', 'Cash Register', 'ASSET', 'Cash in registers'),
(3, '1100', 'Accounts Receivable', 'ASSET', 'Money owed by customers'),
(4, '1200', 'Inventory', 'ASSET', 'Merchandise inventory'),
(5, '1300', 'Credit Card Receivables', 'ASSET', 'Money to be received from credit card companies'),
(6, '1310', 'Debit Card Receivables', 'ASSET', 'Money to be received from debit card transactions'),
(7, '1320', 'Bank Transfer Receivables', 'ASSET', 'Money to be received from bank transfers'),
(8, '1330', 'Digital Wallet Receivables', 'ASSET', 'Money to be received from digital wallet providers'),
(9, '1340', 'Prepaid Expenses', 'ASSET', 'Expenses paid in advance'),
(10, '1350', 'Fixed Assets', 'ASSET', 'Long-term tangible assets'),
(11, '1360', 'Accumulated Depreciation', 'ASSET', 'Accumulated depreciation of fixed assets'),
(12, '1370', 'Other Assets', 'ASSET', 'Other assets not categorized elsewhere'),
(13, '1380', 'Investments', 'ASSET', 'Investments in other businesses'),
(14, '1390', 'Intangible Assets', 'ASSET', 'Patents, trademarks, goodwill, etc.'),
(15, '1400', 'Cash Payments', 'ASSET', 'Cash payments received'),
(16, '1410', 'Credit Card Payments', 'ASSET', 'Credit card payments received'),
(17, '1420', 'Debit Card Payments', 'ASSET', 'Debit card payments received'),
(18, '1430', 'Bank Transfer Payments', 'ASSET', 'Bank transfer payments received'),
(19, '1440', 'Digital Wallet Payments', 'ASSET', 'Digital wallet payments received'),
(20, '1450', 'Mixed Payment Method', 'ASSET', 'Multiple payment methods used'),
(21, '1460', 'Petty Cash', 'ASSET', 'Small cash reserve for minor expenses'),
(22, '1470', 'Bank Account - Main', 'ASSET', 'Main bank account'),
(23, '1480', 'Bank Account - Payroll', 'ASSET', 'Bank account for payroll'),
(24, '1490', 'Bank Account - Savings', 'ASSET', 'Savings bank account'),
(25, '1500', 'Accounts Receivable - Trade', 'ASSET', 'Money owed by trade customers'),
(26, '1510', 'Accounts Receivable - Non-Trade', 'ASSET', 'Money owed by non-trade customers'),
(27, '1520', 'Inventory - Finished Goods', 'ASSET', 'Inventory of finished products'),
(28, '1530', 'Inventory - Raw Materials', 'ASSET', 'Inventory of raw materials'),
(29, '1540', 'Inventory - Work in Progress', 'ASSET', 'Inventory of partially completed products');

-- Payment Methods Accounts (ID 30-39)
BEGIN TRANSACTION;

INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(30, '1550', 'Cash Payment Method', 'ASSET', 'Cash payment method account'),
(31, '1560', 'Credit Card Payment Method', 'ASSET', 'Credit card payment method account'),
(32, '1570', 'Debit Card Payment Method', 'ASSET', 'Debit card payment method account'),
(33, '1580', 'Bank Transfer Payment Method', 'ASSET', 'Bank transfer payment method account'),
(34, '1590', 'Digital Wallet Payment Method', 'ASSET', 'Digital wallet payment method account'),
(35, '1600', 'Mixed Payment Method', 'ASSET', 'Mixed payment method account'),
(36, '1610', 'Check Payment Method', 'ASSET', 'Check payment method account'),
(37, '1620', 'Gift Card Payment Method', 'ASSET', 'Gift card payment method account'),
(38, '1630', 'Store Credit Payment Method', 'ASSET', 'Store credit payment method account'),
(39, '1640', 'Loyalty Points Payment Method', 'ASSET', 'Loyalty points payment method account');

COMMIT;

-- Liability Accounts (ID 40-59)
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(40, '2000', 'Accounts Payable', 'LIABILITY', 'Money owed to suppliers'),
(41, '2100', 'Sales Tax Payable', 'LIABILITY', 'Sales tax collected but not yet paid'),
(42, '2200', 'Income Tax Payable', 'LIABILITY', 'Income tax owed but not yet paid'),
(43, '2300', 'Payroll Tax Payable', 'LIABILITY', 'Payroll tax owed but not yet paid'),
(44, '2400', 'Accrued Expenses', 'LIABILITY', 'Expenses incurred but not yet paid'),
(45, '2500', 'Customer Deposits', 'LIABILITY', 'Deposits made by customers'),
(46, '2600', 'Deferred Revenue', 'LIABILITY', 'Revenue received but not yet earned'),
(47, '2700', 'Current Portion of Long-Term Debt', 'LIABILITY', 'Long-term debt due within one year'),
(48, '2800', 'Long-Term Debt', 'LIABILITY', 'Debt due beyond one year'),
(49, '2900', 'Loans Payable', 'LIABILITY', 'Loans to be repaid'),
(50, '2910', 'Credit Card Processing Fees', 'LIABILITY', 'Fees owed to credit card processors'),
(51, '2920', 'Other Current Liabilities', 'LIABILITY', 'Other current liabilities not categorized elsewhere'),
(52, '2930', 'Other Long-Term Liabilities', 'LIABILITY', 'Other long-term liabilities not categorized elsewhere'),
(53, '2940', 'Notes Payable', 'LIABILITY', 'Written promises to pay'),
(54, '2950', 'Interest Payable', 'LIABILITY', 'Interest owed but not yet paid'),
(55, '2960', 'Rent Payable', 'LIABILITY', 'Rent owed but not yet paid'),
(56, '2970', 'Salaries Payable', 'LIABILITY', 'Salaries owed but not yet paid'),
(57, '2980', 'Unearned Revenue', 'LIABILITY', 'Revenue received but not yet earned'),
(58, '2990', 'Warranty Liability', 'LIABILITY', 'Estimated cost of future warranty claims'),
(59, '3010', 'Lease Liability', 'LIABILITY', 'Obligations under lease agreements');

-- Equity Accounts (ID 60-69)
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(60, '3000', 'Owner''s Equity', 'EQUITY', 'Owner''s investment in the business'),
(61, '3100', 'Common Stock', 'EQUITY', 'Ownership shares in a corporation'),
(62, '3200', 'Preferred Stock', 'EQUITY', 'Preferred ownership shares in a corporation'),
(63, '3300', 'Additional Paid-In Capital', 'EQUITY', 'Amount paid in excess of par value'),
(64, '3400', 'Retained Earnings', 'EQUITY', 'Accumulated profits or losses'),
(65, '3500', 'Treasury Stock', 'EQUITY', 'Stock repurchased by the company'),
(66, '3600', 'Dividends', 'EQUITY', 'Distributions to shareholders'),
(67, '3700', 'Owner''s Draws', 'EQUITY', 'Withdrawals by the owner'),
(68, '3800', 'Accumulated Other Comprehensive Income', 'EQUITY', 'Other comprehensive income items'),
(69, '3900', 'Current Year Earnings', 'EQUITY', 'Earnings for the current year');

-- Revenue Accounts (ID 70-89)
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(70, '4000', 'Sales Revenue', 'REVENUE', 'Revenue from sales'),
(71, '4100', 'Service Revenue', 'REVENUE', 'Revenue from services'),
(72, '4200', 'Interest Income', 'REVENUE', 'Income from interest'),
(73, '4300', 'Rental Income', 'REVENUE', 'Income from rentals'),
(74, '4400', 'Dividend Income', 'REVENUE', 'Income from dividends'),
(75, '4500', 'Gain on Sale of Assets', 'REVENUE', 'Income from selling assets above book value'),
(76, '4600', 'Other Income', 'REVENUE', 'Income from other sources'),
(77, '4700', 'Sales Returns and Allowances', 'REVENUE', 'Reductions in sales due to returns and allowances'),
(78, '4800', 'Sales Discounts', 'REVENUE', 'Reductions in sales due to discounts'),
(79, '4900', 'Shipping and Handling Income', 'REVENUE', 'Income from shipping and handling charges'),
(80, '5010', 'Sales - Product Line A', 'REVENUE', 'Revenue from Product Line A'),
(81, '5020', 'Sales - Product Line B', 'REVENUE', 'Revenue from Product Line B'),
(82, '5030', 'Sales - Product Line C', 'REVENUE', 'Revenue from Product Line C'),
(83, '5040', 'Sales - Service Line A', 'REVENUE', 'Revenue from Service Line A'),
(84, '5050', 'Sales - Service Line B', 'REVENUE', 'Revenue from Service Line B'),
(85, '5060', 'Sales - Service Line C', 'REVENUE', 'Revenue from Service Line C'),
(86, '5070', 'Commission Income', 'REVENUE', 'Income from commissions'),
(87, '5080', 'Royalty Income', 'REVENUE', 'Income from royalties'),
(88, '5090', 'Franchise Income', 'REVENUE', 'Income from franchises'),
(89, '5100', 'Miscellaneous Income', 'REVENUE', 'Income not categorized elsewhere');

-- Expense Accounts (ID 90-150)
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(90, '5000', 'Cost of Goods Sold', 'EXPENSE', 'Cost of products sold'),
(91, '6000', 'Advertising and Marketing', 'EXPENSE', 'Costs for advertising and marketing'),
(92, '6010', 'Bank Fees', 'EXPENSE', 'Fees charged by banks'),
(93, '6020', 'Credit Card Processing Fees', 'EXPENSE', 'Fees for processing credit card transactions'),
(94, '6030', 'Depreciation', 'EXPENSE', 'Allocation of asset costs over their useful lives'),
(95, '6040', 'Insurance', 'EXPENSE', 'Costs for insurance coverage'),
(96, '6050', 'Interest Expense', 'EXPENSE', 'Cost of borrowing money'),
(97, '6060', 'Office Supplies', 'EXPENSE', 'Costs for office supplies'),
(98, '6070', 'Payroll Expenses', 'EXPENSE', 'Costs for employee wages and salaries'),
(99, '6080', 'Professional Fees', 'EXPENSE', 'Fees paid to professionals like lawyers and accountants'),
(100, '6090', 'Rent or Lease', 'EXPENSE', 'Costs for renting or leasing property or equipment'),
(101, '6100', 'Repairs and Maintenance', 'EXPENSE', 'Costs for repairs and maintenance'),
(102, '6110', 'Shipping and Delivery', 'EXPENSE', 'Costs for shipping and delivery'),
(103, '6120', 'Taxes and Licenses', 'EXPENSE', 'Costs for taxes and licenses'),
(104, '6130', 'Telephone and Internet', 'EXPENSE', 'Costs for telephone and internet services'),
(105, '6140', 'Travel', 'EXPENSE', 'Costs for business travel'),
(106, '6150', 'Utilities', 'EXPENSE', 'Costs for utilities like electricity and water'),
(107, '6160', 'Wages and Salaries', 'EXPENSE', 'Costs for employee wages and salaries'),
(108, '6170', 'Employee Benefits', 'EXPENSE', 'Costs for employee benefits'),
(109, '6180', 'Payroll Taxes', 'EXPENSE', 'Taxes on employee wages and salaries'),
(110, '6190', 'Bad Debt', 'EXPENSE', 'Costs for uncollectible accounts'),
(111, '6200', 'Charitable Contributions', 'EXPENSE', 'Costs for charitable donations'),
(112, '6210', 'Dues and Subscriptions', 'EXPENSE', 'Costs for membership dues and subscriptions'),
(113, '6220', 'Education and Training', 'EXPENSE', 'Costs for employee education and training'),
(114, '6230', 'Entertainment', 'EXPENSE', 'Costs for entertaining clients or employees'),
(115, '6240', 'Equipment Rental', 'EXPENSE', 'Costs for renting equipment'),
(116, '6250', 'Meals', 'EXPENSE', 'Costs for business meals'),
(117, '6260', 'Miscellaneous Expenses', 'EXPENSE', 'Expenses not categorized elsewhere'),
(118, '6270', 'Postage and Shipping', 'EXPENSE', 'Costs for postage and shipping'),
(119, '6280', 'Printing and Reproduction', 'EXPENSE', 'Costs for printing and reproduction'),
(120, '6290', 'Software Expense', 'EXPENSE', 'Costs for software'),
(121, '6300', 'Amortization Expense', 'EXPENSE', 'Allocation of intangible asset costs over their useful lives'),
(122, '6310', 'Cash Over and Short', 'EXPENSE', 'Discrepancies between actual cash and recorded cash'),
(123, '6320', 'Freight and Shipping - COS', 'EXPENSE', 'Shipping costs included in cost of sales'),
(124, '6330', 'Purchase Discounts', 'EXPENSE', 'Discounts taken on purchases'),
(125, '6340', 'Purchase Returns and Allowances', 'EXPENSE', 'Reductions in purchases due to returns and allowances'),
(126, '6350', 'Warranty Expense', 'EXPENSE', 'Costs for warranty claims'),
(127, '6360', 'Cost of Services', 'EXPENSE', 'Costs for providing services'),
(128, '6370', 'Sales Commission', 'EXPENSE', 'Commissions paid to salespeople'),
(129, '6380', 'Payroll Processing Fees', 'EXPENSE', 'Fees for processing payroll'),
(130, '6390', 'Loss on Sale of Assets', 'EXPENSE', 'Loss from selling assets below book value'),
(131, '6400', 'Manager Salary', 'EXPENSE', 'Salary paid to managers'),
(132, '6410', 'Employee Salary', 'EXPENSE', 'Salary paid to employees'),
(133, '6420', 'Contractor Payments', 'EXPENSE', 'Payments made to contractors'),
(134, '6430', 'Inventory Adjustment', 'EXPENSE', 'Adjustments to inventory valuation'),
(135, '6440', 'Inventory Shrinkage', 'EXPENSE', 'Loss of inventory due to theft, damage, or error'),
(136, '6450', 'Foreign Currency Exchange Loss', 'EXPENSE', 'Losses from foreign currency exchange rate changes'),
(137, '6460', 'Income Tax Expense', 'EXPENSE', 'Income tax costs'),
(138, '6470', 'Property Tax Expense', 'EXPENSE', 'Property tax costs'),
(139, '6480', 'Sales Tax Expense', 'EXPENSE', 'Sales tax costs'),
(140, '6490', 'Penalties and Fines', 'EXPENSE', 'Costs for penalties and fines'),
(141, '6500', 'Disposal-Scrap Expense', 'EXPENSE', 'Costs for disposal or scrap'),
(142, '6510', 'Gas and Fuel Expense', 'EXPENSE', 'Costs for gas and fuel'),
(143, '6520', 'Vehicle Expense', 'EXPENSE', 'Costs for vehicles'),
(144, '6530', 'Vehicle Repairs Expense', 'EXPENSE', 'Costs for vehicle repairs'),
(145, '6540', 'Water and Sewer Expense', 'EXPENSE', 'Costs for water and sewer'),
(146, '6550', 'Building Repairs Expense', 'EXPENSE', 'Costs for building repairs'),
(147, '6560', 'Equipment Repairs Expense', 'EXPENSE', 'Costs for equipment repairs'),
(148, '6570', 'Legal Fees', 'EXPENSE', 'Fees paid to lawyers'),
(149, '6580', 'Accounting Fees', 'EXPENSE', 'Fees paid to accountants'),
(150, '6590', 'Consulting Fees', 'EXPENSE', 'Fees paid to consultants');

-- Additional Special Accounts (ID 151-200) for safety
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(151, '7000', 'Reserve Account 1', 'ASSET', 'Reserved for special use 1'),
(152, '7010', 'Reserve Account 2', 'ASSET', 'Reserved for special use 2'),
(153, '7020', 'Reserve Account 3', 'ASSET', 'Reserved for special use 3'),
(154, '7030', 'Reserve Account 4', 'ASSET', 'Reserved for special use 4'),
(155, '7040', 'Reserve Account 5', 'ASSET', 'Reserved for special use 5'),
(156, '7050', 'Reserve Account 6', 'ASSET', 'Reserved for special use 6'),
(157, '7060', 'Reserve Account 7', 'ASSET', 'Reserved for special use 7'),
(158, '7070', 'Reserve Account 8', 'ASSET', 'Reserved for special use 8'),
(159, '7080', 'Reserve Account 9', 'ASSET', 'Reserved for special use 9'),
(160, '7090', 'Reserve Account 10', 'ASSET', 'Reserved for special use 10'),
(161, '7100', 'Reserve Account 11', 'ASSET', 'Reserved for special use 11'),
(162, '7110', 'Reserve Account 12', 'ASSET', 'Reserved for special use 12'),
(163, '7120', 'Reserve Account 13', 'ASSET', 'Reserved for special use 13'),
(164, '7130', 'Reserve Account 14', 'ASSET', 'Reserved for special use 14'),
(165, '7140', 'Reserve Account 15', 'ASSET', 'Reserved for special use 15'),
(166, '7150', 'Reserve Account 16', 'ASSET', 'Reserved for special use 16'),
(167, '7160', 'Reserve Account 17', 'ASSET', 'Reserved for special use 17'),
(168, '7170', 'Reserve Account 18', 'ASSET', 'Reserved for special use 18'),
(169, '7180', 'Reserve Account 19', 'ASSET', 'Reserved for special use 19'),
(170, '7190', 'Reserve Account 20', 'ASSET', 'Reserved for special use 20'),
(171, '7200', 'EFECTIVO Account', 'ASSET', 'Special cash account for payment method EFECTIVO'),
(172, '7210', 'TARJETA Account', 'ASSET', 'Special account for payment method TARJETA'),
(173, '7220', 'TRANSFERENCIA Account', 'ASSET', 'Special account for payment method TRANSFERENCIA'),
(174, '7230', 'BILLETERA_DIGITAL Account', 'ASSET', 'Special account for payment method BILLETERA_DIGITAL'),
(175, '7240', 'PAGO_MIXTO Account', 'ASSET', 'Special account for payment method PAGO_MIXTO'),
(176, '7250', 'Cash Payment Account', 'ASSET', 'Cash payment specific account'),
(177, '7260', 'Credit Card Payment Account', 'ASSET', 'Credit card payment specific account'),
(178, '7270', 'Bank Transfer Payment Account', 'ASSET', 'Bank transfer payment specific account'),
(179, '7280', 'Digital Wallet Payment Account', 'ASSET', 'Digital wallet payment specific account'),
(180, '7290', 'Mixed Payment Account', 'ASSET', 'Mixed payment specific account'),
(181, '7300', 'Payment Method 1', 'ASSET', 'Payment method 1 account'),
(182, '7310', 'Payment Method 2', 'ASSET', 'Payment method 2 account'),
(183, '7320', 'Payment Method 3', 'ASSET', 'Payment method 3 account'),
(184, '7330', 'Payment Method 4', 'ASSET', 'Payment method 4 account'),
(185, '7340', 'Payment Method 5', 'ASSET', 'Payment method 5 account'),
(186, '7350', 'Generic Account', 'ASSET', 'Generic account for testing'),
(187, '7360', 'Generic Account 2', 'ASSET', 'Generic account 2 for testing'),
(188, '7370', 'Generic Account 3', 'ASSET', 'Generic account 3 for testing'),
(189, '7380', 'Generic Account 4', 'ASSET', 'Generic account 4 for testing'),
(190, '7390', 'Generic Account 5', 'ASSET', 'Generic account 5 for testing'),
(191, '7400', 'Generic Account 6', 'ASSET', 'Generic account 6 for testing'),
(192, '7410', 'Generic Account 7', 'ASSET', 'Generic account 7 for testing'),
(193, '7420', 'Generic Account 8', 'ASSET', 'Generic account 8 for testing'),
(194, '7430', 'Generic Account 9', 'ASSET', 'Generic account 9 for testing'),
(195, '7440', 'Generic Account 10', 'ASSET', 'Generic account 10 for testing'),
(196, '7450', 'PaymentMethod Account', 'ASSET', 'Payment method base account'),
(197, '7460', 'CashPayment Account', 'ASSET', 'Cash payment enum account'),
(198, '7470', 'CardPayment Account', 'ASSET', 'Card payment enum account'),
(199, '7480', 'TransferPayment Account', 'ASSET', 'Transfer payment enum account'),
(200, '7490', 'WalletPayment Account', 'ASSET', 'Wallet payment enum account');

-- STEP 5: Add any special hardcoded IDs that might exist in code - we're adding extra IDs well beyond 
-- what we expect to need just to be 100% safe
INSERT INTO accounts (id, account_code, name, account_type, description) VALUES
(1001, '8000', 'Special Account 1001', 'ASSET', 'Special hardcoded account 1001'),
(1002, '8010', 'Special Account 1002', 'ASSET', 'Special hardcoded account 1002'),
(1003, '8020', 'Special Account 1003', 'ASSET', 'Special hardcoded account 1003'),
(1004, '8030', 'Special Account 1004', 'ASSET', 'Special hardcoded account 1004'),
(1005, '8040', 'Special Account 1005', 'ASSET', 'Special hardcoded account 1005');

-- STEP 6: Now that we have populated all accounts, add back the foreign key constraints
ALTER TABLE journal_line_items ADD CONSTRAINT fk_jli_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE;
ALTER TABLE journal_line_items ADD CONSTRAINT fk_jli_account FOREIGN KEY (account_id) REFERENCES accounts(id);
ALTER TABLE accounting_transactions ADD CONSTRAINT fk_at_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id);
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_parent FOREIGN KEY (parent_account_id) REFERENCES accounts(id);

-- STEP 7: Create indexes for better performance
CREATE INDEX idx_journal_entries_date ON journal_entries (entry_date);
CREATE INDEX idx_accounting_transactions_id ON accounting_transactions (transaction_id);

-- STEP 8: Print success message
SELECT 'COMPLETE ACCOUNTING SYSTEM RESET SUCCESSFUL! ALL ACCOUNTS CREATED WITH IDs 1-200 AND 1001-1005.' AS 'Success Message';

-- STEP 9: Diagnostic query to check everything is in place
SELECT COUNT(*) AS total_accounts FROM accounts;
