package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendeeRegistrationPanel extends JPanel {
    private EventHub eventHub;
    private String attendeeId;
    private DefaultListModel<String> registrationsListModel;
    private JList<String> registrationsList;
    private ActionListener onRegistrationSelected;
    private ActionListener onCancelRegistration;
    
    public AttendeeRegistrationPanel(EventHub eventHub, String attendeeId) {
        this.eventHub = eventHub;
        this.attendeeId = attendeeId;
        this.registrationsListModel = new DefaultListModel<>();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = ComponentFactory.createHeadingLabel("My Event Registrations");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        registrationsList = new JList<>(registrationsListModel);
        registrationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane registrationsScrollPane = new JScrollPane(registrationsList);
        registrationsScrollPane.setPreferredSize(new Dimension(600, 400));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewRegistrationBtn = new JButton("View Registration");
        JButton cancelRegistrationBtn = new JButton("Cancel Registration");
        
        refreshBtn.addActionListener(e -> loadMyRegistrations());
        viewRegistrationBtn.addActionListener(e -> {
            if (onRegistrationSelected != null) {
                onRegistrationSelected.actionPerformed(e);
            }
        });
        cancelRegistrationBtn.addActionListener(e -> {
            if (onCancelRegistration != null) {
                onCancelRegistration.actionPerformed(e);
            }
        });
        
        buttonsPanel.add(refreshBtn);
        buttonsPanel.add(viewRegistrationBtn);
        buttonsPanel.add(cancelRegistrationBtn);
        
        add(registrationsScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void loadMyRegistrations() {
        registrationsListModel.clear();
        
        try {
            List<Registration> myRegistrations = eventHub.getMyRegistrations(attendeeId);
            
            if (myRegistrations.isEmpty()) {
                registrationsListModel.addElement("No registrations found.");
                registrationsListModel.addElement("Register for events to see them here!");
            } else {
                for (Registration registration : myRegistrations) {
                    Event event = eventHub.searchEvents("", null, null, null).stream()
                        .filter(e -> e.getEventId().equals(registration.getEventId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (event != null) {
                        String regInfo = String.format("ID: %s - %s (%s) - %s - Status: %s", 
                            registration.getRegistrationId(),
                            event.getTitle(),
                            event.getEventType().getDisplayName(),
                            event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            registration.getStatus().getDisplayName()
                        );
                        
                        if (registration.isWaitlisted()) {
                            regInfo += " (Position: " + registration.getWaitlistPosition() + ")";
                        }
                        
                        registrationsListModel.addElement(regInfo);
                    } else {
                        registrationsListModel.addElement("ID: " + registration.getRegistrationId() + 
                            " - Event no longer available - Status: " + registration.getStatus().getDisplayName());
                    }
                }
            }
        } catch (Exception e) {
            registrationsListModel.addElement("Error loading registrations: " + e.getMessage());
        }
    }
    
    public String getSelectedRegistration() {
        return registrationsList.getSelectedValue();
    }
    
    public void setOnRegistrationSelected(ActionListener listener) {
        this.onRegistrationSelected = listener;
    }
    
    public void setOnCancelRegistration(ActionListener listener) {
        this.onCancelRegistration = listener;
    }
}