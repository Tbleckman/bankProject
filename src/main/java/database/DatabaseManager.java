package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main database operations manager
 * Handles all SQL operations for the banking system
 */
public class DatabaseManager {
    
    // ============ CUSTOMER OPERATIONS ============
    
    /**
     * Create a new customer
     * Returns the generated customer_id
     */
    public int createCustomer(String name, String email, String phone) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?) RETURNING customer_id";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
            throw new SQLException("Failed to create customer");
        }
    }
    
    /**
     * Get customer by ID
     */
    public CustomerData getCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new CustomerData(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
            return null;
        }
    }
    
    // ============ ACCOUNT OPERATIONS ============
    
    /**
     * Create a new account
     */
    public void createAccount(int customerId, String accountNumber, String accountType, 
                            double balance, Double interestRate, Double creditLimit) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_number, account_type, balance, interest_rate, credit_limit) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setString(2, accountNumber);
            pstmt.setString(3, accountType);
            pstmt.setDouble(4, balance);
            
            if (interestRate != null) {
                pstmt.setDouble(5, interestRate);
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            if (creditLimit != null) {
                pstmt.setDouble(6, creditLimit);
            } else {
                pstmt.setNull(6, Types.DECIMAL);
            }
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get account by account number
     */
    public AccountData getAccount(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new AccountData(
                    rs.getInt("account_id"),
                    rs.getInt("customer_id"),
                    rs.getString("account_number"),
                    rs.getString("account_type"),
                    rs.getDouble("balance"),
                    rs.getDouble("interest_rate"),
                    rs.getDouble("credit_limit"),
                    rs.getBoolean("is_active")
                );
            }
            return null;
        }
    }
    
    /**
     * Get all accounts for a customer
     */
    public List<AccountData> getCustomerAccounts(int customerId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? AND is_active = TRUE ORDER BY created_at";
        List<AccountData> accounts = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(new AccountData(
                    rs.getInt("account_id"),
                    rs.getInt("customer_id"),
                    rs.getString("account_number"),
                    rs.getString("account_type"),
                    rs.getDouble("balance"),
                    rs.getDouble("interest_rate"),
                    rs.getDouble("credit_limit"),
                    rs.getBoolean("is_active")
                ));
            }
        }
        return accounts;
    }
    
    /**
     * Update account balance
     */
    public void updateBalance(String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Delete (deactivate) an account
     */
    public void deleteAccount(String accountNumber) throws SQLException {
        String sql = "UPDATE accounts SET is_active = FALSE WHERE account_number = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            pstmt.executeUpdate();
        }
    }
    
    // ============ TRANSACTION OPERATIONS ============
    
    /**
     * Log a transaction
     */
    public void logTransaction(String accountNumber, String type, double amount, 
                              double balanceAfter, String description) throws SQLException {
        // First get the account_id
        AccountData account = getAccount(accountNumber);
        if (account == null) {
            throw new SQLException("Account not found: " + accountNumber);
        }
        
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, balance_after, description) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, account.accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, balanceAfter);
            pstmt.setString(5, description);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Get transaction history for an account
     */
    public List<Transaction> getTransactionHistory(String accountNumber, int limit) throws SQLException {
        String sql = "SELECT t.* FROM transactions t " +
                    "JOIN accounts a ON t.account_id = a.account_id " +
                    "WHERE a.account_number = ? " +
                    "ORDER BY t.transaction_date DESC LIMIT ?";
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountNumber);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getInt("account_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getDouble("balance_after"),
                    rs.getString("description"),
                    rs.getString("related_account"),
                    rs.getTimestamp("transaction_date")
                ));
            }
        }
        return transactions;
    }
    
    /**
     * Transfer money between accounts (atomic transaction)
     */
    public void transferBetweenAccounts(String fromAccount, String toAccount, double amount) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Get both accounts
            AccountData from = getAccount(fromAccount);
            AccountData to = getAccount(toAccount);
            
            if (from == null || to == null) {
                throw new SQLException("One or both accounts not found");
            }
            
            if (from.balance < amount) {
                throw new SQLException("Insufficient funds in source account");
            }
            
            // Update balances
            double newFromBalance = from.balance - amount;
            double newToBalance = to.balance + amount;
            
            String updateSql = "UPDATE accounts SET balance = ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                // Deduct from source
                pstmt.setDouble(1, newFromBalance);
                pstmt.setString(2, fromAccount);
                pstmt.executeUpdate();
                
                // Add to destination
                pstmt.setDouble(1, newToBalance);
                pstmt.setString(2, toAccount);
                pstmt.executeUpdate();
            }
            
            // Log transactions
            String transSql = "INSERT INTO transactions (account_id, transaction_type, amount, balance_after, description, related_account) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(transSql)) {
                // Log outgoing transfer
                pstmt.setInt(1, from.accountId);
                pstmt.setString(2, "TRANSFER_OUT");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, newFromBalance);
                pstmt.setString(5, "Transfer to " + toAccount);
                pstmt.setString(6, toAccount);
                pstmt.executeUpdate();
                
                // Log incoming transfer
                pstmt.setInt(1, to.accountId);
                pstmt.setString(2, "TRANSFER_IN");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, newToBalance);
                pstmt.setString(5, "Transfer from " + fromAccount);
                pstmt.setString(6, fromAccount);
                pstmt.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // ============ DATA CLASSES ============
    
    public static class CustomerData {
        public final int customerId;
        public final String name;
        public final String email;
        public final String phone;
        
        public CustomerData(int customerId, String name, String email, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
        
        @Override
        public String toString() {
            return String.format("Customer[ID=%d, Name=%s, Email=%s, Phone=%s]", 
                customerId, name, email, phone);
        }
    }
    
    public static class AccountData {
        public final int accountId;
        public final int customerId;
        public final String accountNumber;
        public final String accountType;
        public final double balance;
        public final double interestRate;
        public final double creditLimit;
        public final boolean isActive;
        
        public AccountData(int accountId, int customerId, String accountNumber, String accountType,
                          double balance, double interestRate, double creditLimit, boolean isActive) {
            this.accountId = accountId;
            this.customerId = customerId;
            this.accountNumber = accountNumber;
            this.accountType = accountType;
            this.balance = balance;
            this.interestRate = interestRate;
            this.creditLimit = creditLimit;
            this.isActive = isActive;
        }
        
        @Override
        public String toString() {
            return String.format("Account[%s, Type=%s, Balance=$%.2f]", 
                accountNumber, accountType, balance);
        }
    }
    
    public static class Transaction {
        public final int transactionId;
        public final int accountId;
        public final String type;
        public final double amount;
        public final double balanceAfter;
        public final String description;
        public final String relatedAccount;
        public final Timestamp date;
        
        public Transaction(int transactionId, int accountId, String type, double amount,
                         double balanceAfter, String description, String relatedAccount, Timestamp date) {
            this.transactionId = transactionId;
            this.accountId = accountId;
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.description = description;
            this.relatedAccount = relatedAccount;
            this.date = date;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: $%.2f | Balance: $%.2f | %s", 
                date, type, amount, balanceAfter, description);
        }
    }
}
