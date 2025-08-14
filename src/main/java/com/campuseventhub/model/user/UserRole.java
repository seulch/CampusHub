package com.campuseventhub.model.user;

public enum UserRole {
    ORGANIZER("Organizer"),
    ATTENDEE("Attendee"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}