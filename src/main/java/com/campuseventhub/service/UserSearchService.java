package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.UserStatus;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for searching and filtering users by various criteria.
 * Handles user lookup operations and query functionality.
 */
public class UserSearchService {
    
    private final Map<String, User> users;
    private final Map<String, User> usersByEmail;
    private final Map<String, User> usersByUsername;
    
    public UserSearchService(Map<String, User> users, 
                           Map<String, User> usersByEmail, 
                           Map<String, User> usersByUsername) {
        this.users = users;
        this.usersByEmail = usersByEmail;
        this.usersByUsername = usersByUsername;
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return usersByUsername.get(username.trim());
    }
    
    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return usersByEmail.get(email.trim().toLowerCase());
    }
    
    /**
     * Find users by role
     */
    public List<User> findByRole(UserRole role) {
        return users.values().stream()
            .filter(user -> user.getRole() == role)
            .collect(Collectors.toList());
    }
    
    /**
     * Find users by status
     */
    public List<User> findByStatus(UserStatus status) {
        return users.values().stream()
            .filter(user -> user.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    /**
     * Search users by name (first or last name contains keyword)
     */
    public List<User> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.trim().toLowerCase();
        return users.values().stream()
            .filter(user -> 
                user.getFirstName().toLowerCase().contains(lowerKeyword) ||
                user.getLastName().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }
    
    /**
     * Get active users only
     */
    public List<User> getActiveUsers() {
        return findByStatus(UserStatus.ACTIVE);
    }
    
    /**
     * Check if username is already taken
     */
    public boolean isUsernameTaken(String username) {
        return usersByUsername.containsKey(username.trim());
    }
    
    /**
     * Check if email is already taken
     */
    public boolean isEmailTaken(String email) {
        return usersByEmail.containsKey(email.trim().toLowerCase());
    }
}