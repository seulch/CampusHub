package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
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
    private ActionListener onViewRegistrations;
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
        JButton refreshBtn = ComponentFactory.createStandardButton("Refresh");
        JButton viewEventBtn = ComponentFactory.createStandardButton("View Event");
        JButton viewRegistrationsBtn = ComponentFactory.createPrimaryButton("View Registrations");
        JButton editEventBtn = ComponentFactory.createStandardButton("Edit Event");
        JButton publishEventBtn = ComponentFactory.createSuccessButton("Publish Event");
        JButton rescheduleEventBtn = ComponentFactory.createWarningButton("Reschedule");
        JButton cancelEventBtn = ComponentFactory.createErrorButton("Cancel Event");
        
        refreshBtn.addActionListener(e -> loadMyEvents());
        viewEventBtn.addActionListener(e -> {
            if (onViewEvent != null) onViewEvent.actionPerformed(e);
        });
        viewRegistrationsBtn.addActionListener(e -> {
            if (onViewRegistrations != null) onViewRegistrations.actionPerformed(e);
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
        buttonsPanel.add(viewRegistrationsBtn);
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
                // Get confirmed registration count
                int confirmedCount = 0;
                if (event.getRegistrations() != null) {
                    for (Registration reg : event.getRegistrations()) {
                        if (reg.getStatus() == RegistrationStatus.CONFIRMED) {
                            confirmedCount++;
                        }
                    }
                }
                int maxCapacity = event.getMaxCapacity();
                
                String eventInfo = String.format("%s - %s (%s) - %s - Registered: %d/%d", 
                    event.getTitle(),
                    event.getEventType().getDisplayName(),
                    event.getStatus().getDisplayName(),
                    event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    confirmedCount,
                    maxCapacity
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
    
    public void setOnViewRegistrations(ActionListener listener) {
        this.onViewRegistrations = listener;
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