package com.campuseventhub.gui.admin;

import com.campuseventhub.gui.common.ComponentFactory;
import com.campuseventhub.model.user.Admin;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;

public class AdminOverviewPanel extends JPanel {
    private EventHub eventHub;
    private Admin admin;
    private JFrame parentFrame;
    private JTabbedPane mainTabbedPane;
    private AdminSystemStatsPanel statsPanel;
    
    public AdminOverviewPanel(EventHub eventHub, Admin admin, JFrame parentFrame, 
                             JTabbedPane mainTabbedPane, AdminSystemStatsPanel statsPanel) {
        this.eventHub = eventHub;
        this.admin = admin;
        this.parentFrame = parentFrame;
        this.mainTabbedPane = mainTabbedPane;
        this.statsPanel = statsPanel;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getFirstName() + " " + admin.getLastName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        contentPanel.add(createSystemMetricsPanel());
        contentPanel.add(createQuickActionsPanel());
        contentPanel.add(createRecentActivityPanel());
        contentPanel.add(createSystemHealthPanel());
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSystemMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Metrics"));
        
        JLabel metricsLabel = new JLabel("<html><body>" +
            "<b>Total Users:</b> " + eventHub.getAllUsers().size() + "<br>" +
            "<b>Total Venues:</b> " + eventHub.listVenues().size() + "<br>" +
            "<b>System Status:</b> <font color='green'>Online</font>" +
            "</body></html>");
        panel.add(metricsLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        JButton addVenueBtn = new JButton("Add New Venue");
        JButton viewUsersBtn = new JButton("View All Users");
        JButton systemReportBtn = new JButton("Generate System Report");
        
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        viewUsersBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
        systemReportBtn.addActionListener(e -> showSystemReport());
        
        panel.add(addVenueBtn);
        panel.add(viewUsersBtn);
        panel.add(systemReportBtn);
        
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
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        JLabel healthLabel = new JLabel("<html><body>" +
            "<b>Status:</b> <font color='green'>All systems operational</font><br>" +
            "<b>Memory Used:</b> " + (usedMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Total:</b> " + (totalMemory / 1024 / 1024) + "MB<br>" +
            "<b>Memory Max:</b> " + (maxMemory / 1024 / 1024) + "MB" +
            "</body></html>");
        
        panel.add(healthLabel, BorderLayout.CENTER);
        return panel;
    }
    
    private void showAddVenueDialog() {
        ComponentFactory.showAddVenueDialog(parentFrame, null);
    }
    
    private void showSystemReport() {
        new AdminActionHandler(eventHub, admin, parentFrame).showSystemReport(statsPanel);
    }
    
    public void refreshMetrics() {
        removeAll();
        initializeComponents();
        revalidate();
        repaint();
    }
}