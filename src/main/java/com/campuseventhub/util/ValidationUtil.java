// =============================================================================
// UTILITY CLASSES
// =============================================================================

package com.campuseventhub.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Validates email format using regex pattern
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates password strength
     * Requirements: at least 8 characters, contains letters and numbers
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasNumber = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }
        
        return hasLetter && hasNumber;
    }
    
    /**
     * Validates username format
     * Requirements: 3-20 characters, alphanumeric and underscore only
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return false;
        }
        
        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Validates event title
     * Requirements: 3-100 characters, not just whitespace
     */
    public static boolean isValidEventTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = title.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 100;
    }
    
    /**
     * Validates name fields (first name, last name)
     * Requirements: 1-50 characters, letters and spaces only
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        if (trimmed.length() < 1 || trimmed.length() > 50) {
            return false;
        }
        
        return trimmed.matches("^[a-zA-Z\\s]+$");
    }
    
    /**
     * Validates event capacity
     * Requirements: positive integer between 1 and 10000
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0 && capacity <= 10000;
    }
    
    /**
     * Validates event title and returns detailed error message if invalid
     */
    public static String validateEventTitle(String title) {
        if (title == null) {
            return "Event title cannot be null";
        }
        if (title.trim().isEmpty()) {
            return "Event title cannot be empty";
        }
        
        String trimmed = title.trim();
        if (trimmed.length() < 3) {
            return "Event title must be at least 3 characters long (current: " + trimmed.length() + ")";
        }
        if (trimmed.length() > 100) {
            return "Event title must be no more than 100 characters long (current: " + trimmed.length() + ")";
        }
        
        return null; // Valid
    }
    
    /**
     * Validates event capacity and returns detailed error message if invalid
     */
    public static String validateEventCapacity(String capacityStr) {
        if (capacityStr == null || capacityStr.trim().isEmpty()) {
            return "Event capacity cannot be empty";
        }
        
        try {
            int capacity = Integer.parseInt(capacityStr.trim());
            if (capacity <= 0) {
                return "Event capacity must be a positive number (minimum: 1)";
            }
            if (capacity > 10000) {
                return "Event capacity cannot exceed 10,000 people";
            }
            return null; // Valid
        } catch (NumberFormatException e) {
            return "Event capacity must be a valid number (entered: '" + capacityStr.trim() + "')";
        }
    }
    
    /**
     * Validates date/time selection and returns detailed error message if invalid
     */
    public static String validateEventDateTime(java.time.LocalDateTime dateTime, String fieldName) {
        if (dateTime == null) {
            return fieldName + " must be selected";
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (dateTime.isBefore(now)) {
            return fieldName + " must be in the future (selected time is in the past)";
        }
        
        java.time.LocalDateTime maxFuture = now.plusYears(2);
        if (dateTime.isAfter(maxFuture)) {
            return fieldName + " cannot be more than 2 years in the future";
        }
        
        return null; // Valid
    }
    
    /**
     * Validates start and end time relationship and returns detailed error message if invalid
     */
    public static String validateTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null; // Individual validation will catch null values
        }
        
        if (!endTime.isAfter(startTime)) {
            return "End time must be after start time";
        }
        
        // Check if event is too long (more than 12 hours)
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        if (hours > 12) {
            return "Event duration cannot exceed 12 hours (current duration: " + hours + " hours)";
        }
        
        if (hours == 0) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            if (minutes < 15) {
                return "Event must be at least 15 minutes long (current duration: " + minutes + " minutes)";
            }
        }
        
        return null; // Valid
    }
}
