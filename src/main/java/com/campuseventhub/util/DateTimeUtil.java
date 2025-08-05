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

    // TODO: Add methods for date/time validation
    // public static boolean isValidEventDateTime(LocalDateTime dateTime)
    // public static boolean hasTimeConflict(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2)
    // public static String formatForDisplay(LocalDateTime dateTime)
    // public static LocalDateTime parseFromString(String dateTimeString)
}