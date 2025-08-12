package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventStatus;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrganizerAnalyticsPanel extends JPanel {
    private EventHub eventHub;
    private String organizerId;
    private JTextArea analyticsArea;
    
    public OrganizerAnalyticsPanel(EventHub eventHub, String organizerId) {
        this.eventHub = eventHub;
        this.organizerId = organizerId;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Event Analytics", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        analyticsArea = new JTextArea();
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane analyticsScrollPane = new JScrollPane(analyticsArea);
        add(analyticsScrollPane, BorderLayout.CENTER);
        
        JButton refreshAnalyticsBtn = new JButton("Refresh Analytics");
        refreshAnalyticsBtn.addActionListener(e -> updateAnalytics());
        add(refreshAnalyticsBtn, BorderLayout.SOUTH);
        
        updateAnalytics();
    }
    
    public void updateAnalytics() {
        StringBuilder analytics = new StringBuilder();
        analytics.append("=== EVENT ANALYTICS DASHBOARD ===\n\n");
        
        List<Event> myEvents = eventHub.getEventsByOrganizer(organizerId);
        
        analytics.append("My Events Summary:\n");
        analytics.append("- Total Events: ").append(myEvents.size()).append("\n");
        
        if (!myEvents.isEmpty()) {
                    long publishedEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.PUBLISHED).count();
        long draftEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.DRAFT).count();
            
            analytics.append("- Published Events: ").append(publishedEvents).append("\n");
            analytics.append("- Draft Events: ").append(draftEvents).append("\n\n");
            
            analytics.append("Recent Events:\n");
            myEvents.stream()
                .limit(5)
                .forEach(event -> {
                    analytics.append("• ").append(event.getTitle())
                            .append(" (").append(event.getEventType().getDisplayName()).append(")")
                            .append(" - ").append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                            .append(" - Status: ").append(event.getStatus().getDisplayName())
                            .append("\n");
                });
        } else {
            analytics.append("- No events created yet\n");
            analytics.append("\nCreate your first event to see detailed analytics here!");
        }
        
        analyticsArea.setText(analytics.toString());
    }
}