package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.util.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

/**
 * Panel for editing existing events with pre-populated fields and validation.
 * 
 * Features:
 * - Pre-populates all fields from existing event data
 * - Validates changes and prevents invalid modifications
 * - Handles venue changes with conflict detection
 * - Provides rollback functionality for failed updates
 * - Maintains event history for audit purposes
 */
public class EventEditingPanel extends JPanel {
    private EventHub eventHub;
    private Event originalEvent;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<EventType> typeCombo;
    private JTextField capacityField;
    private DateTimeSelector startDateTimeSelector;
    private DateTimeSelector endDateTimeSelector;
    private VenueSelectionPanel venueSelectionPanel;
    private ActionListener onSaveChanges;
    private ActionListener onCancel;
    
    public EventEditingPanel(EventHub eventHub, Event event) {
        this.eventHub = eventHub;
        this.originalEvent = event;
        initializeComponents();
        populateFields();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Edit Event: " + originalEvent.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        typeCombo = new JComboBox<>(EventType.values());
        capacityField = new JTextField(10);
        startDateTimeSelector = new DateTimeSelector();
        endDateTimeSelector = new DateTimeSelector();
        venueSelectionPanel = new VenueSelectionPanel(eventHub);
        
        // Add listeners for capacity/time changes to update venue availability
        capacityField.addActionListener(e -> updateVenueAvailability());
        
        // Form layout
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Event Title:"), gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Event Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(capacityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Start Date/Time:"), gbc);
        gbc.gridx = 1;
        formPanel.add(startDateTimeSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("End Date/Time:"), gbc);
        gbc.gridx = 1;
        formPanel.add(endDateTimeSelector, gbc);
        
        // Add venue selection
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(venueSelectionPanel, gbc);
        
        // Add button to update venue availability
        JButton updateVenuesBtn = new JButton("Update Venue Availability");
        updateVenuesBtn.addActionListener(e -> updateVenueAvailability());
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(updateVenuesBtn, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            if (onSaveChanges != null) onSaveChanges.actionPerformed(e);
        });
        
        cancelBtn.addActionListener(e -> {
            if (onCancel != null) onCancel.actionPerformed(e);
        });
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Pre-populates all form fields with data from the original event
     */
    private void populateFields() {
        titleField.setText(originalEvent.getTitle());
        descriptionArea.setText(originalEvent.getDescription());
        typeCombo.setSelectedItem(originalEvent.getEventType());
        capacityField.setText(String.valueOf(originalEvent.getMaxCapacity()));
        startDateTimeSelector.setDateTime(originalEvent.getStartDateTime());
        endDateTimeSelector.setDateTime(originalEvent.getEndDateTime());
        
        // Set venue selection if event has a venue
        if (originalEvent.hasVenue()) {
            venueSelectionPanel.setSelectedVenue(originalEvent.getVenueId());
        }
        
        // Update venue availability based on current settings
        updateVenueAvailability();
    }
    
    /**
     * Updates venue availability based on current time and capacity settings
     */
    private void updateVenueAvailability() {
        LocalDateTime startTime = startDateTimeSelector.getDateTime();
        LocalDateTime endTime = endDateTimeSelector.getDateTime();
        String capacityText = capacityField.getText().trim();
        
        if (startTime != null && endTime != null && !capacityText.isEmpty()) {
            try {
                int capacity = Integer.parseInt(capacityText);
                venueSelectionPanel.updateAvailability(startTime, endTime, capacity);
            } catch (NumberFormatException e) {
                // Invalid capacity - show warning
                JLabel warningLabel = new JLabel("Invalid capacity format");
                warningLabel.setForeground(Color.RED);
                // Could add this to a status area
            }
        }
    }
    
    /**
     * Validates all form inputs and returns validation errors if any
     */
    public String getValidationError() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String capacityText = capacityField.getText().trim();
        LocalDateTime startTime = startDateTimeSelector.getDateTime();
        LocalDateTime endTime = endDateTimeSelector.getDateTime();
        
        // Title validation
        if (!ValidationUtil.isValidEventTitle(title)) {
            return "Event title must be 3-100 characters and contain only letters, numbers, spaces, and basic punctuation";
        }
        
        // Description validation
        if (description.isEmpty()) {
            return "Event description cannot be empty";
        }
        
        // Capacity validation
        try {
            int capacity = Integer.parseInt(capacityText);
            if (capacity <= 0 || capacity > 10000) {
                return "Capacity must be between 1 and 10000";
            }
            
            // Check if reducing capacity below current registrations
            int currentRegistrations = originalEvent.getRegistrations() != null ? 
                originalEvent.getRegistrations().size() : 0;
            if (capacity < currentRegistrations) {
                return String.format("Cannot reduce capacity to %d as there are already %d registered attendees", 
                    capacity, currentRegistrations);
            }
        } catch (NumberFormatException e) {
            return "Capacity must be a valid number";
        }
        
        // Time validation
        if (startTime == null || endTime == null) {
            return "Please select valid start and end times";
        }
        
        String timeError = ValidationUtil.validateTimeRange(startTime, endTime);
        if (timeError != null) {
            return timeError;
        }
        
        // Check if event has already started and trying to change start time
        if (originalEvent.getStartDateTime().isBefore(LocalDateTime.now()) && 
            !startTime.equals(originalEvent.getStartDateTime())) {
            return "Cannot change start time for events that have already started";
        }
        
        return null; // No validation errors
    }
    
    /**
     * Checks if any changes were made to the event
     */
    public boolean hasChanges() {
        String newTitle = titleField.getText().trim();
        String newDescription = descriptionArea.getText().trim();
        EventType newType = (EventType) typeCombo.getSelectedItem();
        String newCapacityText = capacityField.getText().trim();
        LocalDateTime newStartTime = startDateTimeSelector.getDateTime();
        LocalDateTime newEndTime = endDateTimeSelector.getDateTime();
        String newVenueId = venueSelectionPanel.getSelectedVenueId();
        
        // Compare with original values
        if (!newTitle.equals(originalEvent.getTitle())) return true;
        if (!newDescription.equals(originalEvent.getDescription())) return true;
        if (newType != originalEvent.getEventType()) return true;
        
        try {
            int newCapacity = Integer.parseInt(newCapacityText);
            if (newCapacity != originalEvent.getMaxCapacity()) return true;
        } catch (NumberFormatException e) {
            return true; // Invalid capacity is a change
        }
        
        if (newStartTime != null && !newStartTime.equals(originalEvent.getStartDateTime())) return true;
        if (newEndTime != null && !newEndTime.equals(originalEvent.getEndDateTime())) return true;
        
        String originalVenueId = originalEvent.hasVenue() ? originalEvent.getVenueId() : null;
        if (!java.util.Objects.equals(newVenueId, originalVenueId)) return true;
        
        return false;
    }
    
    // Getters for form values
    public String getTitle() { return titleField.getText().trim(); }
    public String getDescription() { return descriptionArea.getText().trim(); }
    public EventType getEventType() { return (EventType) typeCombo.getSelectedItem(); }
    public int getCapacity() { return Integer.parseInt(capacityField.getText().trim()); }
    public LocalDateTime getStartDate() { return startDateTimeSelector.getDateTime(); }
    public LocalDateTime getEndDate() { return endDateTimeSelector.getDateTime(); }
    public String getSelectedVenueId() { return venueSelectionPanel.getSelectedVenueId(); }
    public Event getOriginalEvent() { return originalEvent; }
    
    // Event listeners
    public void setOnSaveChanges(ActionListener listener) { this.onSaveChanges = listener; }
    public void setOnCancel(ActionListener listener) { this.onCancel = listener; }
}