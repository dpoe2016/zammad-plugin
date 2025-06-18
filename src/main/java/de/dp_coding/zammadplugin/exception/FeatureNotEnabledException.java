package de.dp_coding.zammadplugin.exception;

/**
 * Exception thrown when a feature is not enabled in the Zammad instance.
 * This is used for features like time accounting that may not be enabled in all Zammad instances.
 */
public class FeatureNotEnabledException extends ZammadException {
    
    private final String featureName;
    
    /**
     * Constructs a new FeatureNotEnabledException with the specified feature name.
     *
     * @param featureName the name of the feature that is not enabled
     */
    public FeatureNotEnabledException(String featureName) {
        super(featureName + " is not enabled in your Zammad instance. Please contact your Zammad administrator to enable this feature.");
        this.featureName = featureName;
    }
    
    /**
     * Constructs a new FeatureNotEnabledException with the specified feature name and custom message.
     *
     * @param featureName the name of the feature that is not enabled
     * @param message the detail message
     */
    public FeatureNotEnabledException(String featureName, String message) {
        super(message);
        this.featureName = featureName;
    }
    
    /**
     * Gets the name of the feature that is not enabled.
     *
     * @return the feature name
     */
    public String getFeatureName() {
        return featureName;
    }
}