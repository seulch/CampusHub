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
    
    /**
     * Creates a new user with basic information
     * PARAMS: username, email, password, firstName, lastName
     */
    protected User(String username, String email, String password, 
                   String firstName, String lastName) {
        this.userId = java.util.UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.password = password; // Simple password storage for project, maybe change to secure hash algo later (Bcrypt)
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Authenticates user login with username and password
     * PARAMS: username, password
     */
    public boolean login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            if (status == UserStatus.ACTIVE) {
                this.lastLoginAt = LocalDateTime.now();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Handles user logout process
     */
    public void logout() {
        // Simple logout for project (For now)
    }
    
    /**
     * Updates user profile information
     * PARAMS: firstName, lastName, email
     */
    public void updateProfile(String firstName, String lastName, String email) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName;
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
    }
    
    /**
     * Returns the specific role of the user
     */
    public abstract UserRole getRole();
    
    /**
     * Checks if user account is currently active
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    // TODO: Add equals() and hashCode() methods
    // TODO: Add toString() method for debugging
}