package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;

/**
 * Dialog for rescheduling events with new date/time selection and reason input.
 * 
 * Features:
 * - Current event information display
 * - New date/time selection with validation
 * - Venue availability checking for new times
 * - Reason input for rescheduling
 * - Impact assessment and attendee notification
 * - Rollback on failure
 */
public class EventReschedulingDialog extends JDialog {
    private EventHub eventHub;
    private Event event;
    private DateTimeSelector newStartDateTimeSelector;
    private DateTimeSelector newEndDateTimeSelector;
    private JTextArea reasonArea;
    private JLabel impactLabel;
    private JLabel venueStatusLabel;
    private boolean rescheduled = false;
    
    public EventReschedulingDialog(JFrame parent, EventHub eventHub, Event event) {
        super(parent, "Reschedule Event - " + event.getTitle(), true);
        this.eventHub = eventHub;
        this.event = event;
        
        initializeDialog();
        setupEventHandlers();
    }
    
    private void initializeDialog() {
        setSize(600, 550);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("📅 Reschedule Event", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 100, 200));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content in scroll pane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Current event information
        contentPanel.add(createCurrentEventInfoPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        
        // New date/time selection
        contentPanel.add(createNewDateTimePanel());
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Venue status
        contentPanel.add(createVenueStatusPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Reason input
        contentPanel.add(createReasonPanel());
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Impact assessment
        contentPanel.add(createImpactPanel());
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton rescheduleBtn = new JButton("Reschedule Event");
        JButton cancelBtn = new JButton("Cancel");
        JButton checkAvailabilityBtn = new JButton("Check Venue Availability");
        
        rescheduleBtn.setBackground(new Color(0, 120, 200));
        rescheduleBtn.setForeground(Color.WHITE);
        rescheduleBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        checkAvailabilityBtn.setBackground(new Color(255, 165, 0));
        checkAvailabilityBtn.setForeground(Color.WHITE);
        
        rescheduleBtn.addActionListener(e -> performRescheduling());
        cancelBtn.addActionListener(e -> dispose());
        checkAvailabilityBtn.addActionListener(e -> checkVenueAvailability());
        
        buttonPanel.add(checkAvailabilityBtn);
        buttonPanel.add(rescheduleBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createCurrentEventInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Current Event Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(event.getTitle()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Current Date/Time:"), gbc);
        gbc.gridx = 1;
        String currentDateTime = event.getStartDateTime().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
            " - " + event.getEndDateTime().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        JLabel dateTimeLabel = new JLabel(currentDateTime);
        dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(dateTimeLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(event.getEventType().getDisplayName()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Venue:"), gbc);
        gbc.gridx = 1;
        String venueText = event.hasVenue() ? event.getVenueName() : "No venue assigned";
        panel.add(new JLabel(venueText), gbc);
        
        return panel;
    }
    
    private JPanel createNewDateTimePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("New Date & Time"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        newStartDateTimeSelector = new DateTimeSelector();
        newEndDateTimeSelector = new DateTimeSelector();
        
        // Set default values to current event times
        newStartDateTimeSelector.setDateTime(event.getStartDateTime());
        newEndDateTimeSelector.setDateTime(event.getEndDateTime());
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("New Start Date/Time:"), gbc);
        gbc.gridx = 1;
        panel.add(newStartDateTimeSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New End Date/Time:"), gbc);
        gbc.gridx = 1;
        panel.add(newEndDateTimeSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("<html><i>Note: The new time must be in the future and end time must be after start time.</i></html>");
        noteLabel.setForeground(Color.BLUE);
        panel.add(noteLabel, gbc);
        
        return panel;
    }
    
    private JPanel createVenueStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Venue Availability Status"));
        
        venueStatusLabel = new JLabel("Click 'Check Venue Availability' to verify the new time slot");
        venueStatusLabel.setForeground(Color.GRAY);
        panel.add(venueStatusLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReasonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Reason for Rescheduling"));
        
        JLabel instructionLabel = new JLabel("Please provide a reason for rescheduling this event:");
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel noteLabel = new JLabel("<html><i>This reason will be included in the notification sent to all registered attendees.</i></html>");
        noteLabel.setForeground(Color.BLUE);
        panel.add(noteLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createImpactPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Impact Assessment"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Calculate impact
        int registeredCount = event.getRegistrations() != null ? event.getRegistrations().size() : 0;
        int waitlistCount = event.getWaitlist() != null ? event.getWaitlist().size() : 0;
        int totalAffected = registeredCount + waitlistCount;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Registered Attendees:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(registeredCount)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Waitlisted Attendees:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(waitlistCount)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total to be Notified:"), gbc);
        gbc.gridx = 1;
        impactLabel = new JLabel(String.valueOf(totalAffected));
        impactLabel.setFont(new Font("Arial", Font.BOLD, 12));
        if (totalAffected > 0) {
            impactLabel.setForeground(new Color(0, 100, 200));
        }
        panel.add(impactLabel, gbc);
        
        if (totalAffected > 0) {
            gbc.gridx = 0; gbc.gridy = 3;
            gbc.gridwidth = 2;
            JLabel notificationLabel = new JLabel("📧 All affected users will be notified about the new schedule");
            notificationLabel.setForeground(Color.BLUE);
            panel.add(notificationLabel, gbc);
        }
        
        return panel;
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
    
    private void checkVenueAvailability() {
        LocalDateTime newStartTime = newStartDateTimeSelector.getDateTime();
        LocalDateTime newEndTime = newEndDateTimeSelector.getDateTime();
        
        if (newStartTime == null || newEndTime == null) {
            venueStatusLabel.setText("❌ Please select valid start and end times");
            venueStatusLabel.setForeground(Color.RED);
            return;
        }
        
        if (!newStartTime.isBefore(newEndTime)) {
            venueStatusLabel.setText("❌ Start time must be before end time");
            venueStatusLabel.setForeground(Color.RED);
            return;
        }
        
        if (newStartTime.isBefore(LocalDateTime.now())) {
            venueStatusLabel.setText("❌ Cannot reschedule to a past date");
            venueStatusLabel.setForeground(Color.RED);
            return;
        }
        
        if (event.hasVenue()) {
            // Check if the venue is available for the new time
            try {
                boolean canReschedule = eventHub.canRescheduleEvent(event.getEventId());
                if (canReschedule) {
                    venueStatusLabel.setText("✅ Venue appears to be available for the new time slot");
                    venueStatusLabel.setForeground(new Color(0, 150, 0));
                } else {
                    venueStatusLabel.setText("❌ Event cannot be rescheduled (may have already started or be completed)");
                    venueStatusLabel.setForeground(Color.RED);
                }
            } catch (Exception e) {
                venueStatusLabel.setText("⚠️ Unable to verify venue availability");
                venueStatusLabel.setForeground(Color.ORANGE);
            }
        } else {
            venueStatusLabel.setText("ℹ️ No venue assigned - time change will not affect venue bookings");
            venueStatusLabel.setForeground(Color.BLUE);
        }
    }
    
    private void performRescheduling() {
        LocalDateTime newStartTime = newStartDateTimeSelector.getDateTime();
        LocalDateTime newEndTime = newEndDateTimeSelector.getDateTime();
        String reason = reasonArea.getText().trim();
        
        // Validate inputs
        if (newStartTime == null || newEndTime == null) {
            JOptionPane.showMessageDialog(this,
                "Please select valid start and end times.",
                "Invalid Times",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!newStartTime.isBefore(newEndTime)) {
            JOptionPane.showMessageDialog(this,
                "Start time must be before end time.",
                "Invalid Time Range",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (newStartTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                "Cannot reschedule to a past date.",
                "Invalid Date",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please provide a reason for rescheduling the event.",
                "Reason Required",
                JOptionPane.WARNING_MESSAGE);
            reasonArea.requestFocus();
            return;
        }
        
        if (reason.length() < 10) {
            JOptionPane.showMessageDialog(this,
                "Please provide a more detailed reason (at least 10 characters).",
                "Reason Too Short",
                JOptionPane.WARNING_MESSAGE);
            reasonArea.requestFocus();
            return;
        }
        
        // Check if times actually changed
        if (newStartTime.equals(event.getStartDateTime()) && newEndTime.equals(event.getEndDateTime())) {
            JOptionPane.showMessageDialog(this,
                "The new times are the same as the current times. No rescheduling needed.",
                "No Change",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Final confirmation
        int totalAffected = (event.getRegistrations() != null ? event.getRegistrations().size() : 0) +
                           (event.getWaitlist() != null ? event.getWaitlist().size() : 0);
        
        String confirmMessage = String.format(
            "Are you sure you want to reschedule this event?\\n\\n" +
            "Current time: %s - %s\\n" +
            "New time: %s - %s\\n\\n" +
            (totalAffected > 0 ? "This will notify %d affected attendees.\\n\\n" : "") +
            "This action cannot be undone.",
            event.getStartDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            event.getEndDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            newStartTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            newEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            totalAffected
        );
        
        int choice = JOptionPane.showConfirmDialog(this,
            confirmMessage,
            "Confirm Event Rescheduling",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Perform the rescheduling
        try {
            boolean success = eventHub.rescheduleEvent(event.getEventId(), newStartTime, newEndTime, reason);
            
            if (success) {
                rescheduled = true;
                JOptionPane.showMessageDialog(this,
                    "Event has been successfully rescheduled.\\n" +
                    (totalAffected > 0 ? "Notifications have been sent to all affected attendees." : 
                     "No attendees were registered for this event."),
                    "Event Rescheduled",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reschedule the event. The venue may not be available for the new time slot.",
                    "Rescheduling Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "An error occurred while rescheduling the event: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the dialog and returns whether the event was rescheduled
     */
    public boolean showDialog() {
        setVisible(true);
        return rescheduled;
    }
    
    /**
     * Returns whether the event was successfully rescheduled
     */
    public boolean wasRescheduled() {
        return rescheduled;
    }
}