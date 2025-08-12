package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.util.List;

public class AttendeeActionHandler {
    private EventHub eventHub;
    private Attendee attendee;
    private JFrame parentFrame;
    
    public AttendeeActionHandler(EventHub eventHub, Attendee attendee, JFrame parentFrame) {
        this.eventHub = eventHub;
        this.attendee = attendee;
        this.parentFrame = parentFrame;
    }
    
    public void viewEventDetails(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events")) {
            JOptionPane.showMessageDialog(parentFrame, "Event Details:\n" + selectedEvent, 
                "Event Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean registerForEvent(String selectedEvent) {
        if (selectedEvent == null || selectedEvent.contains("No events")) {
            return false;
        }
        
        int choice = JOptionPane.showConfirmDialog(parentFrame, 
            "Do you want to register for this event?\n" + selectedEvent, 
            "Confirm Registration", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }
        
        String eventTitle = selectedEvent.split(" - ")[0];
        List<Event> allEvents = eventHub.searchEvents(eventTitle, null, null, null);
        Event targetEvent = null;
        for (Event event : allEvents) {
            if (event.getTitle().equals(eventTitle)) {
                targetEvent = event;
                break;
            }
        }
        
        if (targetEvent == null) {
            JOptionPane.showMessageDialog(parentFrame, "Event not found. Please refresh the event list.",
                "Event Not Found", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            Registration registration = eventHub.registerForEvent(
                attendee.getUserId(), 
                targetEvent.getEventId()
            );
            
            if (registration != null) {
                if (registration.isConfirmed()) {
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Successfully registered for: " + eventTitle + "\n" +
                        "Registration ID: " + registration.getRegistrationId() + "\n" +
                        "Status: " + registration.getStatus().getDisplayName(),
                        "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                } else if (registration.isWaitlisted()) {
                    JOptionPane.showMessageDialog(parentFrame, 
                        "Added to waitlist for: " + eventTitle + "\n" +
                        "Registration ID: " + registration.getRegistrationId() + "\n" +
                        "Waitlist position: " + registration.getWaitlistPosition(),
                        "Added to Waitlist", JOptionPane.INFORMATION_MESSAGE);
                }
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Registration failed! You may already be registered for this event, " +
                    "or the event may be full with no waitlist available.",
                    "Registration Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Error during registration: " + e.getMessage(),
                "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    public void viewRegistrationDetails(String selectedRegistration) {
        if (selectedRegistration != null) {
            JOptionPane.showMessageDialog(parentFrame, "Registration Details:\n" + selectedRegistration, 
                "Registration Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean cancelRegistration(String selectedRegistration) {
        if (selectedRegistration == null || selectedRegistration.contains("No registrations")) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a registration to cancel!", 
                "No Registration Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int choice = JOptionPane.showConfirmDialog(parentFrame, 
            "Are you sure you want to cancel this registration?\n" + selectedRegistration, 
            "Confirm Cancellation", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            String registrationId = selectedRegistration.split(" - ")[0].replace("ID: ", "");
            
            String reason = JOptionPane.showInputDialog(parentFrame, 
                "Please provide a reason for cancellation (optional):",
                "Cancellation Reason",
                JOptionPane.QUESTION_MESSAGE);
            
            if (reason == null) {
                reason = "User requested cancellation";
            }
            
            boolean cancelled = eventHub.cancelEventRegistration(registrationId, reason);
            
            if (cancelled) {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Registration cancelled successfully!",
                    "Cancellation Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, 
                    "Failed to cancel registration. The registration may no longer be valid.",
                    "Cancellation Failed", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Error during cancellation: " + e.getMessage(),
                "Cancellation Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}