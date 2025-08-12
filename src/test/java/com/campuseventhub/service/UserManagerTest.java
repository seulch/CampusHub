package com.campuseventhub.service;

import com.campuseventhub.model.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

class UserManagerTest {
    
    private UserManager userManager;
    
    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        // Clean up any existing persistence data for clean test state
        try {
            com.campuseventhub.persistence.DataManager.deleteDataFile("users.ser");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        userManager = new UserManager(); // Reinitialize after cleanup
    }
    
    @Test
    @DisplayName("Should create admin user successfully")
    void testCreateAdminUser() {
        User user = userManager.createUser("admin1", "admin@test.com", "password123", 
                                         "Admin", "User", UserRole.ADMIN);
        
        assertNotNull(user);
        assertEquals("admin1", user.getUsername());
        assertEquals("admin@test.com", user.getEmail());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertTrue(user.isActive());
    }
    
    @Test
    @DisplayName("Should create organizer user successfully")
    void testCreateOrganizerUser() {
        User user = userManager.createUser("organizer1", "organizer@test.com", "password123", 
                                         "Organizer", "User", UserRole.ORGANIZER);
        
        assertNotNull(user);
        assertEquals("organizer1", user.getUsername());
        assertEquals("organizer@test.com", user.getEmail());
        assertEquals(UserRole.ORGANIZER, user.getRole());
    }
    
    @Test
    @DisplayName("Should create attendee user successfully")
    void testCreateAttendeeUser() {
        User user = userManager.createUser("attendee1", "attendee@test.com", "password123", 
                                         "Attendee", "User", UserRole.ATTENDEE);
        
        assertNotNull(user);
        assertEquals("attendee1", user.getUsername());
        assertEquals("attendee@test.com", user.getEmail());
        assertEquals(UserRole.ATTENDEE, user.getRole());
    }
    
    // @Test
    // @DisplayName("Should return null for invalid user creation")
    // void testCreateUserWithInvalidData() {
    //     User user = userManager.createUser("", "email@test.com", "password123", 
    //                                      "First", "Last", UserRole.ATTENDEE);
    //     assertNull(user);
    //     
    //     user = userManager.createUser("username", "", "password123", 
    //                                 "First", "Last", UserRole.ATTENDEE);
    //     assertNull(user);
    //     
    //     user = userManager.createUser("username", "email@test.com", "", 
    //                                 "First", "Last", UserRole.ATTENDEE);
    //     assertNull(user);
    // }
    
    @Test
    @DisplayName("Should throw exceptions for invalid user creation")
    void testCreateUserWithInvalidDataThrowsExceptions() {
        // Test invalid username
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("ab", "email@test.com", "password123", 
                                 "First", "Last", UserRole.ATTENDEE);
        });
        
        // Test invalid email
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("username", "invalid-email", "password123", 
                                 "First", "Last", UserRole.ATTENDEE);
        });
        
        // Test invalid password
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("username", "email@test.com", "weak", 
                                 "First", "Last", UserRole.ATTENDEE);
        });
        
        // Test invalid name
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("username", "email@test.com", "password123", 
                                 "Invalid123", "Last", UserRole.ATTENDEE);
        });
    }
    
    @Test
    @DisplayName("Should validate credentials successfully")
    void testValidateCredentials() {
        userManager.createUser("testuser", "test@test.com", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        User authenticatedUser = userManager.validateCredentials("testuser", "password123");
        assertNotNull(authenticatedUser);
        assertEquals("testuser", authenticatedUser.getUsername());
    }
    
    // @Test
    // @DisplayName("Should return null for invalid credentials")
    // void testValidateInvalidCredentials() {
    //     userManager.createUser("testuser", "test@test.com", "password123", 
    //                          "Test", "User", UserRole.ATTENDEE);
    //     
    //     User authenticatedUser = userManager.validateCredentials("testuser", "wrongpassword");
    //     assertNull(authenticatedUser);
    //     
    //     authenticatedUser = userManager.validateCredentials("nonexistent", "password123");
    //     assertNull(authenticatedUser);
    // }
    
    @Test
    @DisplayName("Should return null for invalid credentials")
    void testValidateInvalidCredentialsClean() {
        userManager.createUser("uniqueuser1", "unique1@test.com", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        User authenticatedUser = userManager.validateCredentials("uniqueuser1", "wrongpassword");
        assertNull(authenticatedUser);
        
        authenticatedUser = userManager.validateCredentials("nonexistentuser", "password123");
        assertNull(authenticatedUser);
    }
    
    // @Test
    // @DisplayName("Should update user information successfully")
    // void testUpdateUser() {
    //     User user = userManager.createUser("testuser", "test@test.com", "password123", 
    //                                      "Test", "User", UserRole.ATTENDEE);
    //     
    //     Map<String, Object> updates = new HashMap<>();
    //     updates.put("firstName", "Updated");
    //     updates.put("lastName", "Name");
    //     
    //     boolean result = userManager.updateUser(user.getUserId(), updates);
    //     assertTrue(result);
    //     
    //     User updatedUser = userManager.getUserById(user.getUserId());
    //     assertEquals("Updated", updatedUser.getFirstName());
    //     assertEquals("Name", updatedUser.getLastName());
    // }
    
    @Test
    @DisplayName("Should update user information successfully")
    void testUpdateUserClean() {
        User user = userManager.createUser("uniqueuser2", "unique2@test.com", "password123", 
                                         "Test", "User", UserRole.ATTENDEE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", "Updated");
        updates.put("lastName", "Name");
        
        boolean result = userManager.updateUser(user.getUserId(), updates);
        assertTrue(result);
        
        User updatedUser = userManager.getUserById(user.getUserId());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }
    
    // @Test
    // @DisplayName("Should get all users")
    // void testGetAllUsers() {
    //     userManager.createUser("user1", "user1@test.com", "password123", 
    //                          "User", "One", UserRole.ATTENDEE);
    //     userManager.createUser("user2", "user2@test.com", "password123", 
    //                          "User", "Two", UserRole.ORGANIZER);
    //     
    //     List<User> allUsers = userManager.getAllUsers();
    //     assertEquals(2, allUsers.size());
    // }
    
    @Test
    @DisplayName("Should get all users including new ones")
    void testGetAllUsersIncludesNew() {
        int initialCount = userManager.getAllUsers().size();
        
        userManager.createUser("newuser1", "newuser1@test.com", "password123", 
                             "New", "One", UserRole.ATTENDEE);
        userManager.createUser("newuser2", "newuser2@test.com", "password123", 
                             "New", "Two", UserRole.ORGANIZER);
        
        List<User> allUsers = userManager.getAllUsers();
        assertEquals(initialCount + 2, allUsers.size());
    }
    
    @Test
    @DisplayName("Should get users by role")
    void testGetUsersByRole() {
        userManager.createUser("attendee1", "attendee1@test.com", "password123", 
                             "Attendee", "One", UserRole.ATTENDEE);
        userManager.createUser("attendee2", "attendee2@test.com", "password123", 
                             "Attendee", "Two", UserRole.ATTENDEE);
        userManager.createUser("organizer1", "organizer1@test.com", "password123", 
                             "Organizer", "One", UserRole.ORGANIZER);
        
        List<User> attendees = userManager.getUsersByRole(UserRole.ATTENDEE);
        assertEquals(2, attendees.size());
        
        List<User> organizers = userManager.getUsersByRole(UserRole.ORGANIZER);
        assertEquals(1, organizers.size());
    }
    
    @Test
    @DisplayName("Should check username availability")
    void testUsernameAvailability() {
        assertTrue(userManager.isUsernameAvailable("newuser"));
        
        userManager.createUser("existinguser", "existing@test.com", "password123", 
                             "Existing", "User", UserRole.ATTENDEE);
        
        assertFalse(userManager.isUsernameAvailable("existinguser"));
        assertTrue(userManager.isUsernameAvailable("differentuser"));
    }
    
    @Test
    @DisplayName("Should check email availability")
    void testEmailAvailability() {
        assertTrue(userManager.isEmailAvailable("new@test.com"));
        
        userManager.createUser("user", "existing@test.com", "password123", 
                             "Existing", "User", UserRole.ATTENDEE);
        
        assertFalse(userManager.isEmailAvailable("existing@test.com"));
        assertTrue(userManager.isEmailAvailable("different@test.com"));
    }
    
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        User user = userManager.createUser("todelete", "delete@test.com", "password123", 
                                         "Delete", "User", UserRole.ATTENDEE);
        
        boolean result = userManager.deleteUser(user.getUserId());
        assertTrue(result);
        
        User deletedUser = userManager.getUserById(user.getUserId());
        assertNull(deletedUser);
    }
    
    // NEW TESTS FOR ENHANCED USERMANAGER FEATURES -----------------------------------------------------------
    
    @Test
    @DisplayName("Should prevent duplicate username or email")
    void testDuplicatePrevention() {
        userManager.createUser("uniqueuser3", "unique3@test.com", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        // Try to create user with same username
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("uniqueuser3", "different@test.com", "password123", 
                                 "Different", "User", UserRole.ATTENDEE);
        });
        
        // Try to create user with same email
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("differentuser", "unique3@test.com", "password123", 
                                 "Different", "User", UserRole.ATTENDEE);
        });
    }
    
    @Test
    @DisplayName("Should handle user approval workflow")
    void testUserApprovalWorkflow() {
        // Create a user (should be ACTIVE by default)
        User user = userManager.createUser("pendinguser", "pending@test.com", "password123", 
                                         "Pending", "User", UserRole.ORGANIZER);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        
        // Set to pending (simulating manual status change)
        user.setStatus(UserStatus.PENDING_APPROVAL);
        
        // Test approval
        boolean approved = userManager.approveUser(user.getUserId());
        assertTrue(approved);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    @DisplayName("Should handle user suspension")
    void testUserSuspension() {
        User user = userManager.createUser("suspenduser", "suspend@test.com", "password123", 
                                         "Suspend", "User", UserRole.ATTENDEE);
        assertTrue(user.isActive());
        
        // Suspend user
        boolean suspended = userManager.suspendUser(user.getUserId());
        assertTrue(suspended);
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        assertFalse(user.isActive());
        
        // Should not be able to login when suspended
        User authResult = userManager.validateCredentials("suspenduser", "password123");
        assertNull(authResult);
    }
    
    @Test
    @DisplayName("Should handle persistence operations")
    void testPersistenceIntegration() {
        int initialUserCount = userManager.getAllUsers().size();
        
        // Create a user - should be automatically persisted
        User user = userManager.createUser("persistuser", "persist@test.com", "password123", 
                                         "Persist", "User", UserRole.ATTENDEE);
        assertNotNull(user);
        
        // Create new UserManager instance to test loading
        UserManager newUserManager = new UserManager();
        
        // Should load persisted users
        List<User> loadedUsers = newUserManager.getAllUsers();
        assertEquals(initialUserCount + 1, loadedUsers.size());
        
        // Should be able to find the persisted user
        User loadedUser = loadedUsers.stream()
            .filter(u -> "persistuser".equals(u.getUsername()))
            .findFirst()
            .orElse(null);
        assertNotNull(loadedUser);
        assertEquals("persist@test.com", loadedUser.getEmail());
    }
    
    @Test
    @DisplayName("Should handle enhanced validation in all operations")
    void testEnhancedValidation() {
        // All user creation should validate inputs
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("ab", "test@test.com", "password123", 
                                 "Test", "User", UserRole.ATTENDEE);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "invalid", "password123", 
                                 "Test", "User", UserRole.ATTENDEE);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "test@test.com", "weak", 
                                 "Test", "User", UserRole.ATTENDEE);
        });
    }
    
    @Test
    @DisplayName("Should maintain data consistency across operations")
    void testDataConsistency() {
        User user = userManager.createUser("consistuser", "consist@test.com", "password123", 
                                         "Consist", "User", UserRole.ATTENDEE);
        
        // Username and email should be unavailable
        assertFalse(userManager.isUsernameAvailable("consistuser"));
        assertFalse(userManager.isEmailAvailable("consist@test.com"));
        
        // Delete user
        userManager.deleteUser(user.getUserId());
        
        // Username and email should become available again
        assertTrue(userManager.isUsernameAvailable("consistuser"));
        assertTrue(userManager.isEmailAvailable("consist@test.com"));
    }
} 