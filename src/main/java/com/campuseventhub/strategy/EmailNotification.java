package com.campuseventhub.strategy;

/**
 * Email notification strategy implementation.
 */
public class EmailNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // TODO: Implement email sending
        return false;
    }

    @Override
    public String getDeliveryMethod() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        // TODO: Check email service status
        return true;
    }
}
