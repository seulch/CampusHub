package com.campuseventhub.model.event;

public enum EventType {
    WORKSHOP("Workshop"),
    SEMINAR("Seminar"),
    CLUB_MEETING("Club Meeting"),
    GUEST_LECTURE("Guest Lecture"),
    SOCIAL_EVENT("Social Event");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
