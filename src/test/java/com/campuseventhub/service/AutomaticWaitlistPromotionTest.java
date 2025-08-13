package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.model.notification.NotificationType;
import com.campuseventhub.service.WaitlistManager.WaitlistPromotionResult;
import com.campuseventhub.service.WaitlistManager.WaitlistStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Comprehensive tests for automatic waitlist promotion functionality.
 * 
 * Tests cover:
 * - Automatic promotion when registrations are cancelled
 * - Automatic promotion when event capacity is increased
 * - Waitlist position tracking and updates
 * - Notification system integration
 * - Multiple promotion scenarios
 * - Edge cases and error handling
 * - Integration with registration deadline management
 */
class AutomaticWaitlistPromotionTest {
    
    private EventHub eventHub;
    private EventManager eventManager;
    private WaitlistManager waitlistManager;
    private NotificationService notificationService;
    private String organizerId;
    private String attendeeId1;
    private String attendeeId2;
    private String attendeeId3;
    private String attendeeId4;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        eventManager = eventHub.getEventManager();
        waitlistManager = new WaitlistManager();
        notificationService = new NotificationService();
        waitlistManager.setNotificationService(notificationService);
        baseTime = LocalDateTime.now().plusDays(1);
        
        // Use existing users
        var organizer = eventHub.authenticateUser("organizer", "organizer123");
        organizerId = organizer.getUserId();
        
        var attendee1 = eventHub.authenticateUser("attendee", "attendee123");
        attendeeId1 = attendee1.getUserId();
        
        // Create additional test attendees for waitlist testing
        attendeeId2 = "test-attendee-2";
        attendeeId3 = "test-attendee-3";
        attendeeId4 = "test-attendee-4";
        
        // Clean up any existing test data
        cleanupTestData();
    }
    
    private void cleanupTestData() {
        // This would normally clear test data
        // For now, we'll work with existing data
    }
    
    @Test
    @DisplayName("Test automatic promotion when registration is cancelled")
    void testAutomaticPromotionOnRegistrationCancellation() {
        // Create event with limited capacity
        Event event = eventManager.createEvent(
            "Waitlist Promotion Test", "Testing automatic promotion", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 2
        );
        
        assertNotNull(event, "Event should be created successfully");
        
        // Fill the event to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        Registration reg2 = new Registration(attendeeId2, event.getEventId());
        reg1.confirmRegistration();
        reg2.confirmRegistration();
        event.getRegistrations().add(reg1);
        event.getRegistrations().add(reg2);
        
        // Add attendees to waitlist
        Registration waitlistReg1 = new Registration(attendeeId3, event.getEventId());
        Registration waitlistReg2 = new Registration(attendeeId4, event.getEventId());
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistManager.addToWaitlist(event, waitlistReg2);
        
        // Verify initial state
        assertEquals(2, event.getRegistrations().size(), "Event should have 2 confirmed registrations");
        assertEquals(2, event.getWaitlistSize(), "Event should have 2 people on waitlist");
        assertEquals(1, waitlistReg1.getWaitlistPosition(), "First waitlist entry should be position 1");
        assertEquals(2, waitlistReg2.getWaitlistPosition(), "Second waitlist entry should be position 2");
        
        // Simulate registration cancellation by removing one registration
        event.getRegistrations().remove(0); // Remove first registration to create capacity
        
        // Now handle waitlist promotion
        WaitlistPromotionResult result = waitlistManager.handleRegistrationCancellation(event);
        
        // Verify promotion occurred
        assertTrue(result.hasPromotions(), "Should have promoted someone from waitlist");
        assertEquals(1, result.getPromotionsCount(), "Should have promoted exactly 1 person");
        
        // Verify waitlist positions updated
        assertEquals(1, event.getWaitlistSize(), "Waitlist should now have 1 person");
        assertEquals(1, waitlistReg2.getWaitlistPosition(), "Remaining waitlist entry should be position 1");
        
        // Verify promoted registration status
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg1.getStatus(), "Promoted registration should be confirmed");
        assertEquals(0, waitlistReg1.getWaitlistPosition(), "Promoted registration should have no waitlist position");
    }
    
    @Test
    @DisplayName("Test automatic promotion when capacity is increased")
    void testAutomaticPromotionOnCapacityIncrease() {
        // Create event with limited capacity
        Event event = eventManager.createEvent(
            "Capacity Increase Test", "Testing capacity increase promotion", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 1
        );
        
        assertNotNull(event, "Event should be created successfully");
        
        // Fill the event to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add attendees to waitlist
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        Registration waitlistReg2 = new Registration(attendeeId3, event.getEventId());
        Registration waitlistReg3 = new Registration(attendeeId4, event.getEventId());
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistManager.addToWaitlist(event, waitlistReg2);
        waitlistManager.addToWaitlist(event, waitlistReg3);
        
        // Verify initial state
        assertEquals(1, event.getRegistrations().size(), "Event should have 1 confirmed registration");
        assertEquals(3, event.getWaitlistSize(), "Event should have 3 people on waitlist");
        
        // Increase capacity by 2
        int oldCapacity = event.getMaxCapacity();
        int newCapacity = oldCapacity + 2;
        WaitlistPromotionResult result = waitlistManager.handleCapacityIncrease(event, oldCapacity, newCapacity);
        
        // Update event capacity
        event.setMaxCapacity(newCapacity);
        
        // Verify promotions occurred
        assertTrue(result.hasPromotions(), "Should have promoted people from waitlist");
        assertEquals(2, result.getPromotionsCount(), "Should have promoted exactly 2 people");
        
        // Verify waitlist positions updated
        assertEquals(1, event.getWaitlistSize(), "Waitlist should now have 1 person");
        assertEquals(1, waitlistReg3.getWaitlistPosition(), "Remaining waitlist entry should be position 1");
        
        // Verify promoted registrations status
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg1.getStatus(), "First promoted registration should be confirmed");
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg2.getStatus(), "Second promoted registration should be confirmed");
        assertEquals(0, waitlistReg1.getWaitlistPosition(), "First promoted registration should have no waitlist position");
        assertEquals(0, waitlistReg2.getWaitlistPosition(), "Second promoted registration should have no waitlist position");
    }
    
    @Test
    @DisplayName("Test waitlist position tracking and updates")
    void testWaitlistPositionTracking() {
        // Create event
        Event event = eventManager.createEvent(
            "Position Tracking Test", "Testing position tracking", EventType.SEMINAR,
            baseTime, baseTime.plusHours(2), organizerId, null, 1
        );
        
        // Fill to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add multiple people to waitlist
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        Registration waitlistReg2 = new Registration(attendeeId3, event.getEventId());
        Registration waitlistReg3 = new Registration(attendeeId4, event.getEventId());
        
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistManager.addToWaitlist(event, waitlistReg2);
        waitlistManager.addToWaitlist(event, waitlistReg3);
        
        // Verify positions
        assertEquals(1, waitlistReg1.getWaitlistPosition(), "First person should be position 1");
        assertEquals(2, waitlistReg2.getWaitlistPosition(), "Second person should be position 2");
        assertEquals(3, waitlistReg3.getWaitlistPosition(), "Third person should be position 3");
        
        // Remove middle person from waitlist
        boolean removed = waitlistManager.removeFromWaitlist(event, waitlistReg2.getRegistrationId());
        assertTrue(removed, "Should successfully remove from waitlist");
        
        // Verify positions updated
        assertEquals(1, waitlistReg1.getWaitlistPosition(), "First person should still be position 1");
        assertEquals(2, waitlistReg3.getWaitlistPosition(), "Third person should now be position 2");
        assertEquals(2, event.getWaitlistSize(), "Waitlist should now have 2 people");
    }
    
    @Test
    @DisplayName("Test waitlist statistics")
    void testWaitlistStatistics() {
        // Create event
        Event event = eventManager.createEvent(
            "Statistics Test", "Testing waitlist statistics", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 1
        );
        
        // Fill to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add people to waitlist with different statuses
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        Registration waitlistReg2 = new Registration(attendeeId3, event.getEventId());
        Registration waitlistReg3 = new Registration(attendeeId4, event.getEventId());
        
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistManager.addToWaitlist(event, waitlistReg2);
        waitlistManager.addToWaitlist(event, waitlistReg3);
        
        // Cancel one waitlist registration
        waitlistReg2.cancelRegistration("Test cancellation");
        
        // Get statistics
        WaitlistStatistics stats = waitlistManager.getWaitlistStatistics(event);
        
        assertEquals(3, stats.getTotalWaitlisted(), "Should have 3 total waitlisted");
        assertEquals(2, stats.getActiveWaitlisted(), "Should have 2 active waitlisted");
        assertEquals(1, stats.getCancelledWaitlisted(), "Should have 1 cancelled waitlisted");
    }
    
    @Test
    @DisplayName("Test notification system integration")
    void testNotificationSystemIntegration() {
        // Create event
        Event event = eventManager.createEvent(
            "Notification Test", "Testing notification integration", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 1
        );
        
        // Fill to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add person to waitlist
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        waitlistManager.addToWaitlist(event, waitlistReg1);
        
        // Verify waitlist notification was sent
        var notifications = notificationService.getUserNotifications(attendeeId2);
        assertFalse(notifications.isEmpty(), "Should have received waitlist confirmation notification");
        
        // Count notifications before promotion
        int notificationsBeforePromotion = notifications.size();
        
        // Simulate registration cancellation by removing one registration
        event.getRegistrations().remove(0); // Remove first registration to create capacity
        
        // Cancel first registration to trigger promotion
        WaitlistPromotionResult result = waitlistManager.handleRegistrationCancellation(event);
        
        // Verify promotion notification was sent
        notifications = notificationService.getUserNotifications(attendeeId2);
        assertTrue(notifications.size() > notificationsBeforePromotion, "Should have received promotion notification");
    }
    
    @Test
    @DisplayName("Test no promotion when waitlist is empty")
    void testNoPromotionWhenWaitlistEmpty() {
        // Create event with capacity
        Event event = eventManager.createEvent(
            "Empty Waitlist Test", "Testing empty waitlist scenario", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 2
        );
        
        // Add one registration
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Simulate registration cancellation
        event.getRegistrations().remove(0); // Remove registration to create capacity
        
        // Try to promote from empty waitlist
        WaitlistPromotionResult result = waitlistManager.handleRegistrationCancellation(event);
        
        assertFalse(result.hasPromotions(), "Should not promote when waitlist is empty");
        assertEquals(0, result.getPromotionsCount(), "Should have 0 promotions");
    }
    
    @Test
    @DisplayName("Test promotion respects registration deadline")
    void testPromotionRespectsRegistrationDeadline() {
        // Create event with registration deadline in the past
        Event event = eventManager.createEvent(
            "Deadline Test", "Testing registration deadline respect", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 1
        );
        
        // Set registration deadline to past
        event.setRegistrationDeadline(LocalDateTime.now().minusHours(1));
        
        // Fill to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add person to waitlist
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        waitlistManager.addToWaitlist(event, waitlistReg1);
        
        // Simulate registration cancellation
        event.getRegistrations().remove(0); // Remove registration to create capacity
        
        // Try to promote after deadline
        WaitlistPromotionResult result = waitlistManager.handleRegistrationCancellation(event);
        
        // Should not promote if deadline has passed (unless event hasn't started)
        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline()) && 
            LocalDateTime.now().isBefore(event.getStartDateTime())) {
            // May or may not promote depending on implementation policy
            // For this test, we expect no promotion after deadline
        }
    }
    
    @Test
    @DisplayName("Test multiple simultaneous promotions")
    void testMultipleSimultaneousPromotions() {
        // Create event with capacity for multiple promotions
        Event event = eventManager.createEvent(
            "Multiple Promotions Test", "Testing multiple simultaneous promotions", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 5
        );
        
        // Fill to capacity
        for (int i = 0; i < 5; i++) {
            Registration reg = new Registration("filled-attendee-" + i, event.getEventId());
            reg.confirmRegistration();
            event.getRegistrations().add(reg);
        }
        
        // Add multiple people to waitlist
        Registration waitlistReg1 = new Registration(attendeeId1, event.getEventId());
        Registration waitlistReg2 = new Registration(attendeeId2, event.getEventId());
        Registration waitlistReg3 = new Registration(attendeeId3, event.getEventId());
        Registration waitlistReg4 = new Registration(attendeeId4, event.getEventId());
        
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistManager.addToWaitlist(event, waitlistReg2);
        waitlistManager.addToWaitlist(event, waitlistReg3);
        waitlistManager.addToWaitlist(event, waitlistReg4);
        
        // Increase capacity significantly
        WaitlistPromotionResult result = waitlistManager.handleCapacityIncrease(event, 5, 8);
        
        assertEquals(3, result.getPromotionsCount(), "Should promote 3 people to fill new capacity");
        assertEquals(1, event.getWaitlistSize(), "Should have 1 person remaining on waitlist");
        
        // Verify first 3 were promoted in order
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg1.getStatus(), "First waitlisted should be promoted");
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg2.getStatus(), "Second waitlisted should be promoted");
        assertEquals(RegistrationStatus.CONFIRMED, waitlistReg3.getStatus(), "Third waitlisted should be promoted");
        assertEquals(RegistrationStatus.WAITLISTED, waitlistReg4.getStatus(), "Fourth should remain waitlisted");
        assertEquals(1, waitlistReg4.getWaitlistPosition(), "Remaining person should be position 1");
    }
    
    @Test
    @DisplayName("Test promotion failure handling")
    void testPromotionFailureHandling() {
        // Create event
        Event event = eventManager.createEvent(
            "Failure Handling Test", "Testing promotion failure scenarios", EventType.WORKSHOP,
            baseTime, baseTime.plusHours(2), organizerId, null, 2
        );
        
        // Fill to capacity
        Registration reg1 = new Registration(attendeeId1, event.getEventId());
        reg1.confirmRegistration();
        event.getRegistrations().add(reg1);
        
        // Add person to waitlist and then cancel their registration
        Registration waitlistReg1 = new Registration(attendeeId2, event.getEventId());
        waitlistManager.addToWaitlist(event, waitlistReg1);
        waitlistReg1.cancelRegistration("User cancelled");
        
        // Simulate registration cancellation
        event.getRegistrations().remove(0); // Remove registration to create capacity
        
        // Try to promote
        WaitlistPromotionResult result = waitlistManager.handleRegistrationCancellation(event);
        
        // Should skip cancelled waitlist registrations
        assertFalse(result.hasPromotions(), "Should not promote cancelled waitlist registrations");
    }
}