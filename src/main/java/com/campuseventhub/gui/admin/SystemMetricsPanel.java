// =============================================================================
// SYSTEM METRICS PANEL
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying system metrics and health information.
 *
 * Implementation Details:
 * - Real-time system metrics display
 * - Memory usage monitoring
 * - User and venue statistics
 * - System health indicators
 */
public class SystemMetricsPanel extends JPanel {
    private EventHub eventHub;
    private JLabel metricsLabel;
    private JLabel healthLabel;
    private JButton refreshBtn;
    
    public SystemMetricsPanel() {
        this.eventHub = EventHub.getInstance();
        initializeComponents();
        registerListeners();
        updateMetrics();
    }
    
    private void initializeComponents() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add system info panels
        add(createSystemMetricsPanel());
        add(createQuickActionsPanel());
        add(createRecentActivityPanel());
        add(createSystemHealthPanel());
    }
    
    private void registerListeners() {
        refreshBtn.addActionListener(e -> updateMetrics());
    }
    
    // System Metrics Panel
    private JPanel createSystemMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Metrics"));
        
        metricsLabel = new JLabel();
        panel.add(metricsLabel, BorderLayout.CENTER);
        return panel;
    }
    
    // Quick Actions Panel
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton addVenueBtn = new JButton("Add New Venue");
        JButton viewUsersBtn = new JButton("View All Users");
        refreshBtn = new JButton("Refresh Metrics");
        
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        viewUsersBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Navigate to User Management tab"));
        
        panel.add(addVenueBtn);
        panel.add(viewUsersBtn);
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        
        JTextArea activityArea = new JTextArea("• System started\n• Admin logged in\n• Dashboard loaded");
        activityArea.setEditable(false);
        activityArea.setBackground(getBackground());
        
        panel.add(new JScrollPane(activityArea), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSystemHealthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Health"));
        
        healthLabel = new JLabel();
        panel.add(healthLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private void updateMetrics() {
        // Update system metrics
        String metricsText = "<html><body>" +
            "<b>Total Users:</b> " + eventHub.getAllUsers().size() + "<br>" +
            "<b>Total Venues:</b> " + eventHub.listVenues().size() + "<br>" +
            "<b>System Status:</b> <font color='green'>Online</font>" +
            "</body></html>";
        metricsLabel.setText(metricsText);
        
        // Update system health
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        String healthText = "<html><body>" +
            "<b>Status:</b> <font color='green'>All systems operational</font><br>" +
            "<b>Memory Used:</b> " + (usedMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Total:</b> " + (totalMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Max:</b> " + (maxMemory / 1024 / 1024) + "MB" +
            "</body></html>";
        healthLabel.setText(healthText);
    }
    
    private void showAddVenueDialog() {
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField capacityField = new JTextField(10);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacityField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Venue", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String location = locationField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                
                if (!name.isEmpty() && !location.isEmpty() && capacity > 0) {
                    com.campuseventhub.model.venue.Venue venue = new com.campuseventhub.model.venue.Venue(name, location, capacity);
                    if (eventHub.addVenue(venue)) {
                        updateMetrics();
                        JOptionPane.showMessageDialog(this, "Venue added successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add venue.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please fill all fields with valid data.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacity must be a valid number.");
            }
        }
    }
    
    public void refreshMetrics() {
        updateMetrics();
    }
}

