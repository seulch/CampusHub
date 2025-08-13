package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog for cancelling events with reason input and confirmation.
 * 
 * Features:
 * - Event information display
 * - Reason input with validation
 * - Impact assessment (number of attendees affected)
 * - Confirmation workflow
 * - Automatic notification to attendees
 */
public class EventCancellationDialog extends JDialog {
    private EventHub eventHub;
    private Event event;
    private JTextArea reasonArea;
    private JLabel impactLabel;
    private boolean cancelled = false;
    
    public EventCancellationDialog(JFrame parent, EventHub eventHub, Event event) {
        super(parent, "Cancel Event - " + event.getTitle(), true);
        this.eventHub = eventHub;
        this.event = event;
        
        initializeDialog();
        setupEventHandlers();
    }
    
    private void initializeDialog() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Header with warning
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel warningIcon = new JLabel("⚠️ WARNING: EVENT CANCELLATION", JLabel.CENTER);
        warningIcon.setFont(new Font("Arial", Font.BOLD, 16));
        warningIcon.setForeground(Color.RED);
        headerPanel.add(warningIcon, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Event information
        JPanel eventInfoPanel = createEventInfoPanel();
        contentPanel.add(eventInfoPanel, BorderLayout.NORTH);
        
        // Reason input
        JPanel reasonPanel = createReasonPanel();
        contentPanel.add(reasonPanel, BorderLayout.CENTER);
        
        // Impact assessment
        JPanel impactPanel = createImpactPanel();
        contentPanel.add(impactPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton cancelEventBtn = new JButton("Cancel Event");
        JButton keepEventBtn = new JButton("Keep Event");
        
        cancelEventBtn.setBackground(Color.RED);
        cancelEventBtn.setForeground(Color.WHITE);
        cancelEventBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        keepEventBtn.setBackground(Color.GREEN);
        keepEventBtn.setForeground(Color.WHITE);
        keepEventBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        cancelEventBtn.addActionListener(e -> performCancellation());
        keepEventBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelEventBtn);
        buttonPanel.add(keepEventBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createEventInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Event Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(event.getTitle()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Date/Time:"), gbc);
        gbc.gridx = 1;
        String dateTime = event.getStartDateTime().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
            " - " + event.getEndDateTime().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        panel.add(new JLabel(dateTime), gbc);
        
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
    
    private JPanel createReasonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cancellation Reason"));
        
        JLabel instructionLabel = new JLabel("Please provide a reason for cancelling this event:");
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        reasonArea = new JTextArea(5, 30);
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
        panel.add(new JLabel("Total Affected:"), gbc);
        gbc.gridx = 1;
        impactLabel = new JLabel(String.valueOf(totalAffected));
        impactLabel.setFont(new Font("Arial", Font.BOLD, 12));
        if (totalAffected > 0) {
            impactLabel.setForeground(Color.RED);
        }
        panel.add(impactLabel, gbc);
        
        if (totalAffected > 0) {
            gbc.gridx = 0; gbc.gridy = 3;
            gbc.gridwidth = 2;
            JLabel notificationLabel = new JLabel("📧 All affected users will be notified automatically");
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
    
    private void performCancellation() {
        String reason = reasonArea.getText().trim();
        
        // Validate reason
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please provide a reason for cancelling the event.",
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
        
        // Final confirmation
        int totalAffected = (event.getRegistrations() != null ? event.getRegistrations().size() : 0) +
                           (event.getWaitlist() != null ? event.getWaitlist().size() : 0);
        
        String confirmMessage;
        if (totalAffected > 0) {
            confirmMessage = String.format(
                "Are you absolutely sure you want to cancel this event?\\n\\n" +
                "This action will:\\n" +
                "• Cancel the event permanently\\n" +
                "• Notify %d affected attendees\\n" +
                "• Release any venue bookings\\n\\n" +
                "This action cannot be undone.",
                totalAffected
            );
        } else {
            confirmMessage = "Are you sure you want to cancel this event?\\n\\nThis action cannot be undone.";
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            confirmMessage,
            "Confirm Event Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Perform the cancellation
        try {
            boolean success = eventHub.cancelEvent(event.getEventId(), reason);
            
            if (success) {
                cancelled = true;
                JOptionPane.showMessageDialog(this,
                    "Event has been successfully cancelled.\\n" +
                    (totalAffected > 0 ? "Notifications have been sent to all affected attendees." : 
                     "No attendees were registered for this event."),
                    "Event Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to cancel the event. Please try again or contact system administrator.",
                    "Cancellation Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "An error occurred while cancelling the event: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the dialog and returns whether the event was cancelled
     */
    public boolean showDialog() {
        setVisible(true);
        return cancelled;
    }
    
    /**
     * Returns whether the event was successfully cancelled
     */
    public boolean wasCancelled() {
        return cancelled;
    }
}