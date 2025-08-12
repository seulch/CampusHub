// =============================================================================
// VENUE MANAGEMENT PANEL
// =============================================================================

package com.campuseventhub.gui.admin;

import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;
import com.campuseventhub.gui.common.ComponentFactory;
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
        ComponentFactory.showAddVenueDialog(this, this::loadVenues);
    }
    
    public void refreshData() {
        loadVenues();
    }
}

