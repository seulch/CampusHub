package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.EventType;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

public class OrganizerEventCreationPanel extends JPanel {
    private EventHub eventHub;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<EventType> typeCombo;
    private JTextField capacityField;
    private DateTimeSelector startDateTimeSelector;
    private DateTimeSelector endDateTimeSelector;
    private VenueSelectionPanel venueSelectionPanel;
    private ActionListener onCreateEvent;
    
    public OrganizerEventCreationPanel(EventHub eventHub) {
        this.eventHub = eventHub;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Create New Event", SwingConstants.CENTER);
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
        
        // Set end date to 1 hour after start date by default
        LocalDateTime defaultEnd = startDateTimeSelector.getDateTime();
        if (defaultEnd != null) {
            endDateTimeSelector.setDateTime(defaultEnd.plusHours(1));
        }
        
        // Add listeners for capacity changes to update venue availability
        capacityField.addActionListener(e -> updateVenueAvailability());
        
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
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(updateVenuesBtn, gbc);
        
        JButton createBtn = new JButton("Create Event");
        createBtn.addActionListener(e -> {
            if (onCreateEvent != null) onCreateEvent.actionPerformed(e);
        });
        
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(createBtn, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JLabel helpLabel = new JLabel("<html><body>Fill in the event details above. Select dates and times using the dropdown menus.</body></html>");
        add(helpLabel, BorderLayout.SOUTH);
    }
    
    public String getTitle() { return titleField.getText().trim(); }
    public String getDescription() { return descriptionArea.getText().trim(); }
    public EventType getEventType() { return (EventType) typeCombo.getSelectedItem(); }
    public String getCapacity() { return capacityField.getText().trim(); }
    public LocalDateTime getStartDate() { return startDateTimeSelector.getDateTime(); }
    public LocalDateTime getEndDate() { return endDateTimeSelector.getDateTime(); }
    public String getSelectedVenueId() { return venueSelectionPanel.getSelectedVenueId(); }
    
    public void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        typeCombo.setSelectedIndex(0);
        capacityField.setText("");
        startDateTimeSelector.clear();
        endDateTimeSelector.clear();
        venueSelectionPanel.setSelectedVenue(null);
    }
    
    /**
     * Updates venue availability when date/time or capacity changes
     */
    private void updateVenueAvailability() {
        LocalDateTime start = startDateTimeSelector.getDateTime();
        LocalDateTime end = endDateTimeSelector.getDateTime();
        
        int capacity = 0;
        try {
            String capacityText = capacityField.getText().trim();
            if (!capacityText.isEmpty()) {
                capacity = Integer.parseInt(capacityText);
            }
        } catch (NumberFormatException e) {
            // Invalid capacity, will be caught during validation
        }
        
        if (start != null && end != null) {
            venueSelectionPanel.updateAvailability(start, end, capacity);
        }
    }
    
    /**
     * Validates the form including venue selection
     */
    public boolean isFormValid() {
        if (getTitle().isEmpty() || getDescription().isEmpty() || 
            getCapacity().isEmpty() || getStartDate() == null || getEndDate() == null) {
            return false;
        }
        
        return venueSelectionPanel.isValidSelection();
    }
    
    /**
     * Gets validation error message
     */
    public String getValidationError() {
        if (getTitle().isEmpty()) return "Event title is required";
        if (getDescription().isEmpty()) return "Event description is required";
        if (getCapacity().isEmpty()) return "Event capacity is required";
        if (getStartDate() == null) return "Start date/time is required";
        if (getEndDate() == null) return "End date/time is required";
        if (getSelectedVenueId() == null) return "Venue selection is required";
        if (!venueSelectionPanel.isValidSelection()) return "Selected venue is not available or has insufficient capacity";
        
        return null;
    }
    
    public void setOnCreateEvent(ActionListener listener) {
        this.onCreateEvent = listener;
    }
}