package com.campuseventhub.strategy;

/**
 * In-app notification strategy implementation.
 */
public class InAppNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // Simulated in-app notification
        System.out.println("[IN-APP] To: " + recipient);
        System.out.println("[IN-APP] Message: " + message);
        System.out.println("[IN-APP] Status: Displayed\n");
        return true;
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
