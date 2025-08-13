package com.campuseventhub.service;

import com.campuseventhub.model.user.*;
import com.campuseventhub.persistence.DataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Integration test for UserManager with persistence
 */
public class UserManagerIntegrationTest {
    
    private UserManager userManager;
    
    @BeforeEach
    public void setUp() {
        // Clean up any existing test data
        cleanup();
        userManager = new UserManager();
    }
    
    @AfterEach
    public void cleanup() {
        // Clean up test files
        DataManager.deleteDataFile("users.ser");
    }
    
    @Test
    public void testCreateUserWithValidation() {
        // Test valid user creation
        User user = userManager.createUser("testuser", "test@example.com", "password123", 
                                          "John", "Doe", UserRole.ATTENDEE);
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(UserRole.ATTENDEE, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        
        // Verify user can be retrieved
        User retrieved = userManager.getUserById(user.getUserId());
        assertNotNull(retrieved);
        assertEquals(user.getUserId(), retrieved.getUserId());
    }
    
    @Test
    public void testCreateUserValidationFailures() {
        // Test invalid username
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("ab", "test@example.com", "password123", 
                                  "John", "Doe", UserRole.ATTENDEE);
        });
        
        // Test invalid email
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "invalid-email", "password123", 
                                  "John", "Doe", UserRole.ATTENDEE);
        });
        
        // Test invalid password
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "test@example.com", "weak", 
                                  "John", "Doe", UserRole.ATTENDEE);
        });
        
        // Test invalid name
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "test@example.com", "password123", 
                                  "John123", "Doe", UserRole.ATTENDEE);
        });
    }
    
    @Test
    public void testDuplicateUserPrevention() {
        // Create first user
        userManager.createUser("testuser", "test@example.com", "password123", 
                              "John", "Doe", UserRole.ATTENDEE);
        
        // Try to create user with same username
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("testuser", "different@example.com", "password123", 
                                  "Jane", "Smith", UserRole.ATTENDEE);
        });
        
        // Try to create user with same email
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.createUser("differentuser", "test@example.com", "password123", 
                                  "Jane", "Smith", UserRole.ATTENDEE);
        });
    }
    
    @Test
    public void testUserAuthentication() {
        // Create a user
        User user = userManager.createUser("testuser", "test@example.com", "password123", 
                                          "John", "Doe", UserRole.ATTENDEE);
        
        // Test successful authentication
        User authenticated = userManager.validateCredentials("testuser", "password123");
        assertNotNull(authenticated);
        assertEquals(user.getUserId(), authenticated.getUserId());
        assertNotNull(authenticated.getLastLoginAt());
        
        // Test failed authentication with wrong password
        User failedAuth = userManager.validateCredentials("testuser", "wrongpassword");
        assertNull(failedAuth);
        
        // Test failed authentication with wrong username
        User failedAuth2 = userManager.validateCredentials("wronguser", "password123");
        assertNull(failedAuth2);
    }
    
    @Test
    public void testUserRoleCreation() {
        // Test Admin creation
        User admin = userManager.createUser("admin", "admin@example.com", "password123", 
                                           "Admin", "User", UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(admin instanceof Admin);
        
        // Test Organizer creation
        User organizer = userManager.createUser("organizer", "organizer@example.com", "password123", 
                                               "Organizer", "User", UserRole.ORGANIZER);
        assertEquals(UserRole.ORGANIZER, organizer.getRole());
        assertTrue(organizer instanceof Organizer);
        
        // Test Attendee creation
        User attendee = userManager.createUser("attendee", "attendee@example.com", "password123", 
                                              "Attendee", "User", UserRole.ATTENDEE);
        assertEquals(UserRole.ATTENDEE, attendee.getRole());
        assertTrue(attendee instanceof Attendee);
    }
    
    @Test
    public void testGetUsersByRole() {
        // Create users with different roles
        userManager.createUser("admin1", "admin1@example.com", "password123", 
                              "Admin", "One", UserRole.ADMIN);
        userManager.createUser("admin2", "admin2@example.com", "password123", 
                              "Admin", "Two", UserRole.ADMIN);
        userManager.createUser("organizer1", "organizer1@example.com", "password123", 
                              "Organizer", "One", UserRole.ORGANIZER);
        userManager.createUser("attendee1", "attendee1@example.com", "password123", 
                              "Attendee", "One", UserRole.ATTENDEE);
        
        List<User> admins = userManager.getUsersByRole(UserRole.ADMIN);
        assertEquals(2, admins.size());
        
        List<User> organizers = userManager.getUsersByRole(UserRole.ORGANIZER);
        assertEquals(1, organizers.size());
        
        List<User> attendees = userManager.getUsersByRole(UserRole.ATTENDEE);
        assertEquals(1, attendees.size());
    }
    
    @Test
    public void testUserAvailabilityChecks() {
        // Initially both should be available
        assertTrue(userManager.isUsernameAvailable("testuser"));
        assertTrue(userManager.isEmailAvailable("test@example.com"));
        
        // Create a user
        userManager.createUser("testuser", "test@example.com", "password123", 
                              "John", "Doe", UserRole.ATTENDEE);
        
        // Now both should be unavailable
        assertFalse(userManager.isUsernameAvailable("testuser"));
        assertFalse(userManager.isEmailAvailable("test@example.com"));
        
        // Different username/email should still be available
        assertTrue(userManager.isUsernameAvailable("differentuser"));
        assertTrue(userManager.isEmailAvailable("different@example.com"));
    }
    
    @Test
    public void testUserDeletion() {
        // Create a user
        User user = userManager.createUser("testuser", "test@example.com", "password123", 
                                          "John", "Doe", UserRole.ATTENDEE);
        String userId = user.getUserId();
        
        // Verify user exists
        assertNotNull(userManager.getUserById(userId));
        assertFalse(userManager.isUsernameAvailable("testuser"));
        assertFalse(userManager.isEmailAvailable("test@example.com"));
        
        // Delete user
        boolean deleted = userManager.deleteUser(userId);
        assertTrue(deleted);
        
        // Verify user is gone
        assertNull(userManager.getUserById(userId));
        assertTrue(userManager.isUsernameAvailable("testuser"));
        assertTrue(userManager.isEmailAvailable("test@example.com"));
        
        // Try to delete non-existent user
        boolean deletedAgain = userManager.deleteUser(userId);
        assertFalse(deletedAgain);
    }
    
    @Test
    public void testPasswordHashing() {
        // Create a user
        User user = userManager.createUser("testuser", "test@example.com", "password123", 
                                          "John", "Doe", UserRole.ATTENDEE);
        
        // Password should be hashed (not stored in plain text)
        assertTrue(user.verifyPassword("password123"));
        
        // But authentication should still work
        User authenticated = userManager.validateCredentials("testuser", "password123");
        assertNotNull(authenticated);
    }
}