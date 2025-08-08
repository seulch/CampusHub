// =============================================================================
// USER MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.campuseventhub.model.user.UserStatus;

/**
 * Service for managing user accounts and authentication.
 * 
 * Implementation Details:
 * - Thread-safe user storage and retrieval
 * - Password hashing and verification
 * - User validation and business rules
 * - Account lifecycle management
 * - Security audit logging
 * - Email uniqueness enforcement
 */
public class UserManager {
    private Map<String, User> users;
    private Map<String, User> usersByEmail;
    private Map<String, User> usersByUsername;
    
    /**
     * Initializes thread-safe user storage maps
     */
    public UserManager() {
        this.users = new ConcurrentHashMap<>();
        this.usersByEmail = new ConcurrentHashMap<>();
        this.usersByUsername = new ConcurrentHashMap<>();
        // TODO: Load existing users from persistence
        // TODO: Validate data integrity
        // TODO: Set up indexing for quick lookups
    }
    
    /**
     * Creates a new user account with specified role
     * PARAMS: username, email, password, firstName, lastName, role
     */
    public User createUser(String username, String email, String password,
                          String firstName, String lastName, UserRole role) {
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            role == null) {
            return null;
        }
        
        if (usersByUsername.containsKey(username) || usersByEmail.containsKey(email)) {
            return null;
        }
        
        User user = null;
        switch (role) {
            case ADMIN:
                user = new Admin(username, email, password, firstName, lastName, "SYSTEM_ADMIN");
                break;
            case ORGANIZER:
                user = new Organizer(username, email, password, firstName, lastName, "General");
                break;
            case ATTENDEE:
                user = new Attendee(username, email, password, firstName, lastName);
                break;
            default:
                return null;
        }
        
        users.put(user.getUserId(), user);
        usersByEmail.put(email, user);
        usersByUsername.put(username, user);
        // TODO: Save to persistence layer
        // TODO: Log user creation
        return user;
    }
    
    /**
     * Retrieves user by unique user ID
     * PARAMS: userId
     */
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    /**
     * Validates user credentials for login
     * PARAMS: username, password
     */
    public User validateCredentials(String username, String password) {
        User user = usersByUsername.get(username);
        if (user == null) {
            return null;
        }
        
        if (user.login(username, password)) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Updates user information with provided field updates
     * PARAMS: userId, updates
     */
    public boolean updateUser(String userId, Map<String, Object> updates) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
            switch (field) {
                case "firstName":
                    if (value instanceof String) {
                        user.setFirstName((String) value);
                    }
                    break;
                case "lastName":
                    if (value instanceof String) {
                        user.setLastName((String) value);
                    }
                    break;
                case "email":
                    if (value instanceof String) {
                        String newEmail = (String) value;
                        if (!usersByEmail.containsKey(newEmail)) {
                            usersByEmail.remove(user.getEmail());
                            user.setEmail(newEmail);
                            usersByEmail.put(newEmail, user);
                        }
                    }
                    break;
                case "status":
                    if (value instanceof UserStatus) {
                        user.setStatus((UserStatus) value);
                    }
                    break;
            }
        }
        return true;
    }
    
    /**
     * Retrieves all users in the system
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Retrieves users filtered by specific role
     * PARAMS: role
     */
    public List<User> getUsersByRole(UserRole role) {
        List<User> usersByRole = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() == role) {
                usersByRole.add(user);
            }
        }
        return usersByRole;
    }
    
    /**
     * Deletes a user account from the system
     * PARAMS: userId
     */
    public boolean deleteUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            users.remove(userId);
            usersByEmail.remove(user.getEmail());
            usersByUsername.remove(user.getUsername());
            return true;
        }
        return false;
    }
    
    /**
     * Checks if a username is available for registration
     * PARAMS: username
     */
    public boolean isUsernameAvailable(String username) {
        return !usersByUsername.containsKey(username);
    }
    
    /**
     * Checks if an email is available for registration
     * PARAMS: email
     */
    public boolean isEmailAvailable(String email) {
        return !usersByEmail.containsKey(email);
    }
}