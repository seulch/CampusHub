// =============================================================================
// ATTENDEE DASHBOARD
// =============================================================================

package com.campuseventhub.gui.attendee;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.Registration;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main dashboard for attendee users.
 * 
 * Implementation Details:
 * - Event browsing and search interface
 * - Personal schedule management
 * - Registration history tracking
 * - Recommendation system display
 * - Wishlist management
 * - Notification center
 */
public class AttendeeDashboard extends BaseFrame {
    private Attendee attendee;
    private JTabbedPane mainTabbedPane;
    private JPanel browseEventsPanel;
    private JPanel myRegistrationsPanel;
    private JPanel schedulePanel;
    
    // Data models
    private DefaultListModel<String> eventsListModel;
    private DefaultListModel<String> registrationsListModel;
    
    public AttendeeDashboard(Attendee attendee) {
        super("Campus EventHub - Student Portal");
        this.attendee = attendee;
        
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
        createBrowseEventsPanel();
        createMyRegistrationsPanel();
        createSchedulePanel();
        
        // Add tabs
        mainTabbedPane.addTab("Browse Events", browseEventsPanel);
        mainTabbedPane.addTab("My Registrations", myRegistrationsPanel);
        mainTabbedPane.addTab("My Schedule", schedulePanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void createBrowseEventsPanel() {
        browseEventsPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Browse Available Events", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        browseEventsPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(20);
        JComboBox<EventType> typeFilter = new JComboBox<>();
        typeFilter.addItem(null); // "All types" option
        for (EventType type : EventType.values()) {
            typeFilter.addItem(type);
        }
        
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Type:"));
        searchPanel.add(typeFilter);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        
        // Events list
        eventsListModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsListModel);
        eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane eventsScrollPane = new JScrollPane(eventsList);
        eventsScrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewEventBtn = new JButton("View Details");
        JButton registerBtn = new JButton("Register for Event");
        
        refreshBtn.addActionListener(e -> loadAvailableEvents());
        viewEventBtn.addActionListener(e -> viewEventDetails(eventsList.getSelectedValue()));
        registerBtn.addActionListener(e -> registerForEvent(eventsList.getSelectedValue()));
        searchBtn.addActionListener(e -> searchEvents(searchField.getText().trim(), (EventType) typeFilter.getSelectedItem()));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            loadAvailableEvents();
        });
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewEventBtn);
        buttonsPanel.add(registerBtn);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(eventsScrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        browseEventsPanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    private void createMyRegistrationsPanel() {
        myRegistrationsPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("My Event Registrations", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        myRegistrationsPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Registrations list
        registrationsListModel = new DefaultListModel<>();
        JList<String> registrationsList = new JList<>(registrationsListModel);
        registrationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane registrationsScrollPane = new JScrollPane(registrationsList);
        registrationsScrollPane.setPreferredSize(new Dimension(600, 400));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewRegistrationBtn = new JButton("View Registration");
        JButton cancelRegistrationBtn = new JButton("Cancel Registration");
        
        refreshBtn.addActionListener(e -> loadMyRegistrations());
        viewRegistrationBtn.addActionListener(e -> viewRegistrationDetails(registrationsList.getSelectedValue()));
        cancelRegistrationBtn.addActionListener(e -> cancelRegistration(registrationsList.getSelectedValue()));
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewRegistrationBtn);
        buttonsPanel.add(cancelRegistrationBtn);
        
        myRegistrationsPanel.add(registrationsScrollPane, BorderLayout.CENTER);
        myRegistrationsPanel.add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void createSchedulePanel() {
        schedulePanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("My Event Schedule", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        schedulePanel.add(titleLabel, BorderLayout.NORTH);
        
        // Schedule content
        JTextArea scheduleArea = new JTextArea();
        scheduleArea.setEditable(false);
        scheduleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scheduleScrollPane = new JScrollPane(scheduleArea);
        schedulePanel.add(scheduleScrollPane, BorderLayout.CENTER);
        
        updateSchedule(scheduleArea);
        
        // Refresh button
        JButton refreshScheduleBtn = new JButton("Refresh Schedule");
        refreshScheduleBtn.addActionListener(e -> updateSchedule(scheduleArea));
        schedulePanel.add(refreshScheduleBtn, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        loadAvailableEvents();
        loadMyRegistrations();
    }
    
    private void loadAvailableEvents() {
        eventsListModel.clear();
        List<Event> events = eventHub.searchEvents("", null, null, null); // Get all events
        
        if (events.isEmpty()) {
            eventsListModel.addElement("No events available at the moment.");
        } else {
            for (Event event : events) {
                if (event.isRegistrationOpen()) {
                    String eventInfo = String.format("%s - %s (%s) - %s - Available: %d/%d", 
                        event.getTitle(),
                        event.getEventType().getDisplayName(),
                        event.getStatus().getDisplayName(),
                        event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        event.getAvailableSpots(),
                        event.getMaxCapacity()
                    );
                    eventsListModel.addElement(eventInfo);
                }
            }
            if (eventsListModel.isEmpty()) {
                eventsListModel.addElement("No events open for registration.");
            }
        }
    }
    
    private void loadMyRegistrations() {
        registrationsListModel.clear();
        // Note: This is a simplified implementation
        // In a real application, we would query registrations by attendee ID
        registrationsListModel.addElement("Registration tracking feature coming soon!");
        registrationsListModel.addElement("Your registrations will appear here once implemented.");
    }
    
    private void searchEvents(String keyword, EventType type) {
        eventsListModel.clear();
        List<Event> events = eventHub.searchEvents(keyword, type, null, null);
        
        if (events.isEmpty()) {
            eventsListModel.addElement("No events found matching your search criteria.");
        } else {
            for (Event event : events) {
                if (event.isRegistrationOpen()) {
                    String eventInfo = String.format("%s - %s (%s) - %s - Available: %d/%d", 
                        event.getTitle(),
                        event.getEventType().getDisplayName(),
                        event.getStatus().getDisplayName(),
                        event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        event.getAvailableSpots(),
                        event.getMaxCapacity()
                    );
                    eventsListModel.addElement(eventInfo);
                }
            }
            if (eventsListModel.isEmpty()) {
                eventsListModel.addElement("No events open for registration match your search.");
            }
        }
    }
    
    private void viewEventDetails(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events")) {
            JOptionPane.showMessageDialog(this, "Event Details:\n" + selectedEvent, "Event Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void registerForEvent(String selectedEvent) {
        if (selectedEvent != null && !selectedEvent.contains("No events")) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Do you want to register for this event?\n" + selectedEvent, 
                "Confirm Registration", 
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                // Extract event title from the display string (simplified approach)
                String eventTitle = selectedEvent.split(" - ")[0];
                
                // Search for the actual event object
                List<Event> allEvents = eventHub.searchEvents(eventTitle, null, null, null);
                Event targetEvent = null;
                for (Event event : allEvents) {
                    if (event.getTitle().equals(eventTitle)) {
                        targetEvent = event;
                        break;
                    }
                }
                
                if (targetEvent != null) {
                    // Try to register for the event
                    Registration registration = targetEvent.addRegistration(attendee.getUserId());
                    if (registration != null) {
                        if (registration.isConfirmed()) {
                            JOptionPane.showMessageDialog(this, 
                                "Successfully registered for: " + eventTitle + "\nRegistration ID: " + registration.getRegistrationId());
                        } else if (registration.isWaitlisted()) {
                            JOptionPane.showMessageDialog(this, 
                                "Added to waitlist for: " + eventTitle + "\nWaitlist position: " + registration.getWaitlistPosition());
                        }
                        loadMyRegistrations();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Failed to register. You may already be registered or registration is closed.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Event not found. Please refresh the event list.");
                }
            }
        }
    }
    
    private void viewRegistrationDetails(String selectedRegistration) {
        if (selectedRegistration != null) {
            JOptionPane.showMessageDialog(this, "Registration Details:\n" + selectedRegistration, "Registration Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void cancelRegistration(String selectedRegistration) {
        if (selectedRegistration != null) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel this registration?\n" + selectedRegistration, 
                "Confirm Cancellation", 
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Registration cancellation feature coming soon!");
            }
        }
    }
    
    private void updateSchedule(JTextArea scheduleArea) {
        StringBuilder schedule = new StringBuilder();
        schedule.append("=== MY EVENT SCHEDULE ===\n\n");
        schedule.append("Welcome, ").append(attendee.getFirstName()).append(" ").append(attendee.getLastName()).append("!\n\n");
        
        // Get upcoming events (this would be filtered by user's registrations in real implementation)
        List<Event> upcomingEvents = eventHub.getUpcomingEvents();
        
        if (upcomingEvents.isEmpty()) {
            schedule.append("No upcoming events found.\n");
            schedule.append("Browse available events and register to see them here!\n");
        } else {
            schedule.append("Upcoming Events (All Events - Registration filtering not yet implemented):\n\n");
            upcomingEvents.stream()
                .limit(10)
                .forEach(event -> {
                    schedule.append("• ").append(event.getTitle()).append("\n");
                    schedule.append("  Date: ").append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
                    schedule.append("  Type: ").append(event.getEventType().getDisplayName()).append("\n");
                    schedule.append("  Status: ").append(event.getStatus().getDisplayName()).append("\n\n");
                });
        }
        
        scheduleArea.setText(schedule.toString());
    }
}
