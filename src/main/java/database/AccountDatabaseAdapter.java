package database;

import java.sql.SQLException;
import java.util.List;

/**
 * Adapter class to integrate database operations with existing Account classes
 * Allows current code to work with minimal changes from my previous implementation
 */
public class AccountDatabaseAdapter {
    
    private DatabaseManager db;
    
    public AccountDatabaseAdapter() {
        this.db = new DatabaseManager();
    }
    
    /**
     * Save an account to the database
     * This is called after creating a new account or after any balance changes
     */
    public void saveAccount(Object account) {
        try {
            // Get account details using reflection or type checking
            String accountNumber = getAccountNumber(account);
            String accountType = getAccountType(account);
            double balance = getBalance(account);
            
            // Check if account exists
            DatabaseManager.AccountData existing = db.getAccount(accountNumber);
            
            if (existing == null) {
                // Create new account - you'll need to provide a customer ID
                // For now, we'll use customer ID 1 (you can enhance this later)
                int customerId = 1; // Default customer
                
                Double interestRate = null;
                Double creditLimit = null;
                
                if (accountType.equals("SAVINGS")) {
                    interestRate = getInterestRate(account);
                } else if (accountType.equals("CREDIT")) {
                    creditLimit = getCreditLimit(account);
                }
                
                db.createAccount(customerId, accountNumber, accountType, balance, interestRate, creditLimit);
                System.out.println("Account saved to database: " + accountNumber);
            } else {
                // Update existing account balance
                db.updateBalance(accountNumber, balance);
                System.out.println("Account updated in database: " + accountNumber);
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving account to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load an account from the database
     * Returns the database account data that you can use to recreate your Account object
     */
    public DatabaseManager.AccountData loadAccount(String accountNumber) {
        try {
            return db.getAccount(accountNumber);
        } catch (SQLException e) {
            System.err.println("Error loading account from database: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Log a transaction after a deposit, withdrawal, or transfer
     */
    public void logTransaction(String accountNumber, String type, double amount, double newBalance, String description) {
        try {
            db.logTransaction(accountNumber, type, amount, newBalance, description);
            System.out.println("Transaction logged: " + type + " $" + amount);
        } catch (SQLException e) {
            System.err.println("Error logging transaction: " + e.getMessage());
        }
    }
    
    /**
     * Get transaction history for display
     */
    public void printTransactionHistory(String accountNumber, int limit) {
        try {
            List<DatabaseManager.Transaction> transactions = db.getTransactionHistory(accountNumber, limit);
            
            System.out.println("\n=== Transaction History for " + accountNumber + " ===");
            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
            } else {
                for (DatabaseManager.Transaction t : transactions) {
                    System.out.println(t);
                }
            }
            System.out.println();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction history: " + e.getMessage());
        }
    }
    
    /**
     * Delete an account from the database
     */
    public void deleteAccount(String accountNumber) {
        try {
            db.deleteAccount(accountNumber);
            System.out.println("Account deleted from database: " + accountNumber);
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
        }
    }
    
    /**
     * Transfer between accounts with full transaction support
     */
    public boolean transfer(String fromAccount, String toAccount, double amount) {
        try {
            db.transferBetweenAccounts(fromAccount, toAccount, amount);
            return true;
        } catch (SQLException e) {
            System.err.println("Transfer failed: " + e.getMessage());
            return false;
        }
    }
    
    // ============ HELPER METHODS TO EXTRACT DATA FROM YOUR ACCOUNT OBJECTS ============
    // These work side-by-side with the existing Account class structure from my previous implementation
    
    private String getAccountNumber(Object account) {
        try {
            return (String) account.getClass().getMethod("getAccountNumber").invoke(account);
        } catch (Exception e) {
            System.err.println("Error getting account number: " + e.getMessage());
            return null;
        }
    }
    
    private String getAccountType(Object account) {
        String className = account.getClass().getSimpleName();
        // Map your class names to database types
        if (className.contains("Checking")) return "CHECKING";
        if (className.contains("Savings")) return "SAVINGS";
        if (className.contains("Credit")) return "CREDIT";
        return "CHECKING"; // default
    }
    
    private double getBalance(Object account) {
        try {
            return (double) account.getClass().getMethod("getBankDeposit").invoke(account);
        } catch (Exception e) {
            System.err.println("Error getting balance: " + e.getMessage());
            return 0.0;
        }
    }
    

    //methods not used as they didn't end up being implemented
    //may be added in future implementations
    private Double getInterestRate(Object account) {
        try {
            // Assuming your Savings account has an interest rate method
            return (Double) account.getClass().getMethod("getInterestRate").invoke(account);
        } catch (Exception e) {
            return 2.5; // default interest rate
        }
    }
    
    private Double getCreditLimit(Object account) {
        try {
            // Assuming your Credit account has a credit limit method
            return (Double) account.getClass().getMethod("getCreditLimit").invoke(account);
        } catch (Exception e) {
            return 5000.0; // default credit limit
        }
    }

    public int getRecentTransactionCount(String accountNumber) {
        try {
            return db.getRecentTransactionCount(accountNumber);
        } catch (SQLException e) {
            System.err.println("Error retrieving recent transaction count: " + e.getMessage());
            return 0;
        }
    }
}
