package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrganizerEventListPanel extends JPanel {
    private EventHub eventHub;
    private String organizerId;
    private DefaultListModel<String> eventsListModel;
    private JList<String> eventsList;
    private ActionListener onViewEvent;
    private ActionListener onEditEvent;
    private ActionListener onPublishEvent;
    
    public OrganizerEventListPanel(EventHub eventHub, String organizerId) {
        this.eventHub = eventHub;
        this.organizerId = organizerId;
        this.eventsListModel = new DefaultListModel<>();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("My Events", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        eventsList = new JList<>(eventsListModel);
        eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane eventsScrollPane = new JScrollPane(eventsList);
        eventsScrollPane.setPreferredSize(new Dimension(600, 400));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewEventBtn = new JButton("View Event");
        JButton editEventBtn = new JButton("Edit Event");
        JButton publishEventBtn = new JButton("Publish Event");
        
        refreshBtn.addActionListener(e -> loadMyEvents());
        viewEventBtn.addActionListener(e -> {
            if (onViewEvent != null) onViewEvent.actionPerformed(e);
        });
        editEventBtn.addActionListener(e -> {
            if (onEditEvent != null) onEditEvent.actionPerformed(e);
        });
        publishEventBtn.addActionListener(e -> {
            if (onPublishEvent != null) onPublishEvent.actionPerformed(e);
        });
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewEventBtn);
        buttonsPanel.add(editEventBtn);
        buttonsPanel.add(publishEventBtn);
        
        add(eventsScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void loadMyEvents() {
        eventsListModel.clear();
        List<Event> myEvents = eventHub.getEventsByOrganizer(organizerId);
        
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
    
    public String getSelectedEvent() {
        return eventsList.getSelectedValue();
    }
    
    public void setOnViewEvent(ActionListener listener) {
        this.onViewEvent = listener;
    }
    
    public void setOnEditEvent(ActionListener listener) {
        this.onEditEvent = listener;
    }
    
    public void setOnPublishEvent(ActionListener listener) {
        this.onPublishEvent = listener;
    }
}