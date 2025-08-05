package com.campuseventhub.model.notification;

public enum NotificationType {
    EVENT_REMINDER("Event Reminder"),
    REGISTRATION_CONFIRMATION("Registration Confirmation"),
    EVENT_UPDATE("Event Update"),
    CANCELLATION("Cancellation"),
    SYSTEM_ALERT("System Alert");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
