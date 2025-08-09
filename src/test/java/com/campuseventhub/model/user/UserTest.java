package com.campuseventhub.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class UserTest {
    
    private TestUser user;
    
    // Concrete implementation of abstract User class for testing
    private static class TestUser extends User {
        public TestUser(String username, String email, String password, String firstName, String lastName) {
            super(username, email, password, firstName, lastName);
        }
        
        @Override
        public UserRole getRole() {
            return UserRole.ATTENDEE;
        }
    }
    
    @BeforeEach
    void setUp() {
        user = new TestUser("testuser", "test@example.com", "password123", "Test", "User");
    }
    
    @Test
    @DisplayName("Should create user with correct initial values")
    void testUserCreation() {
        assertNotNull(user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(UserRole.ATTENDEE, user.getRole());
        assertNotNull(user.getCreatedAt());
        assertNull(user.getLastLoginAt());
        assertTrue(user.isActive());
    }
    
    @Test
    @DisplayName("Should login successfully with correct credentials")
    void testSuccessfulLogin() {
        boolean result = user.login("testuser", "password123");
        
        assertTrue(result);
        assertNotNull(user.getLastLoginAt());
    }
    
    @Test
    @DisplayName("Should fail login with incorrect username")
    void testLoginWithIncorrectUsername() {
        boolean result = user.login("wronguser", "password123");
        
        assertFalse(result);
        assertNull(user.getLastLoginAt());
    }
    
    @Test
    @DisplayName("Should fail login with incorrect password")
    void testLoginWithIncorrectPassword() {
        boolean result = user.login("testuser", "wrongpassword");
        
        assertFalse(result);
        assertNull(user.getLastLoginAt());
    }
    
    @Test
    @DisplayName("Should fail login when user status is not active")
    void testLoginWithInactiveStatus() {
        user.setStatus(UserStatus.SUSPENDED);
        
        boolean result = user.login("testuser", "password123");
        
        assertFalse(result);
        assertNull(user.getLastLoginAt());
    }
    
    @Test
    @DisplayName("Should update profile information successfully")
    void testUpdateProfile() {
        user.updateProfile("UpdatedFirst", "UpdatedLast", "updated@example.com");
        
        assertEquals("UpdatedFirst", user.getFirstName());
        assertEquals("UpdatedLast", user.getLastName());
        assertEquals("updated@example.com", user.getEmail());
    }
    
    @Test
    @DisplayName("Should not update profile with null or empty values")
    void testUpdateProfileWithInvalidValues() {
        String originalFirstName = user.getFirstName();
        String originalLastName = user.getLastName();
        String originalEmail = user.getEmail();
        
        user.updateProfile(null, "", "   ");
        
        assertEquals(originalFirstName, user.getFirstName());
        assertEquals(originalLastName, user.getLastName());
        assertEquals(originalEmail, user.getEmail());
    }
    
    @Test
    @DisplayName("Should handle partial profile updates")
    void testPartialProfileUpdate() {
        String originalLastName = user.getLastName();
        String originalEmail = user.getEmail();
        
        user.updateProfile("NewFirst", null, null);
        
        assertEquals("NewFirst", user.getFirstName());
        assertEquals(originalLastName, user.getLastName());
        assertEquals(originalEmail, user.getEmail());
    }
    
    @Test
    @DisplayName("Should check active status correctly")
    void testIsActive() {
        assertTrue(user.isActive());
        
        user.setStatus(UserStatus.SUSPENDED);
        assertFalse(user.isActive());
        
        user.setStatus(UserStatus.PENDING_APPROVAL);
        assertFalse(user.isActive());
        
        user.setStatus(UserStatus.ACTIVE);
        assertTrue(user.isActive());
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void testEquals() {
        TestUser sameUser = new TestUser("different", "different@example.com", "different", "Different", "User");
        sameUser.setStatus(UserStatus.SUSPENDED); // Different status
        
        // Override the userId to be the same for testing equals
        String originalUserId = user.getUserId();
        TestUser identicalUser = new TestUser("testuser", "test@example.com", "password123", "Test", "User");
        // We need reflection to set the userId since it's generated automatically
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(identicalUser, originalUserId);
        } catch (Exception e) {
            fail("Failed to set userId for testing: " + e.getMessage());
        }
        
        // Test equals
        assertTrue(user.equals(user)); // Same object
        assertTrue(user.equals(identicalUser)); // Same userId
        assertFalse(user.equals(null)); // Null object
        assertFalse(user.equals("string")); // Different class
        assertFalse(user.equals(sameUser)); // Different userId
    }
    
    @Test
    @DisplayName("Should implement hashCode correctly")
    void testHashCode() {
        TestUser sameUser = new TestUser("testuser", "test@example.com", "password123", "Test", "User");
        
        assertEquals(user.hashCode(), user.hashCode()); // Consistent
        assertNotEquals(user.hashCode(), sameUser.hashCode()); // Different userId means different hashCode
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void testToString() {
        String toString = user.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("User{"));
        assertTrue(toString.contains("userId="));
        assertTrue(toString.contains("username='testuser'"));
        assertTrue(toString.contains("email='test@example.com'"));
        assertTrue(toString.contains("firstName='Test'"));
        assertTrue(toString.contains("lastName='User'"));
        assertTrue(toString.contains("role='ATTENDEE'"));
        assertTrue(toString.contains("status='ACTIVE'"));
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        
        user.setFirstName("NewFirst");
        user.setLastName("NewLast");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setStatus(UserStatus.SUSPENDED);
        user.setLastLoginAt(now);
        
        assertEquals("NewFirst", user.getFirstName());
        assertEquals("NewLast", user.getLastName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        assertEquals(now, user.getLastLoginAt());
    }
    
    @Test
    @DisplayName("Should handle logout process")
    void testLogout() {
        // Login first
        user.login("testuser", "password123");
        assertNotNull(user.getLastLoginAt());
        
        // Logout (currently simple implementation)
        user.logout();
        
        // Since logout is simple for now, just verify it doesn't throw exception
        assertDoesNotThrow(() -> user.logout());
    }
}

