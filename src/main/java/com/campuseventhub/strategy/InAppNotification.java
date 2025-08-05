package com.campuseventhub.strategy;

/**
 * In-app notification strategy implementation.
 */
public class InAppNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // TODO: Implement in-app notification storage
        return false;
    }

    @Override
    public String getDeliveryMethod() {
        return "IN_APP";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
