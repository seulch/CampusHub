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
            // Get the actual event object to show detailed information
            Event selectedEventObj = getSelectedEventFromString(selectedEvent);
            if (selectedEventObj != null) {
                showDetailedEventInfo(selectedEventObj);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Event Details:\n" + selectedEvent, 
                    "Event Information", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Shows detailed event information including registrations
     */
    private void showDetailedEventInfo(Event event) {
        StringBuilder details = new StringBuilder();
        details.append("=== EVENT DETAILS ===\n\n");
        details.append("Title: ").append(event.getTitle()).append("\n");
        details.append("Description: ").append(event.getDescription()).append("\n");
        details.append("Type: ").append(event.getEventType().getDisplayName()).append("\n");
        details.append("Status: ").append(event.getStatus().getDisplayName()).append("\n");
        details.append("Start: ").append(event.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        details.append("End: ").append(event.getEndDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        
        if (event.hasVenue()) {
            details.append("Venue: ").append(event.getVenueName()).append("\n");
        }
        
        // Registration information
        int totalRegistrations = event.getRegistrations() != null ? event.getRegistrations().size() : 0;
        int confirmedRegistrations = 0;
        int waitlistedRegistrations = 0;
        
        if (event.getRegistrations() != null) {
            for (com.campuseventhub.model.event.Registration reg : event.getRegistrations()) {
                if (reg.getStatus() == com.campuseventhub.model.event.RegistrationStatus.CONFIRMED) {
                    confirmedRegistrations++;
                } else if (reg.getStatus() == com.campuseventhub.model.event.RegistrationStatus.WAITLISTED) {
                    waitlistedRegistrations++;
                }
            }
        }
        
        details.append("\n=== REGISTRATION DETAILS ===\n");
        details.append("Total Capacity: ").append(event.getMaxCapacity()).append("\n");
        details.append("Confirmed Registrations: ").append(confirmedRegistrations).append("\n");
        details.append("Waitlisted Registrations: ").append(waitlistedRegistrations).append("\n");
        details.append("Available Spots: ").append(event.getAvailableSpots()).append("\n");
        details.append("Registration Status: ").append(event.isRegistrationOpen() ? "Open" : "Closed").append("\n");
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        
        JOptionPane.showMessageDialog(parentFrame, scrollPane, 
            "Event Details: " + event.getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Helper method to get the Event object from the list display string
     */
    private Event getSelectedEventFromString(String eventDisplayString) {
        // Try to get the event from the event list panel
        if (parentFrame instanceof OrganizerDashboard) {
            OrganizerDashboard dashboard = (OrganizerDashboard) parentFrame;
            // This would need to be implemented in the dashboard to get the selected event object
            // For now, we'll parse the title from the string and search for it
            String eventTitle = eventDisplayString.split(" - ")[0];
            java.util.List<Event> myEvents = eventHub.getEventsByOrganizer(organizer.getUserId());
            for (Event event : myEvents) {
                if (event.getTitle().equals(eventTitle)) {
                    return event;
                }
            }
        }
        return null;
    }
    
    /**
     * Shows detailed registration information for an event
     */
    public void viewRegistrations(Event selectedEvent) {
        if (selectedEvent == null) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Please select an event to view registrations.", 
                "No Event Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder registrationDetails = new StringBuilder();
        registrationDetails.append("=== REGISTRATION DETAILS ===\n\n");
        registrationDetails.append("Event: ").append(selectedEvent.getTitle()).append("\n");
        registrationDetails.append("Date: ").append(selectedEvent.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        registrationDetails.append("Capacity: ").append(selectedEvent.getMaxCapacity()).append("\n\n");
        
        java.util.List<com.campuseventhub.model.event.Registration> registrations = selectedEvent.getRegistrations();
        
        if (registrations == null || registrations.isEmpty()) {
            registrationDetails.append("No registrations yet.\n");
        } else {
            // Count registrations by status
            int confirmedCount = 0;
            int waitlistedCount = 0;
            java.util.List<com.campuseventhub.model.event.Registration> confirmedRegs = new java.util.ArrayList<>();
            java.util.List<com.campuseventhub.model.event.Registration> waitlistedRegs = new java.util.ArrayList<>();
            
            for (com.campuseventhub.model.event.Registration reg : registrations) {
                if (reg.getStatus() == com.campuseventhub.model.event.RegistrationStatus.CONFIRMED) {
                    confirmedCount++;
                    confirmedRegs.add(reg);
                } else if (reg.getStatus() == com.campuseventhub.model.event.RegistrationStatus.WAITLISTED) {
                    waitlistedCount++;
                    waitlistedRegs.add(reg);
                }
            }
            
            registrationDetails.append("SUMMARY:\n");
            registrationDetails.append("- Confirmed: ").append(confirmedCount).append("\n");
            registrationDetails.append("- Waitlisted: ").append(waitlistedCount).append("\n");
            registrationDetails.append("- Available Spots: ").append(selectedEvent.getAvailableSpots()).append("\n\n");
            
            // Show confirmed registrations
            if (!confirmedRegs.isEmpty()) {
                registrationDetails.append("CONFIRMED ATTENDEES:\n");
                for (com.campuseventhub.model.event.Registration reg : confirmedRegs) {
                    registrationDetails.append("• ").append(reg.getAttendeeId())
                        .append(" (Registered: ").append(reg.getRegistrationTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .append(")\n");
                }
                registrationDetails.append("\n");
            }
            
            // Show waitlisted registrations
            if (!waitlistedRegs.isEmpty()) {
                registrationDetails.append("WAITLISTED ATTENDEES:\n");
                for (com.campuseventhub.model.event.Registration reg : waitlistedRegs) {
                    registrationDetails.append("• ").append(reg.getAttendeeId())
                        .append(" (Position: #").append(reg.getWaitlistPosition())
                        .append(", Registered: ").append(reg.getRegistrationTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .append(")\n");
                }
            }
        }
        
        JTextArea textArea = new JTextArea(registrationDetails.toString());
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(600, 500));
        
        JOptionPane.showMessageDialog(parentFrame, scrollPane, 
            "Registrations: " + selectedEvent.getTitle(), JOptionPane.INFORMATION_MESSAGE);
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