package com.campuseventhub.model.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for User model and subclasses
 */
public class UserModelTest {
    
    @Test
    public void testAttendeeCreationAndValidation() {
        // Valid attendee creation
        Attendee attendee = new Attendee("testuser", "test@example.com", "password123", 
                                       "John", "Doe");
        
        assertNotNull(attendee.getUserId());
        assertEquals("testuser", attendee.getUsername());
        assertEquals("test@example.com", attendee.getEmail());
        assertEquals("John", attendee.getFirstName());
        assertEquals("Doe", attendee.getLastName());
        assertEquals(UserRole.ATTENDEE, attendee.getRole());
        assertEquals(UserStatus.ACTIVE, attendee.getStatus());
        assertNotNull(attendee.getCreatedAt());
        
        // Password should be hashed
        assertNotEquals("password123", attendee.getPassword());
    }
    
    @Test
    public void testOrganizerCreation() {
        Organizer organizer = new Organizer("orguser", "org@example.com", "password123", 
                                           "Jane", "Smith", "Computer Science");
        
        assertEquals(UserRole.ORGANIZER, organizer.getRole());
        assertEquals(UserStatus.ACTIVE, organizer.getStatus());
    }
    
    @Test
    public void testAdminCreation() {
        Admin admin = new Admin("adminuser", "admin@example.com", "password123", 
                               "Admin", "User", "SYSTEM_ADMIN");
        
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals(UserStatus.ACTIVE, admin.getStatus());
    }
    
    @Test
    public void testUserValidationFailures() {
        // Invalid username (too short)
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("ab", "test@example.com", "password123", "John", "Doe");
        });
        
        // Invalid email
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("testuser", "invalid-email", "password123", "John", "Doe");
        });
        
        // Invalid password (too short)
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("testuser", "test@example.com", "weak", "John", "Doe");
        });
        
        // Invalid password (no letters)
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("testuser", "test@example.com", "12345678", "John", "Doe");
        });
        
        // Invalid first name (contains numbers)
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("testuser", "test@example.com", "password123", "John123", "Doe");
        });
        
        // Invalid last name (empty)
        assertThrows(IllegalArgumentException.class, () -> {
            new Attendee("testuser", "test@example.com", "password123", "John", "");
        });
    }
    
    @Test
    public void testUserAuthentication() {
        Attendee user = new Attendee("testuser", "test@example.com", "password123", 
                                   "John", "Doe");
        
        // Successful authentication
        assertTrue(user.login("testuser", "password123"));
        assertNotNull(user.getLastLoginAt());
        
        // Failed authentication - wrong password
        assertFalse(user.login("testuser", "wrongpassword"));
        
        // Failed authentication - wrong username
        assertFalse(user.login("wronguser", "password123"));
        
        // Failed authentication - null password
        assertFalse(user.login("testuser", null));
    }
    
    @Test
    public void testPasswordChange() {
        Attendee user = new Attendee("testuser", "test@example.com", "password123", 
                                   "John", "Doe");
        
        String originalPassword = user.getPassword();
        
        // Change password
        user.changePassword("newpassword456");
        
        // Password hash should be different
        assertNotEquals(originalPassword, user.getPassword());
        
        // Old password should no longer work
        assertFalse(user.login("testuser", "password123"));
        
        // New password should work
        assertTrue(user.login("testuser", "newpassword456"));
        
        // Test invalid new password
        assertThrows(IllegalArgumentException.class, () -> {
            user.changePassword("weak");
        });
    }
    
    @Test
    public void testProfileUpdate() {
        Attendee user = new Attendee("testuser", "test@example.com", "password123", 
                                   "John", "Doe");
        
        // Valid profile updates
        user.updateProfile("Johnny", "Smith", "newemail@example.com");
        
        assertEquals("Johnny", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("newemail@example.com", user.getEmail());
        
        // Invalid updates should be ignored
        String originalEmail = user.getEmail();
        user.updateProfile("ValidName", "ValidLastName", "invalid-email");
        
        assertEquals("ValidName", user.getFirstName());
        assertEquals("ValidLastName", user.getLastName());
        assertEquals(originalEmail, user.getEmail()); // Should remain unchanged
        
        // Null updates should be ignored
        user.updateProfile(null, null, null);
        assertEquals("ValidName", user.getFirstName());
        assertEquals("ValidLastName", user.getLastName());
        assertEquals(originalEmail, user.getEmail());
    }
    
    @Test
    public void testUserStatus() {
        Attendee user = new Attendee("testuser", "test@example.com", "password123", 
                                   "John", "Doe");
        
        // Initially active
        assertTrue(user.isActive());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        
        // Can login when active
        assertTrue(user.login("testuser", "password123"));
        
        // Suspend user
        user.setStatus(UserStatus.SUSPENDED);
        assertFalse(user.isActive());
        
        // Cannot login when suspended
        assertFalse(user.login("testuser", "password123"));
    }
    
    @Test
    public void testEmailNormalization() {
        // Email should be normalized to lowercase
        Attendee user = new Attendee("testuser", "TEST@EXAMPLE.COM", "password123", 
                                   "John", "Doe");
        
        assertEquals("test@example.com", user.getEmail());
    }
    
    @Test
    public void testUserEquality() {
        Attendee user1 = new Attendee("testuser1", "test1@example.com", "password123", 
                                    "John", "Doe");
        Attendee user2 = new Attendee("testuser2", "test2@example.com", "password123", 
                                    "Jane", "Smith");
        
        // Different users should not be equal
        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());
        
        // User should be equal to itself
        assertEquals(user1, user1);
        assertEquals(user1.hashCode(), user1.hashCode());
        
        // Null should not be equal
        assertNotEquals(user1, null);
    }
}