// =============================================================================
// SYSTEM REPORTS PANEL
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for generating and viewing system reports.
 *
 * Implementation Details:
 * - Report selection and generation controls
 * - Visualization of key metrics
 * - Export options for various formats
 * - Scheduling of automated reports
 */
public class SystemReportsPanel extends JPanel {
    private EventHub eventHub;
    private JTextArea reportArea;
    private JButton generateReportBtn;
    
    public SystemReportsPanel() {
        this.eventHub = EventHub.getInstance();
        initializeComponents();
        registerListeners();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = ComponentFactory.createHeadingLabel("System Reports");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);
        
        // Report display area
        reportArea = ComponentFactory.createStandardTextArea();
        
        JScrollPane reportScrollPane = new JScrollPane(reportArea);
        reportScrollPane.setPreferredSize(new Dimension(500, 400));
        add(reportScrollPane, BorderLayout.CENTER);
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout());
        generateReportBtn = ComponentFactory.createPrimaryButton("Generate System Report");
        controlsPanel.add(generateReportBtn);
        add(controlsPanel, BorderLayout.SOUTH);
    }
    
    private void registerListeners() {
        generateReportBtn.addActionListener(e -> generateReport());
    }

    private void generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== CAMPUS EVENTHUB SYSTEM REPORT ===\n\n");
        report.append("Generated: ").append(new java.util.Date()).append("\n\n");
        report.append("USERS:\n");
        report.append("Total Users: ").append(eventHub.getAllUsers().size()).append("\n\n");
        report.append("VENUES:\n");
        report.append("Total Venues: ").append(eventHub.listVenues().size()).append("\n\n");
        
        reportArea.setText(report.toString());
    }
    
    public void refreshReport() {
        generateReport();
    }
}
