package banking;

import database.DatabaseManager;
import domain.Bank;
import domain.BNYMellon;
import domain.CapitalOne;
import domain.Chase;
import org.springframework.stereotype.Service;
import service.RiskAnalysis;
import service.RiskServiceClient;

import java.sql.SQLException;
import java.util.List;

/**
 * Business layer for account operations: create, deposit, withdraw,
 * transfer, view history, close. Ports the validation rules that used
 * to live inline inside Interface.java's CLI prompt loops, and wires
 * withdrawals through the risk_service microservice.
 */
@Service
public class AccountService {

    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 3;

    private final DatabaseManager db = new DatabaseManager();
    private final RiskServiceClient riskClient = new RiskServiceClient();
    private final CustomerService customerService;

    public AccountService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Result of a withdrawal: the updated account plus the risk assessment
     * for the transaction. The withdrawal is still applied even if flagged
     * or if the risk service is unreachable (matches original CLI behavior
     * of degrading gracefully) - the caller decides what to do with the flag.
     */
    public record WithdrawalResult(DatabaseManager.AccountData account, RiskAnalysis risk) {}

    public DatabaseManager.AccountData createAccount(int customerId, String bankType, double initialDeposit) {
        if (initialDeposit < 0) {
            throw new ValidationException("Initial deposit cannot be negative");
        }

        // Confirms the customer exists (throws 404 otherwise)
        customerService.getCustomer(customerId);

        List<DatabaseManager.AccountData> existing = customerService.getCustomerAccounts(customerId);
        if (existing.size() >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw new ValidationException("Customer already has the maximum of " + MAX_ACCOUNTS_PER_CUSTOMER + " accounts");
        }

        String prefix = prefixForBankType(bankType);
        boolean alreadyHasThisBank = existing.stream()
                .anyMatch(acc -> acc.accountNumber.startsWith(prefix));
        if (alreadyHasThisBank) {
            throw new ValidationException("Customer already has an account with this bank");
        }

        Bank bank = newBank(bankType, initialDeposit);

        try {
            db.createAccount(customerId, bank.getAccountNumber(), "CHECKING", initialDeposit, null, null);
            return db.getAccount(bank.getAccountNumber());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account: " + e.getMessage(), e);
        }
    }

    public DatabaseManager.AccountData getAccount(String accountNumber) {
        try {
            DatabaseManager.AccountData account = db.getAccount(accountNumber);
            if (account == null) {
                throw new AccountNotFoundException(accountNumber);
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load account: " + e.getMessage(), e);
        }
    }

    public DatabaseManager.AccountData deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Deposit amount must be positive");
        }

        DatabaseManager.AccountData account = getAccount(accountNumber);
        Bank bank = recreateBank(account.accountNumber, account.balance);
        bank.changeFundsUp(amount);

        try {
            db.updateBalance(accountNumber, bank.getBankDeposit());
            db.logTransaction(accountNumber, "DEPOSIT", amount, bank.getBankDeposit(), "Deposit to account");
            return db.getAccount(accountNumber);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to process deposit: " + e.getMessage(), e);
        }
    }

    public WithdrawalResult withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Withdrawal amount must be positive");
        }

        DatabaseManager.AccountData account = getAccount(accountNumber);
        if (account.balance < amount) {
            throw new ValidationException("Insufficient funds: balance is $" + account.balance);
        }

        // Risk check - mirrors the original CLI flow, but instead of blocking on an
        // interactive Y/N prompt, the withdrawal proceeds and the risk verdict is
        // returned to the caller. Same graceful degradation if risk_service is down.
        RiskAnalysis risk = null;
        try {
            int recentTransactions = db.getRecentTransactionCount(accountNumber);
            risk = riskClient.analyzeTransaction(amount, "withdrawal", recentTransactions);
        } catch (Exception e) {
            // risk_service unreachable - proceed without a risk verdict, same as the CLI did
        }

        Bank bank = recreateBank(account.accountNumber, account.balance);
        bank.changeFundsDown(amount);

        try {
            db.updateBalance(accountNumber, bank.getBankDeposit());
            db.logTransaction(accountNumber, "WITHDRAW", amount, bank.getBankDeposit(), "Withdrawal from account");
            return new WithdrawalResult(db.getAccount(accountNumber), risk);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to process withdrawal: " + e.getMessage(), e);
        }
    }

    public void transfer(String fromAccount, String toAccount, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Transfer amount must be positive");
        }
        // These throw AccountNotFoundException if either side doesn't exist
        getAccount(fromAccount);
        getAccount(toAccount);

        try {
            db.transferBetweenAccounts(fromAccount, toAccount, amount);
        } catch (SQLException e) {
            throw new ValidationException("Transfer failed: " + e.getMessage());
        }
    }

    public List<DatabaseManager.Transaction> getTransactionHistory(String accountNumber, int limit) {
        // Confirms the account exists (throws 404 otherwise)
        getAccount(accountNumber);
        try {
            return db.getTransactionHistory(accountNumber, limit);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transaction history: " + e.getMessage(), e);
        }
    }

    public void closeAccount(String accountNumber) {
        // Confirms the account exists (throws 404 otherwise)
        getAccount(accountNumber);
        try {
            db.deleteAccount(accountNumber);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close account: " + e.getMessage(), e);
        }
    }

    // ============ HELPERS ============

    private static String prefixForBankType(String bankType) {
        return switch (normalizeBankType(bankType)) {
            case "BNY" -> "BNY";
            case "CH" -> "CHS";
            case "CA" -> "CAP";
            default -> throw new ValidationException("Unknown bank type: " + bankType + " (expected BNY, CH, or CA)");
        };
    }

    private static Bank newBank(String bankType, double initialDeposit) {
        return switch (normalizeBankType(bankType)) {
            case "BNY" -> new BNYMellon(initialDeposit);
            case "CH" -> new Chase(initialDeposit);
            case "CA" -> new CapitalOne(initialDeposit);
            default -> throw new ValidationException("Unknown bank type: " + bankType + " (expected BNY, CH, or CA)");
        };
    }

    /** Rebuilds the correct Bank subtype (by account number prefix) at a given balance,
     *  so deposit/withdraw can reuse the existing domain logic (changeFundsUp/Down)
     *  instead of duplicating balance arithmetic in the service layer. */
    private static Bank recreateBank(String accountNumber, double balance) {
        if (accountNumber.startsWith("BNY")) {
            return new BNYMellon(balance, accountNumber);
        } else if (accountNumber.startsWith("CHS")) {
            return new Chase(balance, accountNumber);
        } else if (accountNumber.startsWith("CAP")) {
            return new CapitalOne(balance, accountNumber);
        }
        throw new IllegalStateException("Unrecognized account number prefix: " + accountNumber);
    }

    private static String normalizeBankType(String bankType) {
        return bankType == null ? "" : bankType.trim().toUpperCase();
    }
}
