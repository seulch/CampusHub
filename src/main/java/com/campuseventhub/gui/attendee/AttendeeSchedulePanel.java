package com.campuseventhub.gui.attendee;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.model.user.Attendee;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
        
        JLabel titleLabel = ComponentFactory.createHeadingLabel("My Event Schedule");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        scheduleArea = ComponentFactory.createStandardTextArea();
        
        JScrollPane scheduleScrollPane = new JScrollPane(scheduleArea);
        add(scheduleScrollPane, BorderLayout.CENTER);
        
        JButton refreshScheduleBtn = ComponentFactory.createStandardButton("Refresh Schedule");
        refreshScheduleBtn.addActionListener(e -> updateSchedule());
        add(refreshScheduleBtn, BorderLayout.SOUTH);
        
        updateSchedule();
    }
    
    public void updateSchedule() {
        StringBuilder schedule = new StringBuilder();
        schedule.append("=== MY EVENT SCHEDULE ===\n\n");
        schedule.append("Welcome, ").append(attendee.getFirstName()).append(" ").append(attendee.getLastName()).append("!\n\n");
        
        // Get attendee's registered events only
        List<Registration> myRegistrations = eventHub.getRegistrationsByAttendee(attendee.getUserId());
        
        if (myRegistrations.isEmpty()) {
            schedule.append("No upcoming events in your schedule.\n");
            schedule.append("Browse available events and register to see them here!\n");
        } else {
            schedule.append("Your Registered Events:\n\n");
            
            // Group by registration status
            List<Registration> confirmedRegs = myRegistrations.stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.CONFIRMED)
                .collect(Collectors.toList());
            
            List<Registration> waitlistedRegs = myRegistrations.stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.WAITLISTED)
                .collect(Collectors.toList());
            
            // Show confirmed registrations
            if (!confirmedRegs.isEmpty()) {
                schedule.append("✓ CONFIRMED EVENTS:\n");
                for (Registration reg : confirmedRegs) {
                    Event event = eventHub.getEventById(reg.getEventId());
                    if (event != null) {
                        String statusIcon = "";
                        String statusText = "";
                        
                        switch (event.getStatus()) {
                            case CANCELLED:
                                statusIcon = "⚠️ ";
                                statusText = " [CANCELLED]";
                                break;
                            case DRAFT:
                                statusIcon = "⏳ ";
                                statusText = " [RESCHEDULED]";
                                break;
                            case COMPLETED:
                                statusIcon = "✅ ";
                                statusText = " [COMPLETED]";
                                break;
                            default:
                                statusIcon = "• ";
                                break;
                        }
                        
                        schedule.append(statusIcon).append(event.getTitle()).append(statusText).append("\n");
                        schedule.append("  Date: ").append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
                        schedule.append("  Type: ").append(event.getEventType().getDisplayName()).append("\n");
                        schedule.append("  Status: ").append(event.getStatus().getDisplayName()).append("\n");
                        if (event.getVenue() != null) {
                            schedule.append("  Venue: ").append(event.getVenue().getName()).append("\n");
                        }
                        schedule.append("  Registration: ").append(reg.getRegistrationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
                    }
                }
            }
            
            // Show waitlisted registrations
            if (!waitlistedRegs.isEmpty()) {
                schedule.append("⏳ WAITLISTED EVENTS:\n");
                for (Registration reg : waitlistedRegs) {
                    Event event = eventHub.getEventById(reg.getEventId());
                    if (event != null) {
                        String statusIcon = "";
                        String statusText = "";
                        
                        switch (event.getStatus()) {
                            case CANCELLED:
                                statusIcon = "⚠️ ";
                                statusText = " [CANCELLED]";
                                break;
                            case DRAFT:
                                statusIcon = "⏳ ";
                                statusText = " [RESCHEDULED]";
                                break;
                            case COMPLETED:
                                statusIcon = "✅ ";
                                statusText = " [COMPLETED]";
                                break;
                            default:
                                statusIcon = "• ";
                                break;
                        }
                        
                        schedule.append(statusIcon).append(event.getTitle()).append(statusText).append("\n");
                        schedule.append("  Date: ").append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
                        schedule.append("  Status: ").append(event.getStatus().getDisplayName()).append("\n");
                        schedule.append("  Waitlist Position: #").append(reg.getWaitlistPosition()).append("\n");
                        schedule.append("  Registration: ").append(reg.getRegistrationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
                    }
                }
            }
        }
        
        scheduleArea.setText(schedule.toString());
    }
}