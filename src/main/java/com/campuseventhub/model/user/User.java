// =============================================================================
// ABSTRACT USER MODEL
// =============================================================================

package com.campuseventhub.model.user;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Abstract base class for all user types in the system.
 * 
 * Implementation Details:
 * - Implements Serializable for data persistence
 * - Contains common user attributes and behaviors
 * - Abstract methods for role-specific functionality
 * - Password hashing using BCrypt or similar
 * - Input validation for all setters
 * - Audit trail for user actions
 */
public abstract class User implements Serializable {
    protected String userId;
    protected String username;
    protected String email;
    protected String password; // Will be hashed
    protected String firstName;
    protected String lastName;
    protected UserStatus status;
    protected LocalDateTime createdAt;
    protected LocalDateTime lastLoginAt;
    
    protected User(String username, String email, String password, 
                   String firstName, String lastName) {
        // TODO: Generate unique userId using UUID
        // TODO: Hash password using secure algorithm (BCrypt)
        // TODO: Validate email format and username constraints
        // TODO: Set initial status based on user type
        // TODO: Set createdAt timestamp
        // TODO: Initialize any collections
    }
    
    public boolean login(String username, String password) {
        // TODO: Verify username and hashed password
        // TODO: Check if account is active and not suspended
        // TODO: Update lastLoginAt timestamp
        // TODO: Log successful/failed login attempts
        // TODO: Return authentication result
        return false;
    }
    
    public void logout() {
        // TODO: Clear any session data
        // TODO: Log logout event
        // TODO: Notify observers of logout event
    }
    
    public void updateProfile(String firstName, String lastName, String email) {
        // TODO: Validate input parameters
        // TODO: Check if email is already in use by another user
        // TODO: Update profile information
        // TODO: Log profile update event
        // TODO: Notify observers of profile changes
    }
    
    // Abstract method to be implemented by subclasses
    public abstract UserRole getRole();
    
    public boolean isActive() {
        // TODO: Check if user status is ACTIVE
        // TODO: Consider any temporary restrictions
        return false;
    }
    
    // TODO: Add getters and setters with proper validation
    // TODO: Add equals() and hashCode() methods
    // TODO: Add toString() method for debugging
}