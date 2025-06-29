package com.investment_portfolio.error;
/**
 * Custom exception thrown when an authentication token is invalid or missing.
 * @see RuntimeException
 */
public class InvalidTokenException extends RuntimeException {
    /**
     * Constructs a new InvalidTokenException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
