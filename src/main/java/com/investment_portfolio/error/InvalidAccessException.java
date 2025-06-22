package com.investment_portfolio.error;
/**
 * Exception thrown to indicate that an invalid access attempt has occurred.
 * <p>This is a runtime exception and can be used to signal unauthorized or restricted access violations within the application, such as IP mismatches, invalid authentication tokens, or permission denials.</p>
 * @see RuntimeException
 */
public class InvalidAccessException extends RuntimeException {
    /**
     * Constructs a new {@code InvalidAccessException} with the specified detail message.
     * @param message the detail message describing the reason for the exception.
     */
    public InvalidAccessException(String message) {
        super(message);
    }
}
