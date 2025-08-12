package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendeeSchedulePanel extends JPanel {
    private EventHub eventHub;
    private Attendee attendee;
    private JTextArea scheduleArea;
    
    public AttendeeSchedulePanel(EventHub eventHub, Attendee attendee) {
        this.eventHub = eventHub;
        this.attendee = attendee;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("My Event Schedule", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        scheduleArea = new JTextArea();
        scheduleArea.setEditable(false);
        scheduleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scheduleScrollPane = new JScrollPane(scheduleArea);
        add(scheduleScrollPane, BorderLayout.CENTER);
        
        JButton refreshScheduleBtn = new JButton("Refresh Schedule");
        refreshScheduleBtn.addActionListener(e -> updateSchedule());
        add(refreshScheduleBtn, BorderLayout.SOUTH);
        
        updateSchedule();
    }
    
    public void updateSchedule() {
        StringBuilder schedule = new StringBuilder();
        schedule.append("=== MY EVENT SCHEDULE ===\n\n");
        schedule.append("Welcome, ").append(attendee.getFirstName()).append(" ").append(attendee.getLastName()).append("!\n\n");
        
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