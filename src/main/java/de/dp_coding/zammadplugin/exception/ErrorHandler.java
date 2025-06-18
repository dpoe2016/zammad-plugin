package de.dp_coding.zammadplugin.exception;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;

/**
 * Centralized error handler for the Zammad plugin.
 * This class provides methods for handling different types of exceptions in a consistent way.
 */
public class ErrorHandler {
    
    private static final Logger LOG = Logger.getInstance(ErrorHandler.class);
    
    /**
     * Handles an exception by logging it and showing an appropriate error message to the user.
     *
     * @param project the current project
     * @param exception the exception to handle
     * @param title the title for the error dialog
     */
    public static void handleException(Project project, Throwable exception, String title) {
        LOG.warn("Error in Zammad plugin: " + exception.getMessage(), exception);
        
        String message;
        
        if (exception instanceof ConfigurationException) {
            message = "Configuration error: " + exception.getMessage();
        } else if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            if (apiException.getStatusCode() > 0) {
                message = "API error (status " + apiException.getStatusCode() + "): " + exception.getMessage();
            } else {
                message = "API error: " + exception.getMessage();
            }
        } else if (exception instanceof FeatureNotEnabledException) {
            message = exception.getMessage();
        } else if (exception instanceof ZammadException) {
            message = "Zammad error: " + exception.getMessage();
        } else if (exception instanceof IOException) {
            message = "Network error: " + exception.getMessage();
        } else {
            message = "Unexpected error: " + exception.getMessage();
        }
        
        Messages.showErrorDialog(project, message, title);
    }
    
    /**
     * Handles an exception by logging it without showing an error message to the user.
     * This is useful for background tasks or when the application is shutting down.
     *
     * @param exception the exception to handle
     * @param context a description of the context in which the exception occurred
     */
    public static void handleExceptionSilently(Throwable exception, String context) {
        LOG.warn("Error in Zammad plugin (" + context + "): " + exception.getMessage(), exception);
    }
    
    /**
     * Converts a generic exception to a specific ZammadException.
     * This is useful for wrapping exceptions from third-party libraries.
     *
     * @param exception the exception to convert
     * @param message a message describing the error
     * @return a ZammadException that wraps the original exception
     */
    public static ZammadException convertException(Throwable exception, String message) {
        if (exception instanceof ZammadException) {
            return (ZammadException) exception;
        } else if (exception instanceof IOException) {
            return new ApiException(message, exception);
        } else {
            return new ZammadException(message, exception);
        }
    }
}