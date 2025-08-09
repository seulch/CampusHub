// =============================================================================
// ORGANIZER DASHBOARD
// =============================================================================

package com.campuseventhub.gui.organizer;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.model.user.Organizer;
import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.model.venue.Venue;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main dashboard for organizer users.
 * 
 * Implementation Details:
 * - Tabbed interface for different functions
 * - Event management panels
 * - Analytics and reporting views
 * - Calendar integration for scheduling
 * - Real-time updates and notifications
 * - Drag-and-drop event creation
 */
public class OrganizerDashboard extends BaseFrame {
    private Organizer organizer;
    private JTabbedPane mainTabbedPane;
    private JPanel myEventsPanel;
    private JPanel createEventPanel;
    private JPanel analyticsPanel;
    private JLabel welcomeLabel;
    
    // Data models
    private DefaultListModel<String> eventsListModel;
    
    public OrganizerDashboard(Organizer organizer) {
        super("Campus EventHub - Organizer Dashboard");
        this.organizer = organizer;
        
        initializeComponents();
        loadData();
        registerListeners();
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane();
        
        // Create panels
        createMyEventsPanel();
        createEventCreationPanel();
        createAnalyticsPanel();
        
        // Add tabs
        mainTabbedPane.addTab("My Events", myEventsPanel);
        mainTabbedPane.addTab("Create Event", createEventPanel);
        mainTabbedPane.addTab("Analytics", analyticsPanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void createMyEventsPanel() {
        myEventsPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("My Events", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        myEventsPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Events list
        eventsListModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsListModel);
        eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane eventsScrollPane = new JScrollPane(eventsList);
        eventsScrollPane.setPreferredSize(new Dimension(600, 400));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewEventBtn = new JButton("View Event");
        JButton editEventBtn = new JButton("Edit Event");
        JButton publishEventBtn = new JButton("Publish Event");
        
        refreshBtn.addActionListener(e -> loadMyEvents());
        viewEventBtn.addActionListener(e -> viewEventDetails(eventsList.getSelectedValue()));
        editEventBtn.addActionListener(e -> editEvent(eventsList.getSelectedValue()));
        publishEventBtn.addActionListener(e -> publishEvent(eventsList.getSelectedValue()));
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewEventBtn);
        buttonsPanel.add(editEventBtn);
        buttonsPanel.add(publishEventBtn);
        
        myEventsPanel.add(eventsScrollPane, BorderLayout.CENTER);
        myEventsPanel.add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void createEventCreationPanel() {
        createEventPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Create New Event", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        createEventPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Event creation form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Form fields
        JTextField titleField = new JTextField(30);
        JTextArea descriptionArea = new JTextArea(4, 30);
        JComboBox<EventType> typeCombo = new JComboBox<>(EventType.values());
        JTextField capacityField = new JTextField(10);
        JTextField startDateField = new JTextField(20);
        JTextField endDateField = new JTextField(20);
        
        // Add components to form
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
        formPanel.add(new JLabel("Start Date/Time (YYYY-MM-DD HH:MM):"), gbc);
        gbc.gridx = 1;
        formPanel.add(startDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("End Date/Time (YYYY-MM-DD HH:MM):"), gbc);
        gbc.gridx = 1;
        formPanel.add(endDateField, gbc);
        
        // Create button
        JButton createBtn = new JButton("Create Event");
        createBtn.addActionListener(e -> createEvent(
            titleField.getText().trim(),
            descriptionArea.getText().trim(),
            (EventType) typeCombo.getSelectedItem(),
            capacityField.getText().trim(),
            startDateField.getText().trim(),
            endDateField.getText().trim()
        ));
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(createBtn, gbc);
        
        createEventPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add helpful text
        JLabel helpLabel = new JLabel("<html><body>Fill in the event details above. Date format: YYYY-MM-DD HH:MM (e.g., 2024-12-25 14:30)</body></html>");
        createEventPanel.add(helpLabel, BorderLayout.SOUTH);
    }
    
    private void createAnalyticsPanel() {
        analyticsPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Event Analytics", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        analyticsPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Analytics content
        JTextArea analyticsArea = new JTextArea();
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane analyticsScrollPane = new JScrollPane(analyticsArea);
        analyticsPanel.add(analyticsScrollPane, BorderLayout.CENTER);
        
        updateAnalytics(analyticsArea);
        
        // Refresh button
        JButton refreshAnalyticsBtn = new JButton("Refresh Analytics");
        refreshAnalyticsBtn.addActionListener(e -> updateAnalytics(analyticsArea));
        analyticsPanel.add(refreshAnalyticsBtn, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        loadMyEvents();
    }
    
    private void loadMyEvents() {
        eventsListModel.clear();
        List<Event> myEvents = eventHub.getEventsByOrganizer(organizer.getUserId());
        
        if (myEvents.isEmpty()) {
            eventsListModel.addElement("No events found. Create your first event!");
        } else {
            for (Event event : myEvents) {
                String eventInfo = String.format("%s - %s (%s) - %s", 
                    event.getTitle(),
                    event.getEventType().getDisplayName(),
                    event.getStatus().getDisplayName(),
                    event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
                eventsListModel.addElement(eventInfo);
            }
        }
    }
    
    private void createEvent(String title, String description, EventType type, String capacityStr, String startDateStr, String endDateStr) {
        try {
            if (title.isEmpty() || description.isEmpty() || capacityStr.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            
            int capacity = Integer.parseInt(capacityStr);
            LocalDateTime startDateTime = LocalDateTime.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endDateTime = LocalDateTime.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            if (startDateTime.isAfter(endDateTime)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time.");
                return;
            }
            
            Event event = eventHub.createEvent(title, description, type, startDateTime, endDateTime, organizer.getUserId(), null, capacity);
            
            if (event != null) {
                // Set additional event properties
                event.setMaxCapacity(capacity);
                event.setRegistrationDeadline(startDateTime.minusHours(1)); // Registration closes 1 hour before event
                
                JOptionPane.showMessageDialog(this, "Event created successfully!");
                loadMyEvents();
                mainTabbedPane.setSelectedIndex(0); // Switch to My Events tab
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create event. Please check your permissions.");
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD HH:MM");
        }
    }
    
    private void viewEventDetails(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events found")) {
            JOptionPane.showMessageDialog(this, "Event Details:\n" + selectedEvent, "Event Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void editEvent(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events found")) {
            JOptionPane.showMessageDialog(this, "Event editing feature coming soon!");
        }
    }
    
    private void publishEvent(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events found")) {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to publish this event?", "Confirm Publish", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Extract event title from the display string
                String eventTitle = selectedEvent.split(" - ")[0];
                
                // Find the event
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
                    JOptionPane.showMessageDialog(this, "Event published successfully! It is now open for registration.");
                    loadMyEvents();
                } else {
                    JOptionPane.showMessageDialog(this, "Event not found. Please refresh the event list.");
                }
            }
        }
    }
    
    private void updateAnalytics(JTextArea analyticsArea) {
        StringBuilder analytics = new StringBuilder();
        analytics.append("=== EVENT ANALYTICS FOR ").append(organizer.getUsername().toUpperCase()).append(" ===\n\n");
        
        List<Event> myEvents = eventHub.getEventsByOrganizer(organizer.getUserId());
        
        analytics.append("Event Statistics:\n");
        analytics.append("- Total Events Created: ").append(myEvents.size()).append("\n");
        
        long draftEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.DRAFT).count();
        long publishedEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.PUBLISHED).count();
        long completedEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.COMPLETED).count();
        
        analytics.append("- Draft Events: ").append(draftEvents).append("\n");
        analytics.append("- Published Events: ").append(publishedEvents).append("\n");
        analytics.append("- Completed Events: ").append(completedEvents).append("\n\n");
        
        if (!myEvents.isEmpty()) {
            analytics.append("Recent Events:\n");
            myEvents.stream()
                .limit(5)
                .forEach(event -> analytics.append("- ").append(event.getTitle())
                    .append(" (").append(event.getStatus().getDisplayName()).append(")\n"));
        } else {
            analytics.append("No events created yet.\n");
        }
        
        analyticsArea.setText(analytics.toString());
    }
}