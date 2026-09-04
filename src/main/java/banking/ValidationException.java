package banking;

/**
 * Thrown for business-rule violations that were previously enforced inline
 * in Interface.java's CLI prompts (negative amounts, insufficient funds,
 * account limits, duplicate account types, etc).
 * Mapped to HTTP 400 by GlobalExceptionHandler.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
