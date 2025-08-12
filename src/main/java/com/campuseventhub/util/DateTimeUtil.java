// =============================================================================
// UTILITY CLASSES
// =============================================================================

package com.campuseventhub.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations. (separate later)
 **/
public class DateTimeUtil {
    public static final DateTimeFormatter EVENT_DATETIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Validates event date/time is in the future and within reasonable bounds
     */
    public static boolean isValidEventDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxFuture = now.plusYears(2); // Events can be scheduled up to 2 years in advance
        
        return dateTime.isAfter(now) && dateTime.isBefore(maxFuture);
    }
    
    /**
     * Checks if two time periods overlap
     */
    public static boolean hasTimeConflict(LocalDateTime start1, LocalDateTime end1, 
                                         LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        // Check if one event starts before the other ends and vice versa
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
    /**
     * Formats date/time for display in UI
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(EVENT_DATETIME_FORMAT);
    }
    
    /**
     * Parses date/time string into LocalDateTime
     */
    public static LocalDateTime parseFromString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeString.trim(), EVENT_DATETIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Validates that end time is after start time
     */
    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return endTime.isAfter(startTime);
    }
    
    /**
     * Gets duration in hours between two times
     */
    public static long getDurationHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.Duration.between(start, end).toHours();
    }
}