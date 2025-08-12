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
    
    // @Test
    // @DisplayName("Should create user with correct initial values")
    // void testUserCreation() {
    //     assertNotNull(user.getUserId());
    //     assertEquals("testuser", user.getUsername());
    //     assertEquals("test@example.com", user.getEmail());
    //     assertEquals("password123", user.getPassword());
    //     assertEquals("Test", user.getFirstName());
    //     assertEquals("User", user.getLastName());
    //     assertEquals(UserStatus.ACTIVE, user.getStatus());
    //     assertEquals(UserRole.ATTENDEE, user.getRole());
    //     assertNotNull(user.getCreatedAt());
    //     assertNull(user.getLastLoginAt());
    //     assertTrue(user.isActive());
    // }
    
    @Test
    @DisplayName("Should create user with correct initial values and hashed password")
    void testUserCreationWithHashedPassword() {
        assertNotNull(user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertNotEquals("password123", user.getPassword()); // Password should be hashed
        assertTrue(user.getPassword().length() > 20); // Hashed password is longer
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
    
    // @Test
    // @DisplayName("Should implement equals correctly")
    // void testEquals() {
    //     TestUser sameUser = new TestUser("different", "different@example.com", "different", "Different", "User");
    //     sameUser.setStatus(UserStatus.SUSPENDED); // Different status
    //     
    //     // Override the userId to be the same for testing equals
    //     String originalUserId = user.getUserId();
    //     TestUser identicalUser = new TestUser("testuser", "test@example.com", "password123", "Test", "User");
    //     // We need reflection to set the userId since it's generated automatically
    //     try {
    //         java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
    //         userIdField.setAccessible(true);
    //         userIdField.set(identicalUser, originalUserId);
    //     } catch (Exception e) {
    //         fail("Failed to set userId for testing: " + e.getMessage());
    //     }
    //     
    //     // Test equals
    //     assertTrue(user.equals(user)); // Same object
    //     assertTrue(user.equals(identicalUser)); // Same userId
    //     assertFalse(user.equals(null)); // Null object
    //     assertFalse(user.equals("string")); // Different class
    //     assertFalse(user.equals(sameUser)); // Different userId
    // }
    
    @Test
    @DisplayName("Should implement equals correctly with enhanced validation")
    void testEqualsWithValidPasswords() {
        TestUser sameUser = new TestUser("differentuser", "different@example.com", "strongpass123", "Different", "User");
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
    
    // ========== NEW TESTS (FOR ENHANCED USER FEATURES)! ==========
    
    @Test
    @DisplayName("Should validate input during user creation")
    void testUserCreationValidation() {
        // Test invalid username
        assertThrows(IllegalArgumentException.class, () -> {
            new TestUser("ab", "test@example.com", "password123", "Test", "User");
        });
        
        // Test invalid email
        assertThrows(IllegalArgumentException.class, () -> {
            new TestUser("testuser", "invalid-email", "password123", "Test", "User");
        });
        
        // Test invalid password
        assertThrows(IllegalArgumentException.class, () -> {
            new TestUser("testuser", "test@example.com", "weak", "Test", "User");
        });
        
        // Test invalid first name
        assertThrows(IllegalArgumentException.class, () -> {
            new TestUser("testuser", "test@example.com", "password123", "Test123", "User");
        });
        
        // Test invalid last name
        assertThrows(IllegalArgumentException.class, () -> {
            new TestUser("testuser", "test@example.com", "password123", "Test", "");
        });
    }
    
    @Test
    @DisplayName("Should change password with validation")
    void testPasswordChange() {
        String originalPasswordHash = user.getPassword();
        
        // Change to valid password
        user.changePassword("newstrongpass456");
        assertNotEquals(originalPasswordHash, user.getPassword());
        
        // Old password should no longer work
        assertFalse(user.login("testuser", "password123"));
        
        // New password should work
        assertTrue(user.login("testuser", "newstrongpass456"));
        
        // Test invalid password change
        assertThrows(IllegalArgumentException.class, () -> {
            user.changePassword("weak");
        });
    }
    
    @Test
    @DisplayName("Should handle enhanced profile validation")
    void testEnhancedProfileUpdate() {
        String originalFirstName = user.getFirstName();
        String originalLastName = user.getLastName();
        String originalEmail = user.getEmail();
        
        // Valid updates should work
        user.updateProfile("NewFirst", "NewLast", "newemail@example.com");
        assertEquals("NewFirst", user.getFirstName());
        assertEquals("NewLast", user.getLastName());
        assertEquals("newemail@example.com", user.getEmail());
        
        // Invalid updates should be ignored
        user.updateProfile("Invalid123", "Invalid@Name", "invalid-email");
        assertEquals("NewFirst", user.getFirstName()); // Should remain unchanged
        assertEquals("NewLast", user.getLastName()); // Should remain unchanged
        assertEquals("newemail@example.com", user.getEmail()); // Should remain unchanged
    }
    
    @Test
    @DisplayName("Should normalize email to lowercase")
    void testEmailNormalization() {
        TestUser upperCaseEmailUser = new TestUser("testuser2", "TEST@EXAMPLE.COM", "password123", "Test", "User");
        assertEquals("test@example.com", upperCaseEmailUser.getEmail());
    }
    
    @Test
    @DisplayName("Should trim whitespace from inputs")
    void testInputTrimming() {
        TestUser trimmedUser = new TestUser("  testuser  ", "  test@example.com  ", "password123", "  Test  ", "  User  ");
        assertEquals("testuser", trimmedUser.getUsername());
        assertEquals("test@example.com", trimmedUser.getEmail());
        assertEquals("Test", trimmedUser.getFirstName());
        assertEquals("User", trimmedUser.getLastName());
    }
    
    @Test
    @DisplayName("Should verify password hashing security")
    void testPasswordHashingSecurity() {
        TestUser user1 = new TestUser("user1", "user1@example.com", "password123", "User", "One");
        TestUser user2 = new TestUser("user2", "user2@example.com", "password123", "User", "Two");
        
        // Same password should produce different hashes due to salt including username
        assertNotEquals(user1.getPassword(), user2.getPassword());
        
        // Both should authenticate with same password
        assertTrue(user1.login("user1", "password123"));
        assertTrue(user2.login("user2", "password123"));
        
        // Cross-authentication should fail
        assertFalse(user1.login("user2", "password123"));
        assertFalse(user2.login("user1", "password123"));
    }
}


