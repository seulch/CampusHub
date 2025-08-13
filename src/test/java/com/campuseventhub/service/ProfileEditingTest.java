// =============================================================================
// PROFILE EDITING TESTS
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * Comprehensive tests for profile editing functionality.
 * 
 * Tests cover:
 * - Profile information updates (first name, last name, email)
 * - Password changes with validation
 * - Input validation and error handling
 * - Integration with EventHub and UserManager
 * - Profile retrieval functionality
 * - Security validation for password changes
 */
class ProfileEditingTest {
    
    private EventHub eventHub;
    private Attendee testAttendee;
    private Organizer testOrganizer;
    private Admin testAdmin;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        
        // Create test users with unique names
        int random = (int) (Math.random() * 1000);
        String uniqueAttendeeUsername = "prof_" + random;
        String uniqueOrgUsername = "org_" + random;
        String uniqueAdminUsername = "admin_" + random;
        
        testAttendee = new Attendee(uniqueAttendeeUsername, "profile" + random + "@test.com", "password123", "John", "Doe");
        testOrganizer = new Organizer(uniqueOrgUsername, "org" + random + "@test.com", "password123", "Jane", "Smith", "Computer Science");
        testAdmin = new Admin(uniqueAdminUsername, "admin" + random + "@test.com", "password123", "Bob", "Wilson", "SYSTEM_ADMIN");
        
        // Register test users in the system
        eventHub.registerUser(uniqueAttendeeUsername, "profile" + random + "@test.com", "password123", "John", "Doe", com.campuseventhub.model.user.UserRole.ATTENDEE);
        eventHub.registerUser(uniqueOrgUsername, "org" + random + "@test.com", "password123", "Jane", "Smith", com.campuseventhub.model.user.UserRole.ORGANIZER);
        eventHub.registerUser(uniqueAdminUsername, "admin" + random + "@test.com", "password123", "Bob", "Wilson", com.campuseventhub.model.user.UserRole.ADMIN);
    }
    
    @Test
    @DisplayName("Test profile information update")
    void testUpdateProfile() {
        // Login as attendee
        eventHub.authenticateUser(testAttendee.getUsername(), "password123");
        
        // Update profile
        boolean result = eventHub.updateCurrentUserProfile("Johnny", "Smith", "newemail@test.com");
        
        assertTrue(result, "Profile update should succeed");
        
        // Verify profile was updated
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        assertEquals("Johnny", profile.get("firstName"));
        assertEquals("Smith", profile.get("lastName"));
        assertEquals("newemail@test.com", profile.get("email"));
    }
    
    @Test
    @DisplayName("Test partial profile update")
    void testPartialProfileUpdate() {
        // Login as organizer
        eventHub.authenticateUser(testOrganizer.getUsername(), "password123");
        
        // Update only first name
        boolean result = eventHub.updateCurrentUserProfile("Jennifer", null, null);
        
        assertTrue(result, "Partial profile update should succeed");
        
        // Verify only first name was updated
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        assertEquals("Jennifer", profile.get("firstName"));
        assertEquals("Smith", profile.get("lastName")); // Should remain unchanged
        assertEquals("org@test.com", profile.get("email")); // Should remain unchanged
    }
    
    @Test
    @DisplayName("Test profile update validation")
    void testProfileUpdateValidation() {
        // Login as admin
        eventHub.authenticateUser(testAdmin.getUsername(), "password123");
        
        // Try to update with invalid data
        boolean result1 = eventHub.updateCurrentUserProfile("", "ValidName", "valid@email.com");
        assertFalse(result1, "Update with empty first name should fail");
        
        boolean result2 = eventHub.updateCurrentUserProfile("Valid", "", "valid@email.com");
        assertFalse(result2, "Update with empty last name should fail");
        
        boolean result3 = eventHub.updateCurrentUserProfile("Valid", "Valid", "");
        assertFalse(result3, "Update with empty email should fail");
        
        boolean result4 = eventHub.updateCurrentUserProfile("Valid", "Valid", "invalid-email");
        assertFalse(result4, "Update with invalid email format should fail");
        
        boolean result5 = eventHub.updateCurrentUserProfile("Invalid@Name", "ValidName", "valid@email.com");
        assertFalse(result5, "Update with invalid name characters should fail");
    }
    
    @Test
    @DisplayName("Test password change")
    void testPasswordChange() {
        // Login as attendee
        eventHub.authenticateUser(testAttendee.getUsername(), "password123");
        
        // Change password
        boolean result = eventHub.changeCurrentUserPassword("password123", "newpassword123");
        
        assertTrue(result, "Password change should succeed");
        
        // Verify new password works
        eventHub.logoutCurrentUser();
        assertTrue(eventHub.authenticateUser("profiletest", "newpassword123") != null, 
                  "Should be able to login with new password");
        
        // Verify old password doesn't work
        eventHub.logoutCurrentUser();
        assertNull(eventHub.authenticateUser(testAttendee.getUsername(), "password123"), 
                  "Should not be able to login with old password");
    }
    
    @Test
    @DisplayName("Test password change validation")
    void testPasswordChangeValidation() {
        // Login as organizer
        eventHub.authenticateUser(testOrganizer.getUsername(), "password123");
        
        // Try to change with wrong current password
        boolean result1 = eventHub.changeCurrentUserPassword("wrongpassword", "newpassword123");
        assertFalse(result1, "Password change with wrong current password should fail");
        
        // Try to change to invalid new password
        boolean result2 = eventHub.changeCurrentUserPassword("password123", "123");
        assertFalse(result2, "Password change to short password should fail");
        
        boolean result3 = eventHub.changeCurrentUserPassword("password123", "password");
        assertFalse(result3, "Password change to password without numbers should fail");
        
        boolean result4 = eventHub.changeCurrentUserPassword("password123", "12345678");
        assertFalse(result4, "Password change to numbers-only password should fail");
    }
    
    @Test
    @DisplayName("Test profile retrieval")
    void testProfileRetrieval() {
        // Login as admin
        eventHub.authenticateUser(testAdmin.getUsername(), "password123");
        
        // Get profile
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        
        assertFalse(profile.isEmpty(), "Profile should not be empty");
        assertEquals(testAdmin.getUsername(), profile.get("username"));
        assertEquals("Bob", profile.get("firstName"));
        assertEquals("Wilson", profile.get("lastName"));
        assertEquals(testAdmin.getEmail(), profile.get("email"));
        assertEquals("ADMIN", profile.get("role"));
        assertEquals("ACTIVE", profile.get("status"));
        assertNotNull(profile.get("userId"));
    }
    
    @Test
    @DisplayName("Test profile operations without authentication")
    void testUnauthenticatedProfileOperations() {
        // Make sure no user is logged in
        eventHub.logoutCurrentUser();
        
        // Try profile operations without authentication
        boolean updateResult = eventHub.updateCurrentUserProfile("Test", "Test", "test@test.com");
        assertFalse(updateResult, "Profile update without authentication should fail");
        
        boolean passwordResult = eventHub.changeCurrentUserPassword("old", "new123");
        assertFalse(passwordResult, "Password change without authentication should fail");
        
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        assertTrue(profile.isEmpty(), "Profile retrieval without authentication should return empty map");
    }
    
    @Test
    @DisplayName("Test profile update with whitespace handling")
    void testWhitespaceHandling() {
        // Login as attendee
        eventHub.authenticateUser(testAttendee.getUsername(), "password123");
        
        // Update with whitespace
        boolean result = eventHub.updateCurrentUserProfile("  John  ", "  Smith  ", "  john@test.com  ");
        
        assertTrue(result, "Profile update with whitespace should succeed");
        
        // Verify whitespace was trimmed
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        assertEquals("John", profile.get("firstName"));
        assertEquals("Smith", profile.get("lastName"));
        assertEquals("john@test.com", profile.get("email"));
    }
    
    @Test
    @DisplayName("Test profile update with special characters")
    void testSpecialCharactersInNames() {
        // Login as organizer
        eventHub.authenticateUser(testOrganizer.getUsername(), "password123");
        
        // Try names with special characters (should be rejected)
        boolean result1 = eventHub.updateCurrentUserProfile("John123", "Smith", "valid@email.com");
        assertFalse(result1, "Names with numbers should be rejected");
        
        boolean result2 = eventHub.updateCurrentUserProfile("John@", "Smith", "valid@email.com");
        assertFalse(result2, "Names with special characters should be rejected");
        
        // Try valid names with hyphens and apostrophes (should be accepted)
        boolean result3 = eventHub.updateCurrentUserProfile("Mary-Jane", "O'Connor", "valid@email.com");
        assertTrue(result3, "Names with hyphens and apostrophes should be accepted");
        
        // Verify the valid names were updated
        Map<String, String> profile = eventHub.getCurrentUserProfile();
        assertEquals("Mary-Jane", profile.get("firstName"));
        assertEquals("O'Connor", profile.get("lastName"));
    }
    
    @Test
    @DisplayName("Test multiple password changes")
    void testMultiplePasswordChanges() {
        // Login as admin
        eventHub.authenticateUser(testAdmin.getUsername(), "password123");
        
        // First password change
        boolean result1 = eventHub.changeCurrentUserPassword("password123", "newpass123");
        assertTrue(result1, "First password change should succeed");
        
        // Second password change using the new password
        boolean result2 = eventHub.changeCurrentUserPassword("newpass123", "finalpass123");
        assertTrue(result2, "Second password change should succeed");
        
        // Verify final password works
        eventHub.logoutCurrentUser();
        assertNotNull(eventHub.authenticateUser("adminprofile", "finalpass123"), 
                     "Should be able to login with final password");
        
        // Verify intermediate password doesn't work
        eventHub.logoutCurrentUser();
        assertNull(eventHub.authenticateUser("adminprofile", "newpass123"), 
                  "Should not be able to login with intermediate password");
    }
}