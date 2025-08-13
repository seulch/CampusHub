package com.campuseventhub.model.notification;

public enum NotificationType {
    EVENT_REMINDER("Event Reminder"),
    EVENT_REGISTRATION_CONFIRMATION("Registration Confirmation"),
    EVENT_UPDATE("Event Update"),
    EVENT_CANCELLATION("Event Cancellation"),
    WAITLIST_PROMOTION("Waitlist Promotion"),
    WAITLIST_POSITION_UPDATE("Waitlist Position Update"),
    WAITLIST_REGISTRATION("Waitlist Registration"),
    SYSTEM_ANNOUNCEMENT("System Announcement"),
    SYSTEM_ALERT("System Alert");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
