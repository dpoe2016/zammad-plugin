package de.dp_coding.zammadplugin.exception;

/**
 * Exception thrown when there is an error communicating with the Zammad API.
 * This includes network errors, authentication errors, and API response errors.
 */
public class ApiException extends ZammadException {
    
    private final int statusCode;
    
    /**
     * Constructs a new ApiException with the specified detail message.
     *
     * @param message the detail message
     */
    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
    }
    
    /**
     * Constructs a new ApiException with the specified detail message and HTTP status code.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructs a new ApiException with the specified detail message, cause, and HTTP status code.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param statusCode the HTTP status code
     */
    public ApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructs a new ApiException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }
    
    /**
     * Gets the HTTP status code associated with this exception.
     *
     * @return the HTTP status code, or 0 if not applicable
     */
    public int getStatusCode() {
        return statusCode;
    }
}