package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for editing events with comprehensive validation and conflict handling.
 * 
 * Features:
 * - Modal dialog with proper event editing workflow
 * - Validation before saving changes
 * - Confirmation for unsaved changes
 * - Rollback capability on errors
 * - Integration with venue booking system
 * - Notification system for affected attendees (when applicable)
 */
public class EventEditingDialog extends JDialog {
    private EventHub eventHub;
    private Event originalEvent;
    private EventEditingPanel editingPanel;
    private boolean changesSaved = false;
    
    public EventEditingDialog(JFrame parent, EventHub eventHub, Event event) {
        super(parent, "Edit Event - " + event.getTitle(), true);
        this.eventHub = eventHub;
        this.originalEvent = event;
        
        initializeDialog();
        setupEventHandlers();
    }
    
    private void initializeDialog() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        editingPanel = new EventEditingPanel(eventHub, originalEvent);
        add(editingPanel, BorderLayout.CENTER);
        
        // Set up panel event handlers
        editingPanel.setOnSaveChanges(e -> saveChanges());
        editingPanel.setOnCancel(e -> cancelEditing());
    }
    
    private void setupEventHandlers() {
        // Handle window closing with unsaved changes check
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelEditing();
            }
        });
    }
    
    /**
     * Validates and saves changes to the event
     */
    private void saveChanges() {
        // Check if any changes were made
        if (!editingPanel.hasChanges()) {
            JOptionPane.showMessageDialog(this, 
                "No changes detected.", 
                "No Changes", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Validate all inputs
        String validationError = editingPanel.getValidationError();
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, 
                validationError, 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm changes with user
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to save these changes?\\n" +
            "This may affect registered attendees if you change venue or time.",
            "Confirm Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Prepare update data
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", editingPanel.getTitle());
            updates.put("description", editingPanel.getDescription());
            updates.put("eventType", editingPanel.getEventType());
            updates.put("maxCapacity", editingPanel.getCapacity());
            
            // Handle time changes
            if (!editingPanel.getStartDate().equals(originalEvent.getStartDateTime())) {
                updates.put("startDateTime", editingPanel.getStartDate());
            }
            if (!editingPanel.getEndDate().equals(originalEvent.getEndDateTime())) {
                updates.put("endDateTime", editingPanel.getEndDate());
            }
            
            // Handle venue changes
            String newVenueId = editingPanel.getSelectedVenueId();
            String originalVenueId = originalEvent.hasVenue() ? originalEvent.getVenueId() : null;
            
            boolean venueChanged = !java.util.Objects.equals(newVenueId, originalVenueId);
            
            if (venueChanged) {
                // Handle venue change through EventHub for proper validation
                if (newVenueId != null && !newVenueId.isEmpty()) {
                    // Change to new venue
                    boolean venueChangeSuccess = eventHub.changeEventVenue(
                        originalEvent.getEventId(), 
                        newVenueId
                    );
                    
                    if (!venueChangeSuccess) {
                        JOptionPane.showMessageDialog(this,
                            "Failed to change venue. The selected venue may not be available for the specified time.",
                            "Venue Change Failed",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    // Remove venue assignment
                    boolean venueCancelSuccess = eventHub.cancelEventVenueBooking(
                        originalEvent.getEventId()
                    );
                    
                    if (!venueCancelSuccess) {
                        JOptionPane.showMessageDialog(this,
                            "Failed to remove venue assignment.",
                            "Venue Removal Failed",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            
            // Apply other updates
            boolean updateSuccess = eventHub.getEventManager().updateEvent(
                originalEvent.getEventId(), 
                updates
            );
            
            if (updateSuccess) {
                changesSaved = true;
                
                // Show success message with details of what changed
                StringBuilder message = new StringBuilder("Event updated successfully!\\n\\nChanges made:\\n");
                
                if (!editingPanel.getTitle().equals(originalEvent.getTitle())) {
                    message.append("• Title updated\\n");
                }
                if (!editingPanel.getDescription().equals(originalEvent.getDescription())) {
                    message.append("• Description updated\\n");
                }
                if (editingPanel.getEventType() != originalEvent.getEventType()) {
                    message.append("• Event type updated\\n");
                }
                if (editingPanel.getCapacity() != originalEvent.getMaxCapacity()) {
                    message.append("• Capacity updated\\n");
                }
                if (!editingPanel.getStartDate().equals(originalEvent.getStartDateTime())) {
                    message.append("• Start time updated\\n");
                }
                if (!editingPanel.getEndDate().equals(originalEvent.getEndDateTime())) {
                    message.append("• End time updated\\n");
                }
                if (venueChanged) {
                    message.append("• Venue assignment updated\\n");
                }
                
                JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update event. Please try again.",
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "An error occurred while updating the event: " + e.getMessage(),
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles cancel operation with unsaved changes check
     */
    private void cancelEditing() {
        if (editingPanel.hasChanges()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Are you sure you want to cancel?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        dispose();
    }
    
    /**
     * Returns whether changes were successfully saved
     */
    public boolean wasChangesSaved() {
        return changesSaved;
    }
    
    /**
     * Shows the dialog and returns whether changes were saved
     */
    public boolean showDialog() {
        setVisible(true);
        return changesSaved;
    }
}