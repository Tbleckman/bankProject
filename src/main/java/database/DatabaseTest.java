package database;

import java.sql.SQLException;
import java.util.List;

/**
 * Test file to verify database operations work correctly
 * Run this after setting up your database to make sure everything is connected
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("=== Banking System Database Test ===\n");
        
        // Test 1: Database Connection
        System.out.println("Test 1: Testing database connection...");
        if (DatabaseConfig.testConnection()) {
            System.out.println("Database connection successful!\n");
        } else {
            System.out.println("Database connection failed!");
            System.out.println("Check your DatabaseConfig.java settings (URL, username, password)\n");
            return;
        }
        
        DatabaseManager db = new DatabaseManager();
        
        try {
            // Test 2: Create a new customer
            System.out.println("Test 2: Creating a new customer...");
            int customerId = db.createCustomer("Test User", "test@email.com", "555-1234");
            System.out.println("Customer created with ID: " + customerId + "\n");
            
            // Test 3: Create a checking account
            System.out.println("Test 3: Creating a checking account...");
            String accountNumber = "TEST001";
            db.createAccount(customerId, accountNumber, "CHECKING", 1000.00, null, null);
            System.out.println("Checking account created: " + accountNumber + "\n");
            
            // Test 4: Retrieve the account
            System.out.println("Test 4: Retrieving account information...");
            DatabaseManager.AccountData account = db.getAccount(accountNumber);
            if (account != null) {
                System.out.println("Account found:");
                System.out.println("  Account Number: " + account.accountNumber);
                System.out.println("  Type: " + account.accountType);
                System.out.println("  Balance: $" + account.balance + "\n");
            }
            
            // Test 5: Log a deposit transaction
            System.out.println("Test 5: Making a deposit...");
            double depositAmount = 500.00;
            double newBalance = account.balance + depositAmount;
            db.updateBalance(accountNumber, newBalance);
            db.logTransaction(accountNumber, "DEPOSIT", depositAmount, newBalance, "Test deposit");
            System.out.println("Deposit of $" + depositAmount + " completed\n");
            
            // Test 6: Log a withdrawal transaction
            System.out.println("Test 6: Making a withdrawal...");
            double withdrawAmount = 200.00;
            newBalance = newBalance - withdrawAmount;
            db.updateBalance(accountNumber, newBalance);
            db.logTransaction(accountNumber, "WITHDRAW", withdrawAmount, newBalance, "Test withdrawal");
            System.out.println("Withdrawal of $" + withdrawAmount + " completed\n");
            
            // Test 7: Get transaction history
            System.out.println("Test 7: Retrieving transaction history...");
            List<DatabaseManager.Transaction> transactions = db.getTransactionHistory(accountNumber, 10);
            System.out.println("Found " + transactions.size() + " transactions:");
            for (DatabaseManager.Transaction t : transactions) {
                System.out.println("  " + t);
            }
            System.out.println();
            
            // Test 8: Create a second account for transfer test
            System.out.println("Test 8: Creating a second account for transfer test...");
            String accountNumber2 = "TEST002";
            db.createAccount(customerId, accountNumber2, "SAVINGS", 2000.00, 2.5, null);
            System.out.println("Savings account created: " + accountNumber2 + "\n");
            
            // Test 9: Transfer between accounts
            System.out.println("Test 9: Testing transfer between accounts...");
            System.out.println("  Before transfer:");
            DatabaseManager.AccountData acc1 = db.getAccount(accountNumber);
            DatabaseManager.AccountData acc2 = db.getAccount(accountNumber2);
            System.out.println("    " + accountNumber + " balance: $" + acc1.balance);
            System.out.println("    " + accountNumber2 + " balance: $" + acc2.balance);
            
            double transferAmount = 300.00;
            db.transferBetweenAccounts(accountNumber, accountNumber2, transferAmount);
            
            System.out.println("  After transfer of $" + transferAmount + ":");
            acc1 = db.getAccount(accountNumber);
            acc2 = db.getAccount(accountNumber2);
            System.out.println("    " + accountNumber + " balance: $" + acc1.balance);
            System.out.println("    " + accountNumber2 + " balance: $" + acc2.balance);
            System.out.println("Transfer completed successfully\n");
            
            // Test 10: Get all customer accounts
            System.out.println("Test 10: Getting all accounts for customer...");
            List<DatabaseManager.AccountData> customerAccounts = db.getCustomerAccounts(customerId);
            System.out.println("Found " + customerAccounts.size() + " accounts:");
            for (DatabaseManager.AccountData a : customerAccounts) {
                System.out.println("  " + a.accountNumber + " (" + a.accountType + "): $" + a.balance);
            }
            System.out.println();
            
            System.out.println("=== All tests passed! ===");
            System.out.println("\nYour database is set up correctly and ready to use.");
            System.out.println("You can now integrate these methods into your existing Account classes.");
            
        } catch (SQLException e) {
            System.out.println("X Test failed with error:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}