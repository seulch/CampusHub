package com.campuseventhub.strategy;

/**
 * Email notification strategy implementation.
 */
public class EmailNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // Simulated email sending
        System.out.println("[EMAIL] To: " + recipient);
        System.out.println("[EMAIL] Message: " + message);
        System.out.println("[EMAIL] Status: Delivered\n");
        return true;
    }

    @Override
    public String getDeliveryMethod() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        // Simulated email service availability check
        return true;
    }
}
