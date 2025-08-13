// =============================================================================
// CONFIGURATION MANAGEMENT
// =============================================================================

package com.campuseventhub.config;

import java.util.Properties;

/**
 * Centralized configuration management for application settings.
 * 
 * Implementation Details:
 * - Load configuration from application.properties
 * - Provide type-safe getters for different config values
 * - Support for environment-specific overrides
 * - Validation of required configuration parameters
 * - Default values for optional settings
 */
public class ApplicationConfig {
    private static ApplicationConfig instance;
    private static final Object lock = new Object();
    private Properties properties;
    
    private ApplicationConfig() {
        // TODO: Load properties from resources/config/application.properties
        // TODO: Support environment variable overrides
        // TODO: Validate required configuration keys
        // TODO: Set default values for optional configurations
    }
    
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    // TODO: Add getters for various config values
    // public String getDatabasePath()
    // public int getMaxEventsPerUser()
    // public int getSessionTimeoutMinutes()
    // public boolean isEmailNotificationEnabled()
}
