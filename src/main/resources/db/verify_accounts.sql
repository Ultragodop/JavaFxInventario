-- Script to verify account structure and fix common issues

-- Verify all accounts exist
SELECT 
    account_code, 
    name, 
    account_type, 
    id 
FROM 
    accounts 
ORDER BY 
    account_code;

-- Look for possible duplicate accounts with different IDs
SELECT 
    account_code, 
    COUNT(*) as count, 
    GROUP_CONCAT(id) as ids
FROM 
    accounts 
GROUP BY 
    account_code 
HAVING 
    COUNT(*) > 1;

-- Look for orphaned journal line items
SELECT 
    jli.id, 
    jli.account_id, 
    jli.description
FROM 
    journal_line_items jli
LEFT JOIN 
    accounts a ON jli.account_id = a.id
WHERE 
    a.id IS NULL;

-- This query will help identify what account IDs the system is trying to use
SELECT 
    DISTINCT account_id 
FROM 
    journal_line_items 
ORDER BY 
    account_id;
