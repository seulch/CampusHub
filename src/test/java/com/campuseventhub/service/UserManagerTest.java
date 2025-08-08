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
    
    @Test
    @DisplayName("Should return null for invalid user creation")
    void testCreateUserWithInvalidData() {
        User user = userManager.createUser("", "email@test.com", "password123", 
                                         "First", "Last", UserRole.ATTENDEE);
        assertNull(user);
        
        user = userManager.createUser("username", "", "password123", 
                                    "First", "Last", UserRole.ATTENDEE);
        assertNull(user);
        
        user = userManager.createUser("username", "email@test.com", "", 
                                    "First", "Last", UserRole.ATTENDEE);
        assertNull(user);
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
    
    @Test
    @DisplayName("Should return null for invalid credentials")
    void testValidateInvalidCredentials() {
        userManager.createUser("testuser", "test@test.com", "password123", 
                             "Test", "User", UserRole.ATTENDEE);
        
        User authenticatedUser = userManager.validateCredentials("testuser", "wrongpassword");
        assertNull(authenticatedUser);
        
        authenticatedUser = userManager.validateCredentials("nonexistent", "password123");
        assertNull(authenticatedUser);
    }
    
    @Test
    @DisplayName("Should update user information successfully")
    void testUpdateUser() {
        User user = userManager.createUser("testuser", "test@test.com", "password123", 
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
    
    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        userManager.createUser("user1", "user1@test.com", "password123", 
                             "User", "One", UserRole.ATTENDEE);
        userManager.createUser("user2", "user2@test.com", "password123", 
                             "User", "Two", UserRole.ORGANIZER);
        
        List<User> allUsers = userManager.getAllUsers();
        assertEquals(2, allUsers.size());
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
} 