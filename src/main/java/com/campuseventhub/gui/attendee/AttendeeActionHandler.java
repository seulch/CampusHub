package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.util.QRCodeGenerator;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    
    /**
     * Shows QR code for the selected registration
     */
    public void viewQRCode(String selectedRegistration) {
        if (selectedRegistration == null || selectedRegistration.contains("No registrations")) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a registration to view QR code!", 
                "No Registration Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Extract registration ID from the display string
            String registrationId = selectedRegistration.split(" - ")[0].replace("ID: ", "");
            
            // Get the event title for display
            String eventTitle = selectedRegistration.split(" - ")[1];
            
            // Find the registration to get event ID
            List<Registration> registrations = eventHub.getMyRegistrations(attendee.getUserId());
            Registration targetRegistration = null;
            for (Registration reg : registrations) {
                if (reg.getRegistrationId().equals(registrationId)) {
                    targetRegistration = reg;
                    break;
                }
            }
            
            if (targetRegistration == null) {
                JOptionPane.showMessageDialog(parentFrame, "Registration not found!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Generate QR code data with event and registration info
            String qrData = QRCodeGenerator.generateCheckInQRCode(
                targetRegistration.getEventId(), 
                registrationId, 
                attendee.getUserId()
            );
            
            // Try to create a real QR code image
            StringBuilder qrInfo = new StringBuilder();
            qrInfo.append("=== QR CODE FOR EVENT CHECK-IN ===\n\n");
            qrInfo.append("Event: ").append(eventTitle).append("\n");
            qrInfo.append("Registration ID: ").append(registrationId).append("\n");
            qrInfo.append("Event ID: ").append(targetRegistration.getEventId()).append("\n");
            qrInfo.append("Attendee: ").append(attendee.getFirstName()).append(" ").append(attendee.getLastName()).append("\n\n");
            
            try {
                // Try to generate actual QR code image
                BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(qrData, 200, 200);
                
                // Create a panel with both QR image and text info
                JPanel qrPanel = new JPanel(new BorderLayout());
                
                // Add QR code image
                JLabel qrImageLabel = new JLabel(new ImageIcon(qrImage));
                qrImageLabel.setHorizontalAlignment(JLabel.CENTER);
                qrPanel.add(qrImageLabel, BorderLayout.CENTER);
                
                // Add text info
                qrInfo.append("=== QR CODE DATA ===\n");
                qrInfo.append(qrData).append("\n\n");
                JTextArea textArea = new JTextArea(qrInfo.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Arial", Font.PLAIN, 11));
                textArea.setBackground(Color.WHITE);
                
                JScrollPane textScrollPane = new JScrollPane(textArea);
                textScrollPane.setPreferredSize(new Dimension(400, 150));
                
                qrPanel.add(textScrollPane, BorderLayout.SOUTH);
                qrPanel.setPreferredSize(new Dimension(450, 400));
                
                JOptionPane.showMessageDialog(parentFrame, qrPanel, 
                    "🎫 QR Code: " + eventTitle, JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception qrException) {
                // Fallback to text-based representation if QR generation fails
            
                qrInfo.append("██  ").append(String.format("%-22s", registrationId.substring(0, Math.min(22, registrationId.length())))).append("  ██\n");
                
                qrInfo.append("Note: QR image generation failed, but the text data above\n");
                qrInfo.append("contains all necessary information for check-in.");
                
                JTextArea textArea = new JTextArea(qrInfo.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                
                JOptionPane.showMessageDialog(parentFrame, scrollPane, 
                    "QR Code: " + eventTitle, JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentFrame, 
                "Error generating QR code: " + e.getMessage(),
                "QR Code Error", JOptionPane.ERROR_MESSAGE);
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