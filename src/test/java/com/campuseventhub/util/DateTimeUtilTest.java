package com.campuseventhub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

/**
 * Test class for DateTimeUtil
 */
public class DateTimeUtilTest {
    
    @Test
    public void testIsValidEventDateTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(1);
        LocalDateTime past = now.minusDays(1);
        LocalDateTime farFuture = now.plusYears(3);
        
        // Valid date/time
        assertTrue(DateTimeUtil.isValidEventDateTime(future));
        assertTrue(DateTimeUtil.isValidEventDateTime(now.plusHours(1)));
        
        // Invalid date/time
        assertFalse(DateTimeUtil.isValidEventDateTime(null));
        assertFalse(DateTimeUtil.isValidEventDateTime(past));
        assertFalse(DateTimeUtil.isValidEventDateTime(farFuture));
    }
    
    @Test
    public void testHasTimeConflict() {
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime start2 = LocalDateTime.of(2024, 1, 1, 11, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 1, 1, 13, 0);
        LocalDateTime start3 = LocalDateTime.of(2024, 1, 1, 13, 0);
        LocalDateTime end3 = LocalDateTime.of(2024, 1, 1, 15, 0);
        
        // Overlapping times
        assertTrue(DateTimeUtil.hasTimeConflict(start1, end1, start2, end2));
        assertTrue(DateTimeUtil.hasTimeConflict(start2, end2, start1, end1));
        
        // Non-overlapping times
        assertFalse(DateTimeUtil.hasTimeConflict(start1, end1, start3, end3));
        
        // Null parameters
        assertFalse(DateTimeUtil.hasTimeConflict(null, end1, start2, end2));
        assertFalse(DateTimeUtil.hasTimeConflict(start1, null, start2, end2));
        assertFalse(DateTimeUtil.hasTimeConflict(start1, end1, null, end2));
        assertFalse(DateTimeUtil.hasTimeConflict(start1, end1, start2, null));
    }
    
    @Test
    public void testFormatForDisplay() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        
        assertEquals("2024-01-15 14:30", DateTimeUtil.formatForDisplay(dateTime));
        assertEquals("", DateTimeUtil.formatForDisplay(null));
    }
    
    @Test
    public void testParseFromString() {
        String validString = "2024-01-15 14:30";
        String invalidString = "invalid-date";
        
        LocalDateTime expected = LocalDateTime.of(2024, 1, 15, 14, 30);
        
        assertEquals(expected, DateTimeUtil.parseFromString(validString));
        assertNull(DateTimeUtil.parseFromString(invalidString));
        assertNull(DateTimeUtil.parseFromString(null));
        assertNull(DateTimeUtil.parseFromString(""));
        assertNull(DateTimeUtil.parseFromString("   "));
    }
    
    @Test
    public void testIsValidTimeRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime invalidEnd = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        // Valid range
        assertTrue(DateTimeUtil.isValidTimeRange(start, end));
        
        // Invalid range
        assertFalse(DateTimeUtil.isValidTimeRange(start, invalidEnd));
        assertFalse(DateTimeUtil.isValidTimeRange(start, start)); // same time
        
        // Null parameters
        assertFalse(DateTimeUtil.isValidTimeRange(null, end));
        assertFalse(DateTimeUtil.isValidTimeRange(start, null));
        assertFalse(DateTimeUtil.isValidTimeRange(null, null));
    }
    
    @Test
    public void testGetDurationHours() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime endLonger = LocalDateTime.of(2024, 1, 1, 13, 30);
        
        assertEquals(2, DateTimeUtil.getDurationHours(start, end));
        assertEquals(3, DateTimeUtil.getDurationHours(start, endLonger));
        
        // Null parameters
        assertEquals(0, DateTimeUtil.getDurationHours(null, end));
        assertEquals(0, DateTimeUtil.getDurationHours(start, null));
        assertEquals(0, DateTimeUtil.getDurationHours(null, null));
    }
}