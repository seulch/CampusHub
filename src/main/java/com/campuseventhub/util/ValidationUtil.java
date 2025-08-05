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

    // TODO: Add validation methods
    // public static boolean isValidEmail(String email)
    // public static boolean isValidPassword(String password)
    // public static boolean isValidUsername(String username)
    // public static boolean isValidEventTitle(String title)
}
