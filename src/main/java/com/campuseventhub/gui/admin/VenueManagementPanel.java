// =============================================================================
// VENUE MANAGEMENT PANEL
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for administrators to manage venue information.
 *
 * Implementation Details:
 * - Venue list display and management
 * - Add new venue functionality
 * - Venue details viewing and editing
 * - Venue availability tracking
 */
public class VenueManagementPanel extends JPanel {
    private EventHub eventHub;
    private DefaultListModel<String> venuesListModel;
    private JList<String> venuesList;
    private JButton addVenueBtn;
    private JButton refreshVenuesBtn;
    
    public VenueManagementPanel() {
        this.eventHub = EventHub.getInstance();
        initializeComponents();
        loadVenues();
        registerListeners();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Venue Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Venues list
        venuesListModel = new DefaultListModel<>();
        venuesList = new JList<>(venuesListModel);
        venuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane venuesScrollPane = new JScrollPane(venuesList);
        venuesScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        addVenueBtn = new JButton("Add Venue");
        refreshVenuesBtn = new JButton("Refresh");
        
        buttonsPanel.add(addVenueBtn);
        buttonsPanel.add(refreshVenuesBtn);
        
        add(venuesScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void registerListeners() {
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        refreshVenuesBtn.addActionListener(e -> loadVenues());
    }
    
    private void loadVenues() {
        venuesListModel.clear();
        List<Venue> venues = eventHub.listVenues();
        for (Venue venue : venues) {
            venuesListModel.addElement(venue.getName() + " - Capacity: " + venue.getCapacity());
        }
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
                    Venue venue = new Venue(name, location, capacity);
                    if (eventHub.addVenue(venue)) {
                        loadVenues();
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
    
    public void refreshData() {
        loadVenues();
    }
}

