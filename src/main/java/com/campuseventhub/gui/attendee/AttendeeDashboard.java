package com.campuseventhub.gui.attendee;

import com.campuseventhub.gui.common.BaseFrame;
import com.campuseventhub.gui.common.ProfileEditingPanel;
import com.campuseventhub.model.user.Attendee;
import javax.swing.*;
import java.awt.*;

public class AttendeeDashboard extends BaseFrame {
    private Attendee attendee;
    private JTabbedPane mainTabbedPane;
    private AttendeeEventBrowser eventBrowser;
    private AttendeeRegistrationPanel registrationPanel;
    private AttendeeSchedulePanel schedulePanel;
    private ProfileEditingPanel profilePanel;
    private AttendeeActionHandler actionHandler;
    
    public AttendeeDashboard(Attendee attendee) {
        super("Campus EventHub - Student Portal");
        this.attendee = attendee;
        this.actionHandler = new AttendeeActionHandler(eventHub, attendee, this);
        
        initializeComponents();
        setupEventHandlers();
        loadData();
    }
    
    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        mainTabbedPane = new JTabbedPane();
        
        eventBrowser = new AttendeeEventBrowser(eventHub);
        registrationPanel = new AttendeeRegistrationPanel(eventHub, attendee.getUserId());
        schedulePanel = new AttendeeSchedulePanel(eventHub, attendee);
        profilePanel = new ProfileEditingPanel(eventHub);
        
        mainTabbedPane.addTab("Browse Events", eventBrowser);
        mainTabbedPane.addTab("My Registrations", registrationPanel);
        mainTabbedPane.addTab("My Schedule", schedulePanel);
        mainTabbedPane.addTab("My Profile", profilePanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        eventBrowser.setOnEventSelected(e -> actionHandler.viewEventDetails(eventBrowser.getSelectedEvent()));
        eventBrowser.setOnRegisterEvent(e -> {
            if (actionHandler.registerForEvent(eventBrowser.getSelectedEvent())) {
                eventBrowser.loadAvailableEvents();
                registrationPanel.loadMyRegistrations();
                schedulePanel.updateSchedule();
            }
        });
        
        registrationPanel.setOnRegistrationSelected(e -> 
            actionHandler.viewRegistrationDetails(registrationPanel.getSelectedRegistration()));
        registrationPanel.setOnCancelRegistration(e -> {
            if (actionHandler.cancelRegistration(registrationPanel.getSelectedRegistration())) {
                registrationPanel.loadMyRegistrations();
                eventBrowser.loadAvailableEvents();
                schedulePanel.updateSchedule();
            }
        });
    }
    
    private void loadData() {
        eventBrowser.loadAvailableEvents();
        registrationPanel.loadMyRegistrations();
    }
}