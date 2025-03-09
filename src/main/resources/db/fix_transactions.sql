-- Script to help diagnose and fix transaction issues

-- First, find what account IDs are being used that don't exist
SELECT DISTINCT jli.account_id
FROM journal_line_items jli
LEFT JOIN accounts a ON jli.account_id = a.id
WHERE a.id IS NULL;

-- Get details of problematic transactions
SELECT 
    je.id as journal_entry_id,
    je.description,
    jli.id as line_item_id,
    jli.account_id,
    jli.description as line_description,
    jli.debit,
    jli.credit
FROM 
    journal_entries je
JOIN 
    journal_line_items jli ON je.id = jli.journal_entry_id
LEFT JOIN 
    accounts a ON jli.account_id = a.id
WHERE 
    a.id IS NULL;

-- If you find specific problematic account IDs, add them with this template
-- Replace X with the missing account ID
-- INSERT INTO accounts (id, account_code, name, account_type, description) 
-- VALUES (X, 'TEMP_X', 'Temporary Account X', 'ASSET', 'Auto-created to fix foreign key issues');
