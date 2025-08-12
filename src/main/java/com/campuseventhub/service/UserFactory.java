package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.util.ValidationUtil;

/**
 * Factory class for creating different types of users based on role.
 * Follows the Factory pattern to centralize user creation logic.
 */
public class UserFactory {
    
    /**
     * Creates a new user with the specified role and details
     */
    public static User createUser(String username, String email, String password,
                                 String firstName, String lastName, UserRole role) {
        // Validate input parameters
        validateUserParameters(username, email, password, firstName, lastName, role);
        
        switch (role) {
            case ADMIN:
                return new Admin(username, email, password, firstName, lastName, "SYSTEM_ADMIN");
            case ORGANIZER:
                return new Organizer(username, email, password, firstName, lastName, "General");
            case ATTENDEE:
                return new Attendee(username, email, password, firstName, lastName);
            default:
                throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }
    
    /**
     * Validates all user creation parameters
     */
    private static void validateUserParameters(String username, String email, String password,
                                             String firstName, String lastName, UserRole role) {
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        if (!ValidationUtil.isValidName(firstName)) {
            throw new IllegalArgumentException("Invalid first name");
        }
        if (!ValidationUtil.isValidName(lastName)) {
            throw new IllegalArgumentException("Invalid last name");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }
}