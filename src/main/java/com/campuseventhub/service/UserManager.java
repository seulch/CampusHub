// =============================================================================
// USER MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.UserStatus;
import com.campuseventhub.persistence.UserRepository;
import com.campuseventhub.persistence.DataManager;
import com.campuseventhub.util.ValidationUtil;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * Simplified service for managing user accounts using composition of specialized services.
 * 
 * Implementation Details:
 * - Coordinates between specialized user services
 * - Implements UserRepository interface
 * - Delegates to specialized services for specific operations
 */
public class UserManager implements UserRepository {
    private Map<String, User> users;
    private Map<String, User> usersByEmail;
    private Map<String, User> usersByUsername;
    
    // Specialized services
    private UserAuthenticationService authService;
    private UserSearchService searchService;
    
    /**
     * Initializes thread-safe user storage maps and specialized services
     */
    public UserManager() {
        this.users = new ConcurrentHashMap<>();
        this.usersByEmail = new ConcurrentHashMap<>();
        this.usersByUsername = new ConcurrentHashMap<>();
        
        loadUsersFromPersistence();
        
        // Initialize specialized services
        this.authService = new UserAuthenticationService(usersByUsername);
        this.searchService = new UserSearchService(users, usersByEmail, usersByUsername);
    }
    
    /**
     * Creates and persists a user. Implements UserRepository interface.
     */
    @Override
    public void create(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }
        
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (usersByUsername.containsKey(user.getUsername().trim()) || usersByEmail.containsKey(normalizedEmail)) {
            throw new IllegalArgumentException("Username or email already exists");
        }
        
        users.put(user.getUserId(), user);
        usersByEmail.put(normalizedEmail, user);
        usersByUsername.put(user.getUsername().trim(), user);
        saveUsersToPersistence();
    }
    
    /**
     * Finds user by ID. Implements UserRepository interface.
     */
    @Override
    public User findById(String userId) {
        return users.get(userId);
    }
    
    /**
     * Returns all users. Implements UserRepository interface.
     */
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Updates existing user. Implements UserRepository interface.
     */
    @Override
    public void update(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }
        
        User existingUser = users.get(user.getUserId());
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found: " + user.getUserId());
        }
        
        // Update indices if username or email changed
        if (!existingUser.getUsername().equals(user.getUsername())) {
            usersByUsername.remove(existingUser.getUsername());
            usersByUsername.put(user.getUsername(), user);
        }
        
        if (!existingUser.getEmail().toLowerCase().equals(user.getEmail().toLowerCase())) {
            usersByEmail.remove(existingUser.getEmail().toLowerCase());
            usersByEmail.put(user.getEmail().toLowerCase(), user);
        }
        
        users.put(user.getUserId(), user);
        saveUsersToPersistence();
    }
    
    /**
     * Deletes user by ID. Implements UserRepository interface.
     */
    @Override
    public void deleteById(String userId) {
        User user = users.remove(userId);
        if (user != null) {
            usersByUsername.remove(user.getUsername());
            usersByEmail.remove(user.getEmail().toLowerCase());
            saveUsersToPersistence();
        }
    }

    /**
     * Creates a new user account with specified role using UserFactory
     * PARAMS: username, email, password, firstName, lastName, role
     */
    public User createUser(String username, String email, String password,
                          String firstName, String lastName, UserRole role) {
        // Check for existing username/email
        if (searchService.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (searchService.isEmailTaken(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Use factory to create user
        User user = UserFactory.createUser(username, email, password, firstName, lastName, role);
        
        // Use repository pattern to persist
        create(user);
        return user;
    }
    
    /**
     * Retrieves user by unique user ID
     * PARAMS: userId
     */
    public User getUserById(String userId) {
        return findById(userId);
    }
    
    /**
     * Validates user credentials for login using authentication service
     * PARAMS: username, password
     */
    public User validateCredentials(String username, String password) {
        return authService.validateCredentials(username, password);
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
        return findAll();
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
        User user = findById(userId);
        if (user != null) {
            deleteById(userId);
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
    
    /**
     * Loads users from persistence layer on startup
     */
    @SuppressWarnings("unchecked")
    private void loadUsersFromPersistence() {
        try {
            Object data = DataManager.loadData("users.ser");
            if (data instanceof Map) {
                Map<String, User> loadedUsers = (Map<String, User>) data;
                for (User user : loadedUsers.values()) {
                    users.put(user.getUserId(), user);
                    usersByEmail.put(user.getEmail(), user);
                    usersByUsername.put(user.getUsername(), user);
                }
                System.out.println("Loaded " + loadedUsers.size() + " users from persistence");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing user data found or failed to load: " + e.getMessage());
            // This is normal on first run
        }
    }
    
    /**
     * Saves users to persistence layer
     */
    private void saveUsersToPersistence() {
        try {
            DataManager.saveData("users.ser", new ConcurrentHashMap<>(users));
        } catch (IOException e) {
            System.err.println("Failed to save users to persistence: " + e.getMessage());
        }
    }
    
    /**
     * Gets users pending approval
     */
    public List<User> getPendingApprovals() {
        List<User> pendingUsers = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                pendingUsers.add(user);
            }
        }
        return pendingUsers;
    }
    
    /**
     * Approves a user account
     */
    public boolean approveUser(String userId) {
        User user = users.get(userId);
        if (user != null && user.getStatus() == UserStatus.PENDING_APPROVAL) {
            user.setStatus(UserStatus.ACTIVE);
            saveUsersToPersistence();
            System.out.println("UserManager: Approved user " + userId + " (" + user.getUsername() + ")");
            return true;
        }
        System.out.println("UserManager: Failed to approve user " + userId + " - user not found or not pending approval");
        return false;
    }
    
    /**
     * Suspends a user account
     */
    public boolean suspendUser(String userId) {
        User user = users.get(userId);
        if (user != null && user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.SUSPENDED);
            saveUsersToPersistence();
            System.out.println("UserManager: Suspended user " + userId + " (" + user.getUsername() + ")");
            return true;
        }
        System.out.println("UserManager: Failed to suspend user " + userId + " - user not found or not active");
        return false;
    }
}