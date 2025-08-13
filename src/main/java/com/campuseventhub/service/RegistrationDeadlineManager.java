// =============================================================================
// REGISTRATION DEADLINE MANAGER SERVICE
// =============================================================================

package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.notification.NotificationType;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Comprehensive registration deadline management service with automatic closure and notifications.
 * 
 * Implementation Details:
 * - Automatic registration closure when deadlines are reached
 * - Configurable warning notifications before deadlines
 * - Batch processing for efficient deadline monitoring
 * - Thread-safe operations for concurrent access
 * - Integration with event lifecycle management
 * - Deadline extension capabilities for organizers
 * - Statistical reporting for deadline compliance
 */
public class RegistrationDeadlineManager {
    private EventManager eventManager;
    private NotificationService notificationService;
    private ScheduledExecutorService scheduler;
    private Map<String, LocalDateTime> lastDeadlineCheck;
    private Map<String, Boolean> warningsSent;
    private boolean isRunning;
    
    // Configuration settings
    private static final int CHECK_INTERVAL_MINUTES = 5;
    private static final List<Duration> WARNING_INTERVALS = List.of(
        Duration.ofDays(1),    // 24 hours before deadline
        Duration.ofHours(4),   // 4 hours before deadline
        Duration.ofHours(1)    // 1 hour before deadline
    );
    
    public RegistrationDeadlineManager() {
        this.lastDeadlineCheck = new ConcurrentHashMap<>();
        this.warningsSent = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RegistrationDeadlineMonitor");
            t.setDaemon(true);
            return t;
        });
        this.isRunning = false;
    }
    
    /**
     * Sets the event manager for deadline monitoring
     */
    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Sets the notification service for deadline notifications
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Starts the deadline monitoring service
     */
    public void startDeadlineMonitoring() {
        if (isRunning || eventManager == null) {
            return;
        }
        
        isRunning = true;
        scheduler.scheduleAtFixedRate(
            this::processDeadlines, 
            0, 
            CHECK_INTERVAL_MINUTES, 
            TimeUnit.MINUTES
        );
        
        System.out.println("RegistrationDeadlineManager: Deadline monitoring started");
    }
    
    /**
     * Stops the deadline monitoring service
     */
    public void stopDeadlineMonitoring() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        System.out.println("RegistrationDeadlineManager: Deadline monitoring stopped");
    }
    
    /**
     * Main processing method that checks all events for deadline compliance
     */
    private void processDeadlines() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Event> events = eventManager.findAll();
            
            for (Event event : events) {
                if (event.getRegistrationDeadline() != null && 
                    event.getStatus() == EventStatus.PUBLISHED) {
                    
                    processEventDeadline(event, now);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing registration deadlines: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Processes deadline logic for a specific event
     */
    private void processEventDeadline(Event event, LocalDateTime now) {
        String eventId = event.getEventId();
        LocalDateTime deadline = event.getRegistrationDeadline();
        
        // Check if registration should be closed
        if (now.isAfter(deadline) || now.isEqual(deadline)) {
            closeEventRegistration(event, now);
            return;
        }
        
        // Check for warning notifications
        processDeadlineWarnings(event, now);
    }
    
    /**
     * Closes event registration and notifies attendees
     */
    private void closeEventRegistration(Event event, LocalDateTime closureTime) {
        String eventId = event.getEventId();
        
        // Prevent duplicate closures - check if we've already processed this deadline
        if (lastDeadlineCheck.containsKey(eventId)) {
            return;
        }
        
        try {
            // Close registration by updating event status to REGISTRATION_CLOSED
            // Since REGISTRATION_CLOSED doesn't exist in EventStatus, we'll use a different approach
            // We'll rely on the isRegistrationOpen() method which checks deadline
            
            // Mark the deadline as processed
            lastDeadlineCheck.put(eventId, closureTime);
            
            // Send closure notifications
            sendRegistrationClosureNotifications(event, closureTime);
            
            // Send notifications to waitlisted users
            sendWaitlistClosureNotifications(event);
            
            // Log the closure
            System.out.println(String.format(
                "RegistrationDeadlineManager: Registration closed for event '%s' (ID: %s) at %s",
                event.getTitle(), eventId, closureTime
            ));
            
            // Update event modification time
            event.setLastModified(closureTime);
            eventManager.update(event);
            
        } catch (Exception e) {
            System.err.println(String.format(
                "Failed to close registration for event %s: %s", eventId, e.getMessage()
            ));
        }
    }
    
    /**
     * Processes deadline warning notifications
     */
    private void processDeadlineWarnings(Event event, LocalDateTime now) {
        String eventId = event.getEventId();
        LocalDateTime deadline = event.getRegistrationDeadline();
        Duration timeUntilDeadline = Duration.between(now, deadline);
        
        for (Duration warningInterval : WARNING_INTERVALS) {
            String warningKey = eventId + "-" + warningInterval.toHours();
            
            if (timeUntilDeadline.compareTo(warningInterval) <= 0 && 
                !warningsSent.getOrDefault(warningKey, false)) {
                
                sendDeadlineWarningNotifications(event, warningInterval, timeUntilDeadline);
                warningsSent.put(warningKey, true);
                break; // Only send one warning at a time
            }
        }
    }
    
    /**
     * Sends registration closure notifications to all registered attendees
     */
    private void sendRegistrationClosureNotifications(Event event, LocalDateTime closureTime) {
        if (notificationService == null) {
            return;
        }
        
        List<String> registeredAttendeeIds = event.getRegistrations().stream()
            .map(Registration::getAttendeeId)
            .collect(Collectors.toList());
        
        if (registeredAttendeeIds.isEmpty()) {
            return;
        }
        
        String message = String.format(
            "Registration has closed for '%s'!\n\n" +
            "📅 Event Date: %s\n" +
            "🕒 Event Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "Your registration is confirmed. We look forward to seeing you there!\n\n" +
            "If you need to cancel your registration, please contact the organizer as soon as possible.",
            event.getTitle(),
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
        
        notificationService.sendNotification(message, registeredAttendeeIds, NotificationType.EVENT_UPDATE);
    }
    
    /**
     * Sends closure notifications to waitlisted attendees
     */
    private void sendWaitlistClosureNotifications(Event event) {
        if (notificationService == null || event.getWaitlist() == null || event.getWaitlist().isEmpty()) {
            return;
        }
        
        List<String> waitlistedAttendeeIds = event.getWaitlist().stream()
            .map(Registration::getAttendeeId)
            .collect(Collectors.toList());
        
        String message = String.format(
            "Registration has closed for '%s'.\n\n" +
            "Unfortunately, you were not promoted from the waitlist before the registration deadline.\n\n" +
            "📅 Event Date: %s\n" +
            "🕒 Event Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "Thank you for your interest. Please consider registering early for future events!",
            event.getTitle(),
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
        
        notificationService.sendNotification(message, waitlistedAttendeeIds, NotificationType.EVENT_UPDATE);
    }
    
    /**
     * Sends deadline warning notifications
     */
    private void sendDeadlineWarningNotifications(Event event, Duration warningInterval, Duration timeRemaining) {
        if (notificationService == null) {
            return;
        }
        
        // Send to registered attendees
        List<String> registeredAttendeeIds = event.getRegistrations().stream()
            .map(Registration::getAttendeeId)
            .collect(Collectors.toList());
        
        if (!registeredAttendeeIds.isEmpty()) {
            String registeredMessage = createWarningMessage(event, timeRemaining, false);
            notificationService.sendNotification(registeredMessage, registeredAttendeeIds, NotificationType.EVENT_REMINDER);
        }
        
        // Send to waitlisted attendees
        if (event.getWaitlist() != null && !event.getWaitlist().isEmpty()) {
            List<String> waitlistedAttendeeIds = event.getWaitlist().stream()
                .map(Registration::getAttendeeId)
                .collect(Collectors.toList());
            
            String waitlistMessage = createWarningMessage(event, timeRemaining, true);
            notificationService.sendNotification(waitlistMessage, waitlistedAttendeeIds, NotificationType.EVENT_REMINDER);
        }
    }
    
    /**
     * Creates deadline warning message
     */
    private String createWarningMessage(Event event, Duration timeRemaining, boolean isWaitlisted) {
        String timeDescription = formatTimeRemaining(timeRemaining);
        String statusMessage = isWaitlisted ? 
            "You are currently on the waitlist. Registration will close soon, so your chances of being promoted are running out!" :
            "Your registration is confirmed.";
        
        return String.format(
            "⏰ Registration closes %s for '%s'!\n\n" +
            "%s\n\n" +
            "📅 Event Date: %s\n" +
            "🕒 Event Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "Don't miss out!",
            timeDescription,
            event.getTitle(),
            statusMessage,
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
    }
    
    /**
     * Formats time remaining in a human-readable format
     */
    private String formatTimeRemaining(Duration timeRemaining) {
        long hours = timeRemaining.toHours();
        long days = timeRemaining.toDays();
        
        if (days > 0) {
            return String.format("in %d day%s", days, days == 1 ? "" : "s");
        } else if (hours > 0) {
            return String.format("in %d hour%s", hours, hours == 1 ? "" : "s");
        } else {
            long minutes = timeRemaining.toMinutes();
            return String.format("in %d minute%s", Math.max(minutes, 1), minutes == 1 ? "" : "s");
        }
    }
    
    /**
     * Manually processes deadlines for a specific event (for testing or immediate processing)
     */
    public void processEventDeadlineImmediately(String eventId) {
        if (eventManager == null) {
            return;
        }
        
        Event event = eventManager.findById(eventId);
        if (event != null) {
            processEventDeadline(event, LocalDateTime.now());
        }
    }
    
    /**
     * Extends the registration deadline for an event
     */
    public boolean extendRegistrationDeadline(String eventId, LocalDateTime newDeadline, String reason) {
        if (eventManager == null) {
            return false;
        }
        
        Event event = eventManager.findById(eventId);
        if (event == null || newDeadline == null) {
            return false;
        }
        
        LocalDateTime oldDeadline = event.getRegistrationDeadline();
        if (oldDeadline != null && !newDeadline.isAfter(oldDeadline)) {
            return false; // New deadline must be later than current deadline
        }
        
        // Update the deadline
        event.setRegistrationDeadline(newDeadline);
        event.setLastModified(LocalDateTime.now());
        eventManager.update(event);
        
        // Clear warning flags for this event to allow new warnings
        WARNING_INTERVALS.forEach(interval -> {
            String warningKey = eventId + "-" + interval.toHours();
            warningsSent.remove(warningKey);
        });
        
        // Send deadline extension notifications
        sendDeadlineExtensionNotifications(event, oldDeadline, newDeadline, reason);
        
        return true;
    }
    
    /**
     * Sends deadline extension notifications
     */
    private void sendDeadlineExtensionNotifications(Event event, LocalDateTime oldDeadline, 
                                                   LocalDateTime newDeadline, String reason) {
        if (notificationService == null) {
            return;
        }
        
        // Collect all affected users (registered + waitlisted)
        List<String> allAttendeeIds = new ArrayList<>();
        
        event.getRegistrations().stream()
            .map(Registration::getAttendeeId)
            .forEach(allAttendeeIds::add);
        
        if (event.getWaitlist() != null) {
            event.getWaitlist().stream()
                .map(Registration::getAttendeeId)
                .forEach(allAttendeeIds::add);
        }
        
        if (allAttendeeIds.isEmpty()) {
            return;
        }
        
        String message = String.format(
            "📅 Registration deadline extended for '%s'!\n\n" +
            "Previous deadline: %s at %s\n" +
            "New deadline: %s at %s\n\n" +
            "%s\n\n" +
            "📅 Event Date: %s\n" +
            "🕒 Event Time: %s - %s\n" +
            "📍 Location: %s\n\n" +
            "This gives you more time to make your decision!",
            event.getTitle(),
            oldDeadline.toLocalDate(),
            oldDeadline.toLocalTime(),
            newDeadline.toLocalDate(),
            newDeadline.toLocalTime(),
            reason != null && !reason.trim().isEmpty() ? "Reason: " + reason : "Extended to accommodate more registrations.",
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            event.getVenueName()
        );
        
        notificationService.sendNotification(message, allAttendeeIds, NotificationType.EVENT_UPDATE);
    }
    
    /**
     * Gets registration deadline statistics
     */
    public RegistrationDeadlineStatistics getDeadlineStatistics() {
        if (eventManager == null) {
            return new RegistrationDeadlineStatistics(0, 0, 0, 0);
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<Event> publishedEvents = eventManager.findAll().stream()
            .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
            .collect(Collectors.toList());
        
        int totalEvents = publishedEvents.size();
        int eventsWithDeadlines = 0;
        int openRegistrations = 0;
        int closedRegistrations = 0;
        
        for (Event event : publishedEvents) {
            if (event.getRegistrationDeadline() != null) {
                eventsWithDeadlines++;
                if (event.isRegistrationOpen()) {
                    openRegistrations++;
                } else {
                    closedRegistrations++;
                }
            }
        }
        
        return new RegistrationDeadlineStatistics(totalEvents, eventsWithDeadlines, openRegistrations, closedRegistrations);
    }
    
    /**
     * Statistics class for registration deadlines
     */
    public static class RegistrationDeadlineStatistics {
        private final int totalPublishedEvents;
        private final int eventsWithDeadlines;
        private final int openRegistrations;
        private final int closedRegistrations;
        
        public RegistrationDeadlineStatistics(int totalPublishedEvents, int eventsWithDeadlines, 
                                            int openRegistrations, int closedRegistrations) {
            this.totalPublishedEvents = totalPublishedEvents;
            this.eventsWithDeadlines = eventsWithDeadlines;
            this.openRegistrations = openRegistrations;
            this.closedRegistrations = closedRegistrations;
        }
        
        public int getTotalPublishedEvents() { return totalPublishedEvents; }
        public int getEventsWithDeadlines() { return eventsWithDeadlines; }
        public int getOpenRegistrations() { return openRegistrations; }
        public int getClosedRegistrations() { return closedRegistrations; }
        
        public double getDeadlineComplianceRate() {
            return totalPublishedEvents > 0 ? (double) eventsWithDeadlines / totalPublishedEvents : 0.0;
        }
    }
}