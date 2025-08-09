// =============================================================================
// NOTIFICATION SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.strategy.NotificationStrategy;
import com.campuseventhub.model.notification.Notification;
import com.campuseventhub.model.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling all system notifications using Strategy pattern.
 * 
 * Implementation Details:
 * - Multiple notification delivery strategies
 * - Bulk notification processing
 * - Notification template management
 * - Delivery status tracking
 * - Rate limiting and throttling
 * - User preference respect
 */
public class NotificationService {
    private List<NotificationStrategy> strategies;
    private Map<String, List<Notification>> userNotifications;
    private NotificationTemplateManager templateManager;

    public NotificationService() {
        this.strategies = new ArrayList<>();
        this.userNotifications = new ConcurrentHashMap<>();
        this.templateManager = new NotificationTemplateManager();
        this.templateManager.loadTemplates();
    }

    public void sendNotification(String message, List<String> recipients,
                               NotificationType type) {
        if (message == null || message.trim().isEmpty() || recipients == null || recipients.isEmpty()) {
            return;
        }
        
        String template = templateManager.getTemplate(type);
        String finalMessage = template != null ? template.replace("{message}", message) : message;
        
        // Create notification for each recipient
        for (String recipientId : recipients) {
            Notification notification = new Notification(recipientId, finalMessage, type);
            userNotifications.computeIfAbsent(recipientId, k -> new ArrayList<>()).add(notification);
        }
    }
    
    public void addNotificationStrategy(NotificationStrategy strategy) {
        if (strategy != null) {
            strategies.add(strategy);
        }
    }
    
    public void scheduleNotification(String message, List<String> recipients,
                                   LocalDateTime sendTime, NotificationType type) {
        // Simple implementation for now - just send immediately if time has passed
        if (sendTime.isBefore(LocalDateTime.now()) || sendTime.isEqual(LocalDateTime.now())) {
            sendNotification(message, recipients, type);
        }
        // TODO: Implement proper scheduling for future times
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }
    
    // TODO: Add notification management methods
    // public void markNotificationAsRead(String notificationId)
    // public boolean updateUserPreferences(String userId, Map<NotificationType, Boolean> preferences)
    // public void sendBulkNotification(String message, NotificationType type)
}