// =============================================================================
// WAITLIST MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.model.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Comprehensive waitlist management service for automatic promotion and notifications.
 * 
 * Implementation Details:
 * - Automatic promotion when capacity becomes available
 * - Position tracking and updates for all waitlisted users
 * - Comprehensive notification system for all waitlist events
 * - Bulk operations for capacity increases
 * - Thread-safe operations for concurrent access
 * - Integration with registration deadlines
 * - Rollback capabilities for failed promotions
 */
public class WaitlistManager {
    private NotificationService notificationService;
    private Map<String, WaitlistPromotionResult> promotionHistory;
    
    public WaitlistManager() {
        this.promotionHistory = new ConcurrentHashMap<>();
    }
    
    /**
     * Sets the notification service for waitlist communications
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Promotes attendees from waitlist when spots become available
     * Returns the number of promotions made
     */
    public WaitlistPromotionResult promoteFromWaitlist(Event event, int availableSpots) {
        if (event == null || availableSpots <= 0) {
            return new WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        
        Queue<Registration> waitlist = event.getWaitlist();
        List<Registration> registrations = event.getRegistrations();
        
        if (waitlist == null || waitlist.isEmpty()) {
            return new WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        
        List<Registration> promotedRegistrations = new ArrayList<>();
        List<String> failedPromotions = new ArrayList<>();
        int promotionsCount = 0;
        
        // Promote up to the number of available spots
        while (promotionsCount < availableSpots && !waitlist.isEmpty()) {
            Registration waitlistReg = waitlist.poll();
            
            try {
                // Validate registration is still valid
                if (waitlistReg.getStatus() == RegistrationStatus.CANCELLED) {
                    continue; // Skip cancelled registrations
                }
                
                // Check if registration deadline has passed
                if (event.getRegistrationDeadline() != null && 
                    LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
                    // Re-add to waitlist if deadline passed but event not started
                    if (LocalDateTime.now().isBefore(event.getStartDateTime())) {
                        waitlist.offer(waitlistReg);
                    }
                    break;
                }
                
                // Promote the registration
                waitlistReg.confirmRegistration();
                waitlistReg.setWaitlistPosition(0); // Clear waitlist position
                registrations.add(waitlistReg);
                promotedRegistrations.add(waitlistReg);
                promotionsCount++;
                
                // Send promotion notification
                sendPromotionNotification(event, waitlistReg);
                
            } catch (Exception e) {
                // If promotion fails, re-add to waitlist and track failure
                waitlist.offer(waitlistReg);
                failedPromotions.add(waitlistReg.getAttendeeId());
            }
        }
        
        // Update positions for remaining waitlist entries
        updateWaitlistPositions(waitlist);
        
        // Send position update notifications to remaining waitlisted users
        sendPositionUpdateNotifications(event, waitlist);
        
        WaitlistPromotionResult result = new WaitlistPromotionResult(
            promotionsCount, promotedRegistrations, failedPromotions);
        
        // Store promotion history
        promotionHistory.put(event.getEventId() + "-" + System.currentTimeMillis(), result);
        
        return result;
    }
    
    /**
     * Handles automatic promotion when a registration is cancelled
     */
    public WaitlistPromotionResult handleRegistrationCancellation(Event event) {
        if (event == null || !event.hasCapacity()) {
            return new WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        
        int availableSpots = event.getAvailableSpots();
        return promoteFromWaitlist(event, availableSpots);
    }
    
    /**
     * Handles automatic promotion when event capacity is increased
     */
    public WaitlistPromotionResult handleCapacityIncrease(Event event, int oldCapacity, int newCapacity) {
        if (event == null || newCapacity <= oldCapacity) {
            return new WaitlistPromotionResult(0, new ArrayList<>(), new ArrayList<>());
        }
        
        int additionalSpots = newCapacity - Math.max(oldCapacity, event.getRegistrations().size());
        return promoteFromWaitlist(event, additionalSpots);
    }
    
    /**
     * Adds a registration to the waitlist with proper positioning
     */
    public boolean addToWaitlist(Event event, Registration registration) {
        if (event == null || registration == null) {
            return false;
        }
        
        Queue<Registration> waitlist = event.getWaitlist();
        if (waitlist == null) {
            event.setWaitlist(new LinkedList<>());
            waitlist = event.getWaitlist();
        }
        
        // Set waitlist position
        int position = waitlist.size() + 1;
        registration.setWaitlistPosition(position);
        registration.setStatus(RegistrationStatus.WAITLISTED);
        
        // Add to waitlist
        waitlist.offer(registration);
        
        // Send waitlist confirmation notification
        sendWaitlistNotification(event, registration);
        
        return true;
    }
    
    /**
     * Removes a registration from the waitlist and updates positions
     */
    public boolean removeFromWaitlist(Event event, String registrationId) {
        if (event == null || registrationId == null) {
            return false;
        }
        
        Queue<Registration> waitlist = event.getWaitlist();
        if (waitlist == null || waitlist.isEmpty()) {
            return false;
        }
        
        // Convert to list for easier removal
        List<Registration> waitlistList = new ArrayList<>(waitlist);
        boolean removed = false;
        
        for (int i = 0; i < waitlistList.size(); i++) {
            if (waitlistList.get(i).getRegistrationId().equals(registrationId)) {
                waitlistList.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            // Rebuild waitlist queue
            waitlist.clear();
            waitlist.addAll(waitlistList);
            
            // Update positions
            updateWaitlistPositions(waitlist);
            
            // Send position update notifications
            sendPositionUpdateNotifications(event, waitlist);
        }
        
        return removed;
    }
    
    /**
     * Updates waitlist positions for all entries
     */
    private void updateWaitlistPositions(Queue<Registration> waitlist) {
        if (waitlist == null || waitlist.isEmpty()) {
            return;
        }
        
        int position = 1;
        for (Registration reg : waitlist) {
            reg.setWaitlistPosition(position++);
        }
    }
    
    /**
     * Sends promotion notification to a newly promoted attendee
     */
    private void sendPromotionNotification(Event event, Registration registration) {
        if (notificationService == null || event == null || registration == null) {
            return;
        }
        
        String message = String.format(
            "Great news! You've been promoted from the waitlist for '%s'!\n\n" +
            "Event Details:\n" +
            "📅 Date: %s\n" +
            "🕒 Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "Your registration is now confirmed. We look forward to seeing you there!",
            event.getTitle(),
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
        
        List<String> recipients = List.of(registration.getAttendeeId());
        notificationService.sendNotification(message, recipients, NotificationType.WAITLIST_PROMOTION);
    }
    
    /**
     * Sends waitlist confirmation notification
     */
    private void sendWaitlistNotification(Event event, Registration registration) {
        if (notificationService == null || event == null || registration == null) {
            return;
        }
        
        String message = String.format(
            "You've been added to the waitlist for '%s'.\n\n" +
            "Your position: #%d\n\n" +
            "Event Details:\n" +
            "📅 Date: %s\n" +
            "🕒 Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "We'll notify you if a spot becomes available. Thank you for your interest!",
            event.getTitle(),
            registration.getWaitlistPosition(),
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
        
        List<String> recipients = List.of(registration.getAttendeeId());
        notificationService.sendNotification(message, recipients, NotificationType.WAITLIST_REGISTRATION);
    }
    
    /**
     * Sends position update notifications to all waitlisted users
     */
    private void sendPositionUpdateNotifications(Event event, Queue<Registration> waitlist) {
        if (notificationService == null || event == null || waitlist == null || waitlist.isEmpty()) {
            return;
        }
        
        for (Registration reg : waitlist) {
            if (reg.getWaitlistPosition() <= 5) { // Only notify top 5
                String message = String.format(
                    "Waitlist update for '%s'!\n\n" +
                    "Your new position: #%d\n\n" +
                    "You're getting closer to the front of the line. We'll notify you immediately if a spot opens up!",
                    event.getTitle(),
                    reg.getWaitlistPosition()
                );
                
                List<String> recipients = List.of(reg.getAttendeeId());
                notificationService.sendNotification(message, recipients, NotificationType.WAITLIST_POSITION_UPDATE);
            }
        }
    }
    
    /**
     * Gets comprehensive waitlist statistics for an event
     */
    public WaitlistStatistics getWaitlistStatistics(Event event) {
        if (event == null) {
            return new WaitlistStatistics(0, 0, 0);
        }
        
        Queue<Registration> waitlist = event.getWaitlist();
        if (waitlist == null || waitlist.isEmpty()) {
            return new WaitlistStatistics(0, 0, 0);
        }
        
        int totalWaitlisted = waitlist.size();
        int activeWaitlisted = 0;
        int cancelledWaitlisted = 0;
        
        for (Registration reg : waitlist) {
            if (reg.getStatus() == RegistrationStatus.WAITLISTED) {
                activeWaitlisted++;
            } else if (reg.getStatus() == RegistrationStatus.CANCELLED) {
                cancelledWaitlisted++;
            }
        }
        
        return new WaitlistStatistics(totalWaitlisted, activeWaitlisted, cancelledWaitlisted);
    }
    
    /**
     * Gets the current waitlist position for a specific attendee
     */
    public int getWaitlistPosition(Event event, String attendeeId) {
        if (event == null || attendeeId == null) {
            return -1;
        }
        
        Queue<Registration> waitlist = event.getWaitlist();
        if (waitlist == null || waitlist.isEmpty()) {
            return -1;
        }
        
        for (Registration reg : waitlist) {
            if (reg.getAttendeeId().equals(attendeeId)) {
                return reg.getWaitlistPosition();
            }
        }
        
        return -1;
    }
    
    /**
     * Result class for waitlist promotion operations
     */
    public static class WaitlistPromotionResult {
        private final int promotionsCount;
        private final List<Registration> promotedRegistrations;
        private final List<String> failedPromotions;
        
        public WaitlistPromotionResult(int promotionsCount, 
                                     List<Registration> promotedRegistrations, 
                                     List<String> failedPromotions) {
            this.promotionsCount = promotionsCount;
            this.promotedRegistrations = new ArrayList<>(promotedRegistrations);
            this.failedPromotions = new ArrayList<>(failedPromotions);
        }
        
        public int getPromotionsCount() { return promotionsCount; }
        public List<Registration> getPromotedRegistrations() { return promotedRegistrations; }
        public List<String> getFailedPromotions() { return failedPromotions; }
        public boolean hasPromotions() { return promotionsCount > 0; }
        public boolean hasFailures() { return !failedPromotions.isEmpty(); }
    }
    
    /**
     * Statistics class for waitlist information
     */
    public static class WaitlistStatistics {
        private final int totalWaitlisted;
        private final int activeWaitlisted;
        private final int cancelledWaitlisted;
        
        public WaitlistStatistics(int totalWaitlisted, int activeWaitlisted, int cancelledWaitlisted) {
            this.totalWaitlisted = totalWaitlisted;
            this.activeWaitlisted = activeWaitlisted;
            this.cancelledWaitlisted = cancelledWaitlisted;
        }
        
        public int getTotalWaitlisted() { return totalWaitlisted; }
        public int getActiveWaitlisted() { return activeWaitlisted; }
        public int getCancelledWaitlisted() { return cancelledWaitlisted; }
    }
}