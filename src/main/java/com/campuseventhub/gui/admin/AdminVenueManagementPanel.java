package com.campuseventhub.gui.admin;

import com.campuseventhub.gui.common.ComponentFactory;
import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminVenueManagementPanel extends JPanel {
    private EventHub eventHub;
    private DefaultListModel<String> venuesListModel;
    private JFrame parentFrame;
    
    public AdminVenueManagementPanel(EventHub eventHub, JFrame parentFrame) {
        this.eventHub = eventHub;
        this.parentFrame = parentFrame;
        this.venuesListModel = new DefaultListModel<>();
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Venue Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        JList<String> venuesList = new JList<>(venuesListModel);
        venuesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane venuesScrollPane = new JScrollPane(venuesList);
        venuesScrollPane.setPreferredSize(new Dimension(400, 300));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton addVenueBtn = new JButton("Add Venue");
        JButton refreshVenuesBtn = new JButton("Refresh");
        
        addVenueBtn.addActionListener(e -> showAddVenueDialog());
        refreshVenuesBtn.addActionListener(e -> loadVenuesData());
        
        buttonsPanel.add(addVenueBtn);
        buttonsPanel.add(refreshVenuesBtn);
        
        add(venuesScrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void loadVenuesData() {
        venuesListModel.clear();
        List<Venue> venues = eventHub.listVenues();
        for (Venue venue : venues) {
            venuesListModel.addElement(venue.getName() + " - Capacity: " + venue.getCapacity());
        }
    }
    
    private void showAddVenueDialog() {
        ComponentFactory.showAddVenueDialog(parentFrame, this::loadVenuesData);
    }
}