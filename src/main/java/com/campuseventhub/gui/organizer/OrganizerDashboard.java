package com.campuseventhub.gui.organizer;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.gui.common.ProfileEditingPanel;
import com.campuseventhub.model.user.Organizer;
import javax.swing.*;
import java.awt.*;

public class OrganizerDashboard extends BaseFrame {
    private Organizer organizer;
    private JTabbedPane mainTabbedPane;
    private OrganizerEventListPanel eventListPanel;
    private OrganizerEventCreationPanel eventCreationPanel;
    private OrganizerAnalyticsPanel analyticsPanel;
    private ProfileEditingPanel profilePanel;
    private OrganizerActionHandler actionHandler;
    
    public OrganizerDashboard(Organizer organizer) {
        super("Campus EventHub - Organizer Dashboard");
        this.organizer = organizer;
        this.actionHandler = new OrganizerActionHandler(eventHub, organizer, this);
        
        initializeComponents();
        setupEventHandlers();
        loadData();
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        mainTabbedPane = new JTabbedPane();
        
        eventListPanel = new OrganizerEventListPanel(eventHub, organizer.getUserId());
        eventCreationPanel = new OrganizerEventCreationPanel(eventHub);
        analyticsPanel = new OrganizerAnalyticsPanel(eventHub, organizer.getUserId());
        profilePanel = new ProfileEditingPanel(eventHub);
        
        mainTabbedPane.addTab("My Events", eventListPanel);
        mainTabbedPane.addTab("Create Event", eventCreationPanel);
        mainTabbedPane.addTab("Analytics", analyticsPanel);
        mainTabbedPane.addTab("My Profile", profilePanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        eventListPanel.setOnViewEvent(e -> 
            actionHandler.viewEventDetails(eventListPanel.getSelectedEvent()));
        
        eventListPanel.setOnEditEvent(e -> {
            actionHandler.editEvent(eventListPanel.getSelectedEventObject());
            eventListPanel.loadMyEvents(); // Refresh the list after editing
        });
        
        eventListPanel.setOnPublishEvent(e -> {
            if (actionHandler.publishEvent(eventListPanel.getSelectedEvent())) {
                eventListPanel.loadMyEvents();
                analyticsPanel.updateAnalytics();
            }
        });
        
        eventListPanel.setOnCancelEvent(e -> {
            actionHandler.cancelEvent(eventListPanel.getSelectedEventObject());
            eventListPanel.loadMyEvents(); // Refresh the list after cancellation
        });
        
        eventListPanel.setOnRescheduleEvent(e -> {
            actionHandler.rescheduleEvent(eventListPanel.getSelectedEventObject());
            eventListPanel.loadMyEvents(); // Refresh the list after rescheduling
        });
        
        eventCreationPanel.setOnCreateEvent(e -> {
            if (actionHandler.createEvent(
                eventCreationPanel.getTitle(),
                eventCreationPanel.getDescription(),
                eventCreationPanel.getEventType(),
                eventCreationPanel.getCapacity(),
                eventCreationPanel.getStartDate(),
                eventCreationPanel.getEndDate(),
                eventCreationPanel.getSelectedVenueId()
            )) {
                eventCreationPanel.clearForm();
                eventListPanel.loadMyEvents();
                analyticsPanel.updateAnalytics();
                mainTabbedPane.setSelectedIndex(0);
            }
        });
    }
    
    private void loadData() {
        eventListPanel.loadMyEvents();
    }
}