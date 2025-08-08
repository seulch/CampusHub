package com.campuseventhub.service;

import com.campuseventhub.model.user.User;
import com.campuseventhub.model.user.UserRole;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Attendee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserManager Tests")
class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    @DisplayName("Should create admin user successfully")
    void testCreateAdminUser() {
        User admin = userManager.createUser("admin", "admin@campus.edu", "password123", 
                                         "Admin", "User", UserRole.ADMIN);
        
        assertNotNull(admin);
        assertTrue(admin instanceof Admin);
        assertEquals("admin", admin.getUsername());
        assertEquals("admin@campus.edu", admin.getEmail());
        assertEquals(UserRole.ADMIN, admin.getRole());
    }

    @Test
    @DisplayName("Should create organizer user successfully")
    void testCreateOrganizerUser() {
        User organizer = userManager.createUser("organizer", "organizer@campus.edu", "password123", 
                                             "Event", "Organizer", UserRole.ORGANIZER);
        
        assertNotNull(organizer);
        assertTrue(organizer instanceof Organizer);
        assertEquals("organizer", organizer.getUsername());
        assertEquals(UserRole.ORGANIZER, organizer.getRole());
    }

    @Test
    @DisplayName("Should create attendee user successfully")
    void testCreateAttendeeUser() {
        User attendee = userManager.createUser("attendee", "attendee@campus.edu", "password123", 
                                            "Event", "Attendee", UserRole.ATTENDEE);
        
        assertNotNull(attendee);
        assertTrue(attendee instanceof Attendee);
        assertEquals("attendee", attendee.getUsername());
        assertEquals(UserRole.ATTENDEE, attendee.getRole());
    }

    @Test
    @DisplayName("Should validate credentials successfully")
    void testValidateCredentials() {
        // Create a user first
        userManager.createUser("testuser", "test@campus.edu", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        // Test valid credentials
        User authenticatedUser = userManager.validateCredentials("testuser", "password123");
        assertNotNull(authenticatedUser);
        assertEquals("testuser", authenticatedUser.getUsername());
    }

    @Test
    @DisplayName("Should return null for invalid credentials")
    void testInvalidCredentials() {
        // Create a user first
        userManager.createUser("testuser", "test@campus.edu", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        // Test invalid credentials
        User authenticatedUser = userManager.validateCredentials("testuser", "wrongpassword");
        assertNull(authenticatedUser);
    }

    @Test
    @DisplayName("Should return null for non-existent user")
    void testNonExistentUser() {
        User authenticatedUser = userManager.validateCredentials("nonexistent", "password123");
        assertNull(authenticatedUser);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById() {
        User createdUser = userManager.createUser("testuser", "test@campus.edu", "password123", 
                                               "Test", "User", UserRole.ATTENDEE);
        
        User retrievedUser = userManager.getUserById(createdUser.getUserId());
        assertNotNull(retrievedUser);
        assertEquals(createdUser.getUserId(), retrievedUser.getUserId());
    }

    @Test
    @DisplayName("Should return null for non-existent user ID")
    void testGetUserByNonExistentId() {
        User retrievedUser = userManager.getUserById("nonexistent");
        assertNull(retrievedUser);
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        User user = userManager.createUser("testuser", "test@campus.edu", "password123", 
                                        "Test", "User", UserRole.ATTENDEE);
        
        Map<String, Object> updates = Map.of("firstName", "Updated", "lastName", "Name");
        boolean result = userManager.updateUser(user.getUserId(), updates);
        
        assertTrue(result);
        
        User updatedUser = userManager.getUserById(user.getUserId());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }

    @Test
    @DisplayName("Should return all users")
    void testGetAllUsers() {
        userManager.createUser("user1", "user1@campus.edu", "password123", 
                             "User", "One", UserRole.ATTENDEE);
        userManager.createUser("user2", "user2@campus.edu", "password123", 
                             "User", "Two", UserRole.ORGANIZER);
        
        List<User> allUsers = userManager.getAllUsers();
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
    }

    @Test
    @DisplayName("Should prevent duplicate usernames")
    void testDuplicateUsername() {
        userManager.createUser("testuser", "test1@campus.edu", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        // Try to create another user with same username
        User duplicateUser = userManager.createUser("testuser", "test2@campus.edu", "password123", 
                                                 "Test", "User2", UserRole.ATTENDEE);
        
        // Should return null or handle duplicate gracefully
        assertNull(duplicateUser);
    }

    @Test
    @DisplayName("Should prevent duplicate emails")
    void testDuplicateEmail() {
        userManager.createUser("user1", "test@campus.edu", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        // Try to create another user with same email
        User duplicateUser = userManager.createUser("user2", "test@campus.edu", "password123", 
                                                 "Test", "User2", UserRole.ATTENDEE);
        
        // Should return null or handle duplicate gracefully
        assertNull(duplicateUser);
    }
} 