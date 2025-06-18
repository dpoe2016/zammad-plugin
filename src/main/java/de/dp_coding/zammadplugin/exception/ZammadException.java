package de.dp_coding.zammadplugin.exception;

/**
 * Base exception class for all Zammad plugin exceptions.
 * This provides a common type for catching all plugin-specific exceptions.
 */
public class ZammadException extends Exception {
    
    /**
     * Constructs a new ZammadException with the specified detail message.
     *
     * @param message the detail message
     */
    public ZammadException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ZammadException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ZammadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new ZammadException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public ZammadException(Throwable cause) {
        super(cause);
    }
}