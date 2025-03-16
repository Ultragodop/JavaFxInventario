package com.minimercado.javafxinventario.DAO;

import com.minimercado.javafxinventario.modules.Transaction;
import com.minimercado.javafxinventario.modules.AccountingEntry;
import com.minimercado.javafxinventario.utils.DatabaseConnection;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the accounting module.
 * Handles database operations related to accounting entries, journal entries, and financial reports.
 */
public class AccountingDAO {
    
    private static final Logger logger = Logger.getLogger(AccountingDAO.class.getName());
    
    /**
     * Records a transaction in the accounting system.
     * Creates accounting entries for the transaction.
     * 
     * @param transaction The transaction to record
     * @return true if recording was successful, false otherwise
     */
    public boolean recordTransaction(Transaction transaction) {
        Connection conn = null;
        try {
            // Check if the transaction already exists to avoid duplicates
            if (transactionExists(transaction.getId())) {
                System.out.println("Transaction already exists in database, skipping: " + transaction.getId());
                return true; // Consider it a success since it's already there
            }
            
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // First record in accounting_transactions
            String sql = "INSERT INTO accounting_transactions (transaction_id, transaction_type, transaction_date, amount, description) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, transaction.getId());
                stmt.setString(2, transaction.getType());
                stmt.setTimestamp(3, Timestamp.valueOf(transaction.getTimestamp()));
                stmt.setDouble(4, transaction.getAmount());
                stmt.setString(5, transaction.getDescription());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Insert failed, no rows affected.");
                }
                System.out.println("Transaction recorded in database: " + transaction.getId());
            }
            
            // Create a journal entry for this transaction
            if (transaction.getType().startsWith("venta")) {
                createSaleJournalEntry(conn, transaction);
                System.out.println("Sale journal entry created for transaction: " + transaction.getId());
            } else if (transaction.getType().contains("reversal")) {
                createReversalJournalEntry(conn, transaction);
                System.out.println("Reversal journal entry created for transaction: " + transaction.getId());
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error recording transaction in accounting system: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Creates a journal entry for a sale transaction.
     * 
     * @param conn Database connection
     * @param transaction The sale transaction
     * @throws SQLException if an error occurs
     */
    private void createSaleJournalEntry(Connection conn, Transaction transaction) throws SQLException {
        // Insert journal entry
        String journalSql = "INSERT INTO journal_entries (entry_date, reference_number, description, is_posted, created_by) " +
                          "VALUES (?, ?, ?, TRUE, ?)";
        
        int journalEntryId;
        try (PreparedStatement stmt = conn.prepareStatement(journalSql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime timestamp = transaction.getTimestamp();
            Date entryDate = Date.valueOf(timestamp.toLocalDate());
            
            stmt.setDate(1, entryDate);
            stmt.setString(2, transaction.getId());
            stmt.setString(3, "Venta registrada: " + transaction.getDescription());
            stmt.setString(4, "Sistema");
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    journalEntryId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating journal entry failed, no ID obtained.");
                }
            }
        }
        
        // Link transaction to journal entry
        String linkSql = "UPDATE accounting_transactions SET journal_entry_id = ? WHERE transaction_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(linkSql)) {
            stmt.setInt(1, journalEntryId);
            stmt.setString(2, transaction.getId());
            stmt.executeUpdate();
        }
        
        // Insert journal line items (debits and credits)
        double amount = transaction.getAmount();
        
        // Get account IDs by their account codes instead of using hardcoded IDs
        int cashAccountId = getAccountIdByCode(conn, "1000");
        int salesRevenueAccountId = getAccountIdByCode(conn, "4000");
        
        // Debit Cash (or Accounts Receivable based on payment type)
        insertJournalLineItem(conn, journalEntryId, cashAccountId, "Cash receipt", amount, 0);
        
        // Credit Sales Revenue
        insertJournalLineItem(conn, journalEntryId, salesRevenueAccountId, "Sales revenue", 0, amount);
    }
    
    /**
     * Creates a journal entry for a reversal transaction.
     * 
     * @param conn Database connection
     * @param transaction The reversal transaction
     * @throws SQLException if an error occurs
     */
    private void createReversalJournalEntry(Connection conn, Transaction transaction) throws SQLException {
        // Insert journal entry for reversal
        String journalSql = "INSERT INTO journal_entries (entry_date, reference_number, description, is_posted, created_by) " +
                          "VALUES (?, ?, ?, TRUE, ?)";
        
        int journalEntryId;
        try (PreparedStatement stmt = conn.prepareStatement(journalSql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime timestamp = transaction.getTimestamp();
            Date entryDate = Date.valueOf(timestamp.toLocalDate());
            
            stmt.setDate(1, entryDate);
            stmt.setString(2, transaction.getId());
            stmt.setString(3, "Reversión: " + transaction.getDescription());
            stmt.setString(4, "Sistema");
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    journalEntryId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating journal entry failed, no ID obtained.");
                }
            }
        }
        
        // Link transaction to journal entry
        String linkSql = "UPDATE accounting_transactions SET journal_entry_id = ? WHERE transaction_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(linkSql)) {
            stmt.setInt(1, journalEntryId);
            stmt.setString(2, transaction.getId());
            stmt.executeUpdate();
        }
        
        // Insert journal line items (reversed entries)
        double amount = Math.abs(transaction.getAmount());
        
        // Get account IDs by their account codes instead of using hardcoded IDs
        int cashAccountId = getAccountIdByCode(conn, "1000");
        int salesRevenueAccountId = getAccountIdByCode(conn, "4000");
        
        // Credit Cash (reversing the original debit)
        insertJournalLineItem(conn, journalEntryId, cashAccountId, "Reversal of cash receipt", 0, amount);
        
        // Debit Sales Revenue (reversing the original credit)
        insertJournalLineItem(conn, journalEntryId, salesRevenueAccountId, "Reversal of sales revenue", amount, 0);
    }
    
    /**
     * Inserts a line item into a journal entry.
     * 
     * @param conn Database connection
     * @param journalEntryId The journal entry ID
     * @param accountId The account ID
     * @param description Description of the line item
     * @param debit Debit amount
     * @param credit Credit amount
     * @throws SQLException if an error occurs
     */
    private void insertJournalLineItem(Connection conn, int journalEntryId, int accountId, 
                                      String description, double debit, double credit) throws SQLException {
        String sql = "INSERT INTO journal_line_items (journal_entry_id, account_id, description, debit, credit) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, journalEntryId);
            stmt.setInt(2, accountId);
            stmt.setString(3, description);
            stmt.setDouble(4, debit);
            stmt.setDouble(5, credit);
            
            stmt.executeUpdate();
        }
    }

    /**
     * Creates a transaction for an expense.
     * @param amount The amount of the expense
     * @param type The type of transaction
     * @param description A description of the transaction
     * @param paymentMethod The payment method used
     * @param expenseDate The date of the expense
     * @param accountCode The account code to use
     * @param receiptNumber The receipt number for the expense
     * @return true if the transaction was created successfully, false otherwise
     */
    public boolean createExpenseTransaction(double amount, String type, String description, String paymentMethod, LocalDateTime expenseDate, String accountCode, String receiptNumber) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert journal entry
            String journalSql = "INSERT INTO journal_entries (entry_date, reference_number, description, is_posted, created_by) " +
                    "VALUES (?, ?, ?, TRUE, ?)";

            int journalEntryId;
            try (PreparedStatement stmt = conn.prepareStatement(journalSql, Statement.RETURN_GENERATED_KEYS)) {
                Date entryDate = Date.valueOf(expenseDate.toLocalDate());

                stmt.setDate(1, entryDate);
                stmt.setString(2, receiptNumber);
                stmt.setString(3, description);
                stmt.setString(4, "Sistema");

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating journal entry failed, no ID obtained.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        journalEntryId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating journal entry failed, no ID obtained.");
                    }
                }
            }

            // Insert journal line items (debit and credit)
            // Get account IDs by their account codes instead of using hardcoded IDs
            int expenseAccountId = getAccountIdByCode(conn, accountCode);
            int cashAccountId = getAccountIdByCode(conn, "1000"); // Assuming 1000 is cash account

            // Debit Expense Account
            insertJournalLineItem(conn, journalEntryId, expenseAccountId, "Expense: " + description, amount, 0);

            // Credit Cash Account
            insertJournalLineItem(conn, journalEntryId, cashAccountId, "Payment for: " + description, 0, amount);

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error creating expense transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the account ID for a given account code.
     *
     * @param conn Database connection
     * @param accountCode The account code to look up
     * @return The account ID
     * @throws SQLException if an error occurs or the account code doesn't exist
     */
    private int getAccountIdByCode(Connection conn, String accountCode) throws SQLException {
        String sql = "SELECT id FROM accounts WHERE account_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                throw new SQLException("Account with code '" + accountCode + "' not found in accounts table");
            }
        }
    }

    /**
     * Gets all transactions recorded in the accounting system.
     * 
     * @return List of accounting transactions
     */
    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM accounting_transactions ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String transactionId = rs.getString("transaction_id");
                String transactionType = rs.getString("transaction_type");
                LocalDateTime transactionDate = rs.getTimestamp("transaction_date").toLocalDateTime();
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                
                Transaction transaction = new Transaction(transactionType, amount, description);
                transaction.setId(transactionId);
                transaction.setTimestamp(transactionDate);
                
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error getting accounting transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Gets transactions for a specific date range.
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of transactions in the date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM accounting_transactions WHERE DATE(transaction_date) BETWEEN ? AND ? " +
                    "ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String transactionId = rs.getString("transaction_id");
                    String transactionType = rs.getString("transaction_type");
                    LocalDateTime transactionDate = rs.getTimestamp("transaction_date").toLocalDateTime();
                    double amount = rs.getDouble("amount");
                    String description = rs.getString("description");
                    
                    Transaction transaction = new Transaction(transactionType, amount, description);
                    transaction.setId(transactionId);
                    transaction.setTimestamp(transactionDate);
                    
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions by date range: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Gets the general ledger entries for a specific account and date range.
     * 
     * @param accountCode The account code
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of ledger entries
     */
    public List<Map<String, Object>> getLedgerEntries(String accountCode, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> entries = new ArrayList<>();
        String sql = "SELECT je.entry_date, je.reference_number, je.description as entry_desc, " +
                    "jli.description as line_desc, jli.debit, jli.credit " +
                    "FROM journal_entries je " +
                    "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                    "JOIN accounts a ON jli.account_id = a.id " +
                    "WHERE a.account_code = ? AND je.entry_date BETWEEN ? AND ? " +
                    "ORDER BY je.entry_date, je.id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountCode);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("entryDate", rs.getDate("entry_date").toLocalDate());
                    entry.put("reference", rs.getString("reference_number"));
                    entry.put("entryDescription", rs.getString("entry_desc"));
                    entry.put("lineDescription", rs.getString("line_desc"));
                    entry.put("debit", rs.getDouble("debit"));
                    entry.put("credit", rs.getDouble("credit"));
                    
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ledger entries: " + e.getMessage());
            e.printStackTrace();
        }
        
        return entries;
    }
    
    /**
     * Gets the account balance for a specific account and date.
     * 
     * @param accountCode The account code
     * @param asOfDate The date up to which to calculate the balance
     * @return The account balance
     */
    public double getAccountBalance(String accountCode, LocalDate asOfDate) {
        String sql = "SELECT SUM(jli.debit) - SUM(jli.credit) as balance " +
                    "FROM journal_entries je " +
                    "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                    "JOIN accounts a ON jli.account_id = a.id " +
                    "WHERE a.account_code = ? AND je.entry_date <= ? AND a.account_type IN ('ASSET', 'EXPENSE') " +
                    "UNION " +
                    "SELECT SUM(jli.credit) - SUM(jli.debit) as balance " +
                    "FROM journal_entries je " +
                    "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                    "JOIN accounts a ON jli.account_id = a.id " +
                    "WHERE a.account_code = ? AND je.entry_date <= ? AND a.account_type IN ('LIABILITY', 'EQUITY', 'REVENUE')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountCode);
            stmt.setDate(2, Date.valueOf(asOfDate));
            stmt.setString(3, accountCode);
            stmt.setDate(4, Date.valueOf(asOfDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting account balance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Gets an income statement (revenue - expenses) for a specific period.
     * 
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return Map containing revenue, expenses, and net income
     */
    public Map<String, Double> getIncomeStatement(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> result = new HashMap<>();
        result.put("totalRevenue", 0.0);
        result.put("totalExpenses", 0.0);
        result.put("netIncome", 0.0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get total revenue
            double revenue = calculateTotalForType(conn, "REVENUE", startDate, endDate);
            result.put("totalRevenue", revenue);
            
            // Get total expenses - using a generalized query that includes all expense transactions
            double expenses = 0.0;
            
            // First, get expenses from accounting journal entries
            String expenseSql = "SELECT SUM(jli.debit) - SUM(jli.credit) as total " +
                              "FROM journal_entries je " +
                              "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                              "JOIN accounts a ON jli.account_id = a.id " +
                              "WHERE a.account_type = 'EXPENSE' AND je.entry_date BETWEEN ? AND ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(expenseSql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        expenses += rs.getDouble("total");
                    }
                }
            }
            
            // Next, get expenses from transaction records that might not be in journal yet
            String transactionSql = "SELECT SUM(CASE WHEN transaction_type IN ('egreso', 'gasto', 'compra') " +
                                    "THEN ABS(amount) ELSE 0 END) as expense_total " +
                                    "FROM accounting_transactions " +
                                    "WHERE DATE(transaction_date) BETWEEN ? AND ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(transactionSql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        expenses += rs.getDouble("expense_total");
                    }
                }
            }
            
            result.put("totalExpenses", expenses);
            
            // Calculate net income
            double netIncome = revenue - expenses;
            result.put("netIncome", netIncome);
            
        } catch (SQLException e) {
            System.err.println("Error generating income statement: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Helper method to calculate total for a specific account type
     */
    private double calculateTotalForType(Connection conn, String accountType, 
                                       LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql;
        
        if ("REVENUE".equals(accountType)) {
            sql = "SELECT SUM(jli.credit) - SUM(jli.debit) as total " +
                  "FROM journal_entries je " +
                  "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                  "JOIN accounts a ON jli.account_id = a.id " +
                  "WHERE a.account_type = ? AND je.entry_date BETWEEN ? AND ?";
        } else {
            sql = "SELECT SUM(jli.debit) - SUM(jli.credit) as total " +
                  "FROM journal_entries je " +
                  "JOIN journal_line_items jli ON je.id = jli.journal_entry_id " +
                  "JOIN accounts a ON jli.account_id = a.id " +
                  "WHERE a.account_type = ? AND je.entry_date BETWEEN ? AND ?";
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountType);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Creates a new account in the chart of accounts.
     * 
     * @param accountCode The account code
     * @param name The account name
     * @param description The account description
     * @param accountType The account type
     * @param parentAccountId The parent account ID (optional)
     * @return true if creation was successful, false otherwise
     */
    public boolean createAccount(String accountCode, String name, String description, 
                               String accountType, Integer parentAccountId) {
        String sql = "INSERT INTO accounts (account_code, name, description, account_type, parent_account_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accountCode);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setString(4, accountType);
            
            if (parentAccountId != null) {
                stmt.setInt(5, parentAccountId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all accounts in the chart of accounts.
     * 
     * @return List of accounts
     */
    public List<Map<String, Object>> getAllAccounts() {
        List<Map<String, Object>> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> account = new HashMap<>();
                account.put("id", rs.getInt("id"));
                account.put("accountCode", rs.getString("account_code"));
                account.put("name", rs.getString("name"));
                account.put("description", rs.getString("description"));
                account.put("accountType", rs.getString("account_type"));
                account.put("parentAccountId", rs.getInt("parent_account_id"));
                account.put("isActive", rs.getBoolean("is_active"));
                
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all accounts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return accounts;
    }
    
    /**
     * Verifies if a transaction is already recorded in the accounting system.
     * 
     * @param transactionId The transaction ID
     * @return true if transaction exists, false otherwise
     */
    public boolean transactionExists(String transactionId) {
        String sql = "SELECT COUNT(*) FROM accounting_transactions WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transactionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if transaction exists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get all transaction categories from the database
     * 
     * @return List of distinct transaction categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT type FROM accounting_transactions WHERE type IS NOT NULL ORDER BY type";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("type"));
            }
            
            // If no categories found, add some defaults
            if (categories.isEmpty()) {
                categories.add("Ventas");
                categories.add("Compras");
                categories.add("Gastos");
                categories.add("Salarios");
                categories.add("Otros ingresos");
                categories.add("Otros gastos");
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting transaction categories", e);
            // Return default categories if there's an error
            categories.add("Ventas");
            categories.add("Compras");
            categories.add("Gastos");
            categories.add("Otros");
        }
        
        return categories;
    }
}
