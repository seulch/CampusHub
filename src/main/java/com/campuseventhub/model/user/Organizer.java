// =============================================================================
// ORGANIZER USER IMPLEMENTATION
// =============================================================================

package com.campuseventhub.model.user;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.report.Report;
import java.util.List;
import java.util.ArrayList;

/**
 * Organizer user class with event management capabilities.
 * 
 * Implementation Details:
 * - Maintains list of created events
 * - Department-based event categorization
 * - Event lifecycle management (create, update, cancel)
 * - Analytics and reporting for organized events
 * - Notification system for attendee communication
 * - Capacity and venue management
 */
public class Organizer extends User {
    private List<Event> createdEvents;
    private String department;
    private String contactNumber;
    private int maxEventsAllowed;
    
    public Organizer(String username, String email, String password,
                    String firstName, String lastName, String department) {
        super(username, email, password, firstName, lastName);
        // TODO: Initialize createdEvents list
        // TODO: Set department and validate against predefined list
        // TODO: Set default maxEventsAllowed from configuration
        // TODO: Set status to PENDING_APPROVAL for new organizers
    }
    
    public Event createEvent(String title, String description) {
        // TODO: Check if organizer has reached maximum event limit
        // TODO: Validate event details (date, venue availability, etc.)
        // TODO: Create new Event instance with DRAFT status
        // TODO: Add event to createdEvents list
        // TODO: Save event to persistence layer
        // TODO: Log event creation
        // TODO: Return created event
        return null;
    }
    
    public boolean updateEvent(String eventId) {
        // TODO: Find event in createdEvents list
        // TODO: Verify organizer owns this event
        // TODO: Check if event is in editable state
        // TODO: Validate updated information
        // TODO: Update event details
        // TODO: Notify registered attendees of changes
        // TODO: Save updated event
        // TODO: Return success status
        return false;
    }
    
    public boolean cancelEvent(String eventId, String reason) {
        // TODO: Find and validate event ownership
        // TODO: Check if event can be cancelled (not already completed)
        // TODO: Update event status to CANCELLED
        // TODO: Notify all registered attendees
        // TODO: Handle refunds/credits if applicable
        // TODO: Update venue availability
        // TODO: Log cancellation with reason
        return false;
    }
    
    public Report getEventAnalytics(String eventId) {
        // TODO: Verify event ownership
        // TODO: Collect registration data, attendance data
        // TODO: Calculate metrics (attendance rate, demographics, etc.)
        // TODO: Generate EventReport instance
        // TODO: Return comprehensive analytics report
        return null;
    }
    
    public void sendNotification(String message, List<String> recipients) {
        // TODO: Validate message content and recipients
        // TODO: Use NotificationService to send messages
        // TODO: Support multiple notification types (email, in-app)
        // TODO: Log notification sending
        // TODO: Handle delivery failures gracefully
    }
    
    @Override
    public UserRole getRole() {
        return UserRole.ORGANIZER;
    }
    
    // TODO: Add additional methods for event management
    // public List<Event> getUpcomingEvents()
    // public List<Event> getCompletedEvents()
    // public boolean rescheduleEvent(String eventId, LocalDateTime newDateTime)
}
