// =============================================================================
// NOTIFICATION TEMPLATE MANAGER
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.notification.NotificationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages notification message templates.
 *
 * Implementation Details:
 * - In-memory storage for templates
 * - Loading default templates
 * - Retrieving templates by type
 */
public class NotificationTemplateManager {
    private Map<NotificationType, String> templates;

    public NotificationTemplateManager() {
        this.templates = new HashMap<>();
    }

    public void loadTemplates() {
        // Load default templates
        templates.put(NotificationType.EVENT_REGISTRATION_CONFIRMATION, 
                     "Event Registration Confirmed: {message}");
        templates.put(NotificationType.EVENT_CANCELLATION, 
                     "Event Cancelled: {message}");
        templates.put(NotificationType.EVENT_REMINDER, 
                     "Event Reminder: {message}");
        templates.put(NotificationType.SYSTEM_ANNOUNCEMENT, 
                     "System Announcement: {message}");
    }

    public String getTemplate(NotificationType type) {
        return templates.getOrDefault(type, "Notification: {message}");
    }
}
