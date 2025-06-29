package com.investment_portfolio.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;


/**
 * Utility class for handling error-related logging and operations.
 * <p>This class provides a centralized logger for tracking error events in the application.</p>
 * <p>The class is marked as final to prevent inheritance.</p>
 */
public final class Error {
    /**
     * The logger that is responsible of tracking the actions on the application.
     */
    private static final Logger logger = LoggerFactory.getLogger(Error.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>This constructor always throws {@link UnsupportedOperationException} to enforce non-instantiability.</p>
     */
    public Error() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Handling and logging errors during request processing.
     * @param status The HTTP status code to return.
     * @param message The human-readable error message.
     * @param error The exception instance containing the error details.
     * @return A {@link ResponseEntity} containing the error response body.
     */
    public static ResponseEntity<Object> handleError(int status, String message, Throwable error) {
        logger.error("{}\nStatus: {}\nError: {}", message, status, error.getMessage());
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
