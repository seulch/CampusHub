package com.campuseventhub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ValidationUtil
 */
public class ValidationUtilTest {
    
    @Test
    public void testIsValidEmail() {
        // Valid emails
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.org"));
        assertTrue(ValidationUtil.isValidEmail("test123@test.edu"));
        
        // Invalid emails
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail("   "));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@domain.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
    }
    
    @Test
    public void testIsValidPassword() {
        // Valid passwords
        assertTrue(ValidationUtil.isValidPassword("password123"));
        assertTrue(ValidationUtil.isValidPassword("TestPass1"));
        assertTrue(ValidationUtil.isValidPassword("a1234567"));
        
        // Invalid passwords
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword("short1"));
        assertFalse(ValidationUtil.isValidPassword("NoNumbers"));
        assertFalse(ValidationUtil.isValidPassword("12345678")); // only numbers
        assertFalse(ValidationUtil.isValidPassword("abcdefgh")); // only letters
    }
    
    @Test
    public void testIsValidUsername() {
        // Valid usernames
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("test_user"));
        assertTrue(ValidationUtil.isValidUsername("username"));
        
        // Invalid usernames
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername("ab")); // too short
        assertFalse(ValidationUtil.isValidUsername("a".repeat(21))); // too long
        assertFalse(ValidationUtil.isValidUsername("user-name")); // contains dash
        assertFalse(ValidationUtil.isValidUsername("user name")); // contains space
        assertFalse(ValidationUtil.isValidUsername("user@name")); // contains special char
    }
    
    @Test
    public void testIsValidEventTitle() {
        // Valid titles
        assertTrue(ValidationUtil.isValidEventTitle("test")); // Now valid (3 chars)
        assertTrue(ValidationUtil.isValidEventTitle("Java Workshop"));
        assertTrue(ValidationUtil.isValidEventTitle("Annual Tech Conference 2024"));
        assertTrue(ValidationUtil.isValidEventTitle("Introduction to Machine Learning"));
        
        // Invalid titles
        assertFalse(ValidationUtil.isValidEventTitle(null));
        assertFalse(ValidationUtil.isValidEventTitle(""));
        assertFalse(ValidationUtil.isValidEventTitle("   "));
        assertFalse(ValidationUtil.isValidEventTitle("Te")); // too short (less than 3)
        assertFalse(ValidationUtil.isValidEventTitle("a".repeat(101))); // too long
    }
    
    @Test
    public void testIsValidName() {
        // Valid names
        assertTrue(ValidationUtil.isValidName("John"));
        assertTrue(ValidationUtil.isValidName("Mary Jane"));
        assertTrue(ValidationUtil.isValidName("O Connor"));
        
        // Invalid names
        assertFalse(ValidationUtil.isValidName(null));
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName("   "));
        assertFalse(ValidationUtil.isValidName("a".repeat(51))); // too long
        assertFalse(ValidationUtil.isValidName("John123")); // contains numbers
        assertFalse(ValidationUtil.isValidName("John-Doe")); // contains dash
        assertFalse(ValidationUtil.isValidName("John@Doe")); // contains special char
    }
    
    @Test
    public void testIsValidCapacity() {
        // Valid capacities
        assertTrue(ValidationUtil.isValidCapacity(1));
        assertTrue(ValidationUtil.isValidCapacity(50));
        assertTrue(ValidationUtil.isValidCapacity(1000));
        assertTrue(ValidationUtil.isValidCapacity(10000));
        
        // Invalid capacities
        assertFalse(ValidationUtil.isValidCapacity(0));
        assertFalse(ValidationUtil.isValidCapacity(-1));
        assertFalse(ValidationUtil.isValidCapacity(10001));
    }
}