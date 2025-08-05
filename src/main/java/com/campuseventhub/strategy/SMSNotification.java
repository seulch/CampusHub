package com.campuseventhub.strategy;

/**
 * SMS notification strategy implementation.
 */
public class SMSNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // TODO: Implement SMS sending
        return false;
    }

    @Override
    public String getDeliveryMethod() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        // TODO: Check SMS service status
        return true;
    }
}
