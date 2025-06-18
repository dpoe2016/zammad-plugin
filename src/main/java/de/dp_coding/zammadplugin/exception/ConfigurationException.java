package de.dp_coding.zammadplugin.exception;

/**
 * Exception thrown when there is a configuration error in the Zammad plugin.
 * This includes missing URL, API token, or other configuration issues.
 */
public class ConfigurationException extends ZammadException {
    
    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}