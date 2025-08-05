// =============================================================================
// STRATEGY PATTERN IMPLEMENTATIONS
// =============================================================================

package com.campuseventhub.strategy;

/**
 * Strategy interface for different notification delivery methods.
 */
public interface NotificationStrategy {
    boolean sendNotification(String message, String recipient);
    String getDeliveryMethod();
    boolean isAvailable();
}