package banking;

/**
 * Thrown when a requested account number doesn't exist or is inactive.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("No active account found with number: " + accountNumber);
    }
}
