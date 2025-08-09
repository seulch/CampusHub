// =============================================================================
// NOTIFICATION MODEL
// =============================================================================

package com.campuseventhub.model.notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Basic notification model storing message content and delivery details.
 */
public class Notification {
    private String notificationId;
    private String recipientId;
    private String message;
    private List<String> recipients;
    private NotificationType type;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public Notification(String message, List<String> recipients, NotificationType type) {
        this.notificationId = java.util.UUID.randomUUID().toString();
        this.message = message;
        this.recipients = recipients;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(String recipientId, String message, NotificationType type) {
        this.notificationId = java.util.UUID.randomUUID().toString();
        this.recipientId = recipientId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public String getNotificationId() { return notificationId; }
    public String getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public List<String> getRecipients() { return recipients; }
    public NotificationType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }

    public void markSent() {
        this.sentAt = LocalDateTime.now();
    }
}
