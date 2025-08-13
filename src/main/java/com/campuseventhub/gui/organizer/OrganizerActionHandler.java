package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.util.ValidationUtil;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrganizerActionHandler {
    private EventHub eventHub;
    private Organizer organizer;
    private JFrame parentFrame;
    
    public OrganizerActionHandler(EventHub eventHub, Organizer organizer, JFrame parentFrame) {
        this.eventHub = eventHub;
        this.organizer = organizer;
        this.parentFrame = parentFrame;
    }
    
    public boolean createEvent(String title, String description, EventType type, 
                              String capacityStr, LocalDateTime startDateTime, LocalDateTime endDateTime, String venueId) {
        try {
            // Validate event title
            String titleError = ValidationUtil.validateEventTitle(title);
            if (titleError != null) {
                JOptionPane.showMessageDialog(parentFrame, titleError, "Invalid Event Title", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate description
            if (description == null || description.trim().isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Event description cannot be empty.", "Missing Description", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate capacity
            String capacityError = ValidationUtil.validateEventCapacity(capacityStr);
            if (capacityError != null) {
                JOptionPane.showMessageDialog(parentFrame, capacityError, "Invalid Capacity", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate start date/time
            String startTimeError = ValidationUtil.validateEventDateTime(startDateTime, "Start date/time");
            if (startTimeError != null) {
                JOptionPane.showMessageDialog(parentFrame, startTimeError, "Invalid Start Time", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate end date/time
            String endTimeError = ValidationUtil.validateEventDateTime(endDateTime, "End date/time");
            if (endTimeError != null) {
                JOptionPane.showMessageDialog(parentFrame, endTimeError, "Invalid End Time", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Validate time range
            String timeRangeError = ValidationUtil.validateTimeRange(startDateTime, endDateTime);
            if (timeRangeError != null) {
                JOptionPane.showMessageDialog(parentFrame, timeRangeError, "Invalid Time Range", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // All validation passed, create the event
            int capacity = Integer.parseInt(capacityStr.trim());
            Event event = eventHub.createEvent(title, description, type, startDateTime, 
                endDateTime, organizer.getUserId(), venueId, capacity);
            
            if (event != null) {
                event.setMaxCapacity(capacity);
                event.setRegistrationDeadline(startDateTime.minusHours(1));
                eventHub.getEventManager().update(event);
                
                JOptionPane.showMessageDialog(parentFrame, 
                    "Event '" + title + "' created successfully!\n\n" +
                    "Start: " + startDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")) + "\n" +
                    "End: " + endDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")) + "\n" +
                    "Capacity: " + capacity + " people", 
                    "Event Created", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Failed to create event. This may be due to:\n" +
                    "• Insufficient permissions\n" +
                    "• A scheduling conflict\n" +
                    "• System error\n\n" +
                    "Please try again or contact an administrator.", 
                    "Event Creation Failed", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "An unexpected error occurred while creating the event:\n\n" + 
                e.getMessage() + "\n\n" +
                "Please try again or contact technical support if the problem persists.", 
                "System Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    public void viewEventDetails(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events found")) {
            JOptionPane.showMessageDialog(parentFrame, "Event Details:\n" + selectedEvent, 
                "Event Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void editEvent(Event selectedEvent) {
        if (selectedEvent != null) {
            EventEditingDialog dialog = new EventEditingDialog(parentFrame, eventHub, selectedEvent);
            boolean changesSaved = dialog.showDialog();
            
            if (changesSaved) {
                // Refresh the event list to show updated information
                // This will be handled by the dashboard's event list panel
                JOptionPane.showMessageDialog(parentFrame, 
                    "Event updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, 
                "Please select an event to edit.", 
                "No Event Selected", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void cancelEvent(Event selectedEvent) {
        if (selectedEvent != null) {
            // Check if event can be cancelled
            if (!eventHub.canCancelEvent(selectedEvent.getEventId())) {
                JOptionPane.showMessageDialog(parentFrame,
                    "This event cannot be cancelled. It may have already started, been completed, or already cancelled.",
                    "Cannot Cancel Event",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            EventCancellationDialog dialog = new EventCancellationDialog(parentFrame, eventHub, selectedEvent);
            boolean wasCancelled = dialog.showDialog();
            
            if (wasCancelled) {
                JOptionPane.showMessageDialog(parentFrame,
                    "Event has been successfully cancelled. All registered attendees have been notified.",
                    "Event Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame,
                "Please select an event to cancel.",
                "No Event Selected",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void rescheduleEvent(Event selectedEvent) {
        if (selectedEvent != null) {
            // Check if event can be rescheduled
            if (!eventHub.canRescheduleEvent(selectedEvent.getEventId())) {
                JOptionPane.showMessageDialog(parentFrame,
                    "This event cannot be rescheduled. It may have already started, been completed, or been cancelled.",
                    "Cannot Reschedule Event",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            EventReschedulingDialog dialog = new EventReschedulingDialog(parentFrame, eventHub, selectedEvent);
            boolean wasRescheduled = dialog.showDialog();
            
            if (wasRescheduled) {
                JOptionPane.showMessageDialog(parentFrame,
                    "Event has been successfully rescheduled. All registered attendees have been notified.",
                    "Event Rescheduled",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame,
                "Please select an event to reschedule.",
                "No Event Selected",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public boolean publishEvent(String selectedEvent) {
        if (selectedEvent == null || selectedEvent.contains("No events found")) {
            return false;
        }
        
        int choice = JOptionPane.showConfirmDialog(parentFrame, 
            "Are you sure you want to publish this event?", 
            "Confirm Publish", JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }
        
        String eventTitle = selectedEvent.split(" - ")[0];
        List<Event> myEvents = eventHub.getEventsByOrganizer(organizer.getUserId());
        Event targetEvent = null;
        
        for (Event event : myEvents) {
            if (event.getTitle().equals(eventTitle)) {
                targetEvent = event;
                break;
            }
        }
        
        if (targetEvent != null) {
            targetEvent.updateStatus(EventStatus.PUBLISHED);
            JOptionPane.showMessageDialog(parentFrame, 
                "Event published successfully! It is now open for registration.");
            return true;
        } else {
            JOptionPane.showMessageDialog(parentFrame, 
                "Event not found. Please refresh the event list.");
            return false;
        }
    }
}