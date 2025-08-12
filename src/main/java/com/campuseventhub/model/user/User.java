// =============================================================================
// ABSTRACT USER MODEL
// =============================================================================

package com.campuseventhub.model.user;

import com.campuseventhub.util.ValidationUtil;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

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
        // Validate all inputs
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username: " + username);
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password: password must be at least 8 characters with letters and numbers");
        }
        if (!ValidationUtil.isValidName(firstName)) {
            throw new IllegalArgumentException("Invalid first name: " + firstName);
        }
        if (!ValidationUtil.isValidName(lastName)) {
            throw new IllegalArgumentException("Invalid last name: " + lastName);
        }
        
        this.userId = java.util.UUID.randomUUID().toString();
        this.username = username.trim();
        this.email = email.trim().toLowerCase();
        this.password = hashPassword(password); // Hash the password
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Authenticates user login with username and password
     * PARAMS: username, password
     */
    public boolean login(String username, String password) {
        if (this.username.equals(username) && verifyPassword(password)) {
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
        if (firstName != null && ValidationUtil.isValidName(firstName)) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && ValidationUtil.isValidName(lastName)) {
            this.lastName = lastName.trim();
        }
        if (email != null && ValidationUtil.isValidEmail(email)) {
            this.email = email.trim().toLowerCase();
        }
    }
    
    /**
     * Hashes a password using SHA-256 with salt
     */
    private String hashPassword(String password) {
        try {
            // Add a simple salt (in production, use unique salt per user)
            String saltedPassword = password + this.username + "campuseventhub_salt";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verifies a plain text password against the stored hash
     */
    private boolean verifyPassword(String password) {
        if (password == null) {
            return false;
        }
        return this.password.equals(hashPassword(password));
    }
    
    /**
     * Updates user password with validation and hashing
     */
    public void changePassword(String newPassword) {
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Invalid password: password must be at least 8 characters with letters and numbers");
        }
        this.password = hashPassword(newPassword);
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return userId.equals(user.userId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("User{userId='%s', username='%s', email='%s', firstName='%s', lastName='%s', role='%s', status='%s'}", 
                           userId, username, email, firstName, lastName, getRole(), status);
    }
}