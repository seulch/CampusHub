package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import java.util.Map;

/**
 * Service dedicated to user authentication and credential validation.
 * Handles login logic and session management.
 */
public class UserAuthenticationService {
    
    private final Map<String, User> usersByUsername;
    
    public UserAuthenticationService(Map<String, User> usersByUsername) {
        this.usersByUsername = usersByUsername;
    }
    
    /**
     * Validates user credentials for login
     */
    public User validateCredentials(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        // Trim username to match how it's stored
        User user = usersByUsername.get(username.trim());
        if (user == null) {
            return null;
        }
        
        if (user.login(username.trim(), password)) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Checks if a user can log in (not suspended)
     */
    public boolean canUserLogin(User user) {
        return user != null && user.isActive();
    }
}