package com.campuseventhub.gui.admin;

import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;

public class AdminSystemStatsPanel extends JPanel {
    private EventHub eventHub;
    private JTextArea statsArea;
    
    public AdminSystemStatsPanel(EventHub eventHub) {
        this.eventHub = eventHub;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = ComponentFactory.createHeadingLabel("System Statistics");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        statsArea = ComponentFactory.createStandardTextArea();
        
        JScrollPane statsScrollPane = new JScrollPane(statsArea);
        add(statsScrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = ComponentFactory.createStandardButton("Refresh Stats");
        refreshBtn.addActionListener(e -> updateSystemStats());
        add(refreshBtn, BorderLayout.SOUTH);
        
        updateSystemStats();
    }
    
    public void updateSystemStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== SYSTEM STATISTICS ===\n\n");
        stats.append("User Statistics:\n");
        stats.append("- Total Users: ").append(eventHub.getAllUsers().size()).append("\n");
        stats.append("- Active Users: ").append(eventHub.getAllUsers().stream().mapToInt(u -> u.isActive() ? 1 : 0).sum()).append("\n\n");
        
        stats.append("Venue Statistics:\n");
        stats.append("- Total Venues: ").append(eventHub.listVenues().size()).append("\n");
        stats.append("- Active Venues: ").append(eventHub.listVenues().stream().mapToInt(v -> v.isActive() ? 1 : 0).sum()).append("\n\n");
        
        statsArea.setText(stats.toString());
    }
    
    public String generateSystemReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== CAMPUS EVENTHUB SYSTEM REPORT ===\n\n");
        report.append("Generated: ").append(new java.util.Date()).append("\n\n");
        report.append("USERS:\n");
        report.append("Total Users: ").append(eventHub.getAllUsers().size()).append("\n\n");
        report.append("VENUES:\n");
        report.append("Total Venues: ").append(eventHub.listVenues().size()).append("\n\n");
        return report.toString();
    }
}