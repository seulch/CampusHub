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
    private ActionListener onCancelEvent;
    private ActionListener onRescheduleEvent;
    
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
        JButton rescheduleEventBtn = new JButton("Reschedule");
        JButton cancelEventBtn = new JButton("Cancel Event");
        
        // Style the cancel button to be more prominent
        cancelEventBtn.setBackground(new Color(220, 53, 69));
        cancelEventBtn.setForeground(Color.WHITE);
        cancelEventBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Style the reschedule button
        rescheduleEventBtn.setBackground(new Color(255, 193, 7));
        rescheduleEventBtn.setForeground(Color.BLACK);
        rescheduleEventBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
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
        rescheduleEventBtn.addActionListener(e -> {
            if (onRescheduleEvent != null) onRescheduleEvent.actionPerformed(e);
        });
        cancelEventBtn.addActionListener(e -> {
            if (onCancelEvent != null) onCancelEvent.actionPerformed(e);
        });
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewEventBtn);
        buttonsPanel.add(editEventBtn);
        buttonsPanel.add(publishEventBtn);
        buttonsPanel.add(rescheduleEventBtn);
        buttonsPanel.add(cancelEventBtn);
        
        add(eventsScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private List<Event> currentEvents; // Store current events for selection
    
    public void loadMyEvents() {
        eventsListModel.clear();
        currentEvents = eventHub.getEventsByOrganizer(organizerId);
        
        if (currentEvents.isEmpty()) {
            eventsListModel.addElement("No events found. Create your first event!");
        } else {
            for (Event event : currentEvents) {
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
    
    /**
     * Gets the selected Event object (not just the display string)
     */
    public Event getSelectedEventObject() {
        int selectedIndex = eventsList.getSelectedIndex();
        if (selectedIndex >= 0 && currentEvents != null && selectedIndex < currentEvents.size()) {
            return currentEvents.get(selectedIndex);
        }
        return null;
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
    
    public void setOnCancelEvent(ActionListener listener) {
        this.onCancelEvent = listener;
    }
    
    public void setOnRescheduleEvent(ActionListener listener) {
        this.onRescheduleEvent = listener;
    }
}