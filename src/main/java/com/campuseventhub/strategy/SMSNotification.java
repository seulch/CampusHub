package com.campuseventhub.strategy;

/**
 * SMS notification strategy implementation.
 */
public class SMSNotification implements NotificationStrategy {

    @Override
    public boolean sendNotification(String message, String recipient) {
        // Simulated SMS sending
        System.out.println("[SMS] To: " + recipient);
        System.out.println("[SMS] Message: " + message);
        System.out.println("[SMS] Status: Delivered\n");
        return true;
    }

    @Override
    public String getDeliveryMethod() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        // Simulated SMS service availability check
        return true;
    }
}
