package banking;

/**
 * Thrown when a requested customer ID doesn't exist.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(int customerId) {
        super("No customer found with ID: " + customerId);
    }
}
