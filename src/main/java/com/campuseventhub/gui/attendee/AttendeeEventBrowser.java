package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendeeEventBrowser extends JPanel {
    private EventHub eventHub;
    private DefaultListModel<String> eventsListModel;
    private JList<String> eventsList;
    private JTextField searchField;
    private JComboBox<EventType> typeFilter;
    private ActionListener onEventSelected;
    private ActionListener onRegisterEvent;
    
    public AttendeeEventBrowser(EventHub eventHub) {
        this.eventHub = eventHub;
        this.eventsListModel = new DefaultListModel<>();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Browse Available Events", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        typeFilter = new JComboBox<>();
        typeFilter.addItem(null);
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
        
        eventsList = new JList<>(eventsListModel);
        eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane eventsScrollPane = new JScrollPane(eventsList);
        eventsScrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewEventBtn = new JButton("View Details");
        JButton registerBtn = new JButton("Register for Event");
        
        refreshBtn.addActionListener(e -> loadAvailableEvents());
        viewEventBtn.addActionListener(e -> {
            if (onEventSelected != null) {
                onEventSelected.actionPerformed(e);
            }
        });
        registerBtn.addActionListener(e -> {
            if (onRegisterEvent != null) {
                onRegisterEvent.actionPerformed(e);
            }
        });
        
        searchBtn.addActionListener(e -> searchEvents(searchField.getText().trim(), (EventType) typeFilter.getSelectedItem()));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            loadAvailableEvents();
        });
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewEventBtn);
        buttonsPanel.add(registerBtn);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(eventsScrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    public void loadAvailableEvents() {
        eventsListModel.clear();
        List<Event> events = eventHub.searchEvents("", null, null, null);
        
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
    
    public String getSelectedEvent() {
        return eventsList.getSelectedValue();
    }
    
    public void setOnEventSelected(ActionListener listener) {
        this.onEventSelected = listener;
    }
    
    public void setOnRegisterEvent(ActionListener listener) {
        this.onRegisterEvent = listener;
    }
}