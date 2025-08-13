package com.campuseventhub.gui.organizer;

import com.campuseventhub.model.venue.Venue;
import com.campuseventhub.service.EventHub;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel for selecting venues with availability checking
 */
public class VenueSelectionPanel extends JPanel {
    private EventHub eventHub;
    private JComboBox<VenueOption> venueComboBox;
    private JLabel availabilityLabel;
    private JLabel capacityLabel;
    private JButton checkAvailabilityBtn;
    private ActionListener onVenueSelected;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int requiredCapacity;
    
    public VenueSelectionPanel(EventHub eventHub) {
        this.eventHub = eventHub;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Venue selection
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Select Venue:"), gbc);
        
        venueComboBox = new JComboBox<>();
        venueComboBox.setPreferredSize(new Dimension(300, 25));
        venueComboBox.addActionListener(e -> onVenueSelectionChanged());
        gbc.gridx = 1;
        add(venueComboBox, gbc);
        
        // Check availability button
        checkAvailabilityBtn = new JButton("Check Availability");
        checkAvailabilityBtn.addActionListener(e -> checkVenueAvailability());
        gbc.gridx = 2;
        add(checkAvailabilityBtn, gbc);
        
        // Availability status
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Availability:"), gbc);
        
        availabilityLabel = new JLabel("Select venue and set date/time first");
        availabilityLabel.setForeground(Color.BLUE);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(availabilityLabel, gbc);
        
        // Capacity info
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Venue Capacity:"), gbc);
        
        capacityLabel = new JLabel("Not selected");
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(capacityLabel, gbc);
        
        loadAllVenues();
    }
    
    /**
     * Updates venue availability based on event date/time
     */
    public void updateAvailability(LocalDateTime startTime, LocalDateTime endTime, int requiredCapacity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredCapacity = requiredCapacity;
        
        loadAvailableVenues();
        checkVenueAvailability();
    }
    
    /**
     * Loads all venues into the combo box
     */
    private void loadAllVenues() {
        venueComboBox.removeAllItems();
        venueComboBox.addItem(new VenueOption(null, "No venue selected"));
        
        List<Venue> venues = eventHub.listVenues();
        for (Venue venue : venues) {
            if (venue.isActive()) {
                venueComboBox.addItem(new VenueOption(venue, formatVenueDisplay(venue)));
            }
        }
    }
    
    /**
     * Loads only available venues for the specified time
     */
    private void loadAvailableVenues() {
        if (startTime == null || endTime == null) {
            loadAllVenues();
            return;
        }
        
        venueComboBox.removeAllItems();
        venueComboBox.addItem(new VenueOption(null, "No venue selected"));
        
        List<Venue> availableVenues = eventHub.getAvailableVenues(startTime, endTime, requiredCapacity);
        for (Venue venue : availableVenues) {
            venueComboBox.addItem(new VenueOption(venue, formatVenueDisplay(venue)));
        }
        
        if (availableVenues.isEmpty()) {
            venueComboBox.addItem(new VenueOption(null, "No venues available for selected time"));
        }
    }
    
    private String formatVenueDisplay(Venue venue) {
        return String.format("%s - %s (Capacity: %d)", 
            venue.getName(), venue.getLocation(), venue.getCapacity());
    }
    
    private void onVenueSelectionChanged() {
        VenueOption selected = (VenueOption) venueComboBox.getSelectedItem();
        if (selected != null && selected.venue != null) {
            capacityLabel.setText(String.valueOf(selected.venue.getCapacity()));
            
            // Check capacity compatibility
            if (requiredCapacity > 0 && selected.venue.getCapacity() < requiredCapacity) {
                capacityLabel.setForeground(Color.RED);
                capacityLabel.setText(selected.venue.getCapacity() + " (Insufficient for " + requiredCapacity + " attendees)");
            } else {
                capacityLabel.setForeground(Color.BLACK);
            }
        } else {
            capacityLabel.setText("Not selected");
            capacityLabel.setForeground(Color.BLACK);
        }
        
        checkVenueAvailability();
        
        if (onVenueSelected != null) {
            onVenueSelected.actionPerformed(null);
        }
    }
    
    private void checkVenueAvailability() {
        VenueOption selected = (VenueOption) venueComboBox.getSelectedItem();
        
        if (selected == null || selected.venue == null) {
            availabilityLabel.setText("No venue selected");
            availabilityLabel.setForeground(Color.BLUE);
            return;
        }
        
        if (startTime == null || endTime == null) {
            availabilityLabel.setText("Set event date/time to check availability");
            availabilityLabel.setForeground(Color.BLUE);
            return;
        }
        
        List<String> conflicts = eventHub.getEventVenueConflicts(selected.venue.getVenueId());
        if (conflicts.isEmpty()) {
            availabilityLabel.setText("✓ Available");
            availabilityLabel.setForeground(Color.GREEN);
        } else {
            availabilityLabel.setText("✗ " + String.join(", ", conflicts));
            availabilityLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Gets the selected venue
     */
    public Venue getSelectedVenue() {
        VenueOption selected = (VenueOption) venueComboBox.getSelectedItem();
        return selected != null ? selected.venue : null;
    }
    
    /**
     * Gets the selected venue ID
     */
    public String getSelectedVenueId() {
        Venue venue = getSelectedVenue();
        return venue != null ? venue.getVenueId() : null;
    }
    
    /**
     * Sets the selected venue by ID
     */
    public void setSelectedVenue(String venueId) {
        for (int i = 0; i < venueComboBox.getItemCount(); i++) {
            VenueOption option = venueComboBox.getItemAt(i);
            if (option.venue != null && option.venue.getVenueId().equals(venueId)) {
                venueComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    /**
     * Checks if venue selection is valid
     */
    public boolean isValidSelection() {
        VenueOption selected = (VenueOption) venueComboBox.getSelectedItem();
        if (selected == null || selected.venue == null) {
            return false;
        }
        
        // Check capacity
        if (requiredCapacity > 0 && selected.venue.getCapacity() < requiredCapacity) {
            return false;
        }
        
        // Check availability
        if (startTime != null && endTime != null) {
            List<String> conflicts = eventHub.getEventVenueConflicts(selected.venue.getVenueId());
            return conflicts.isEmpty();
        }
        
        return true;
    }
    
    public void setOnVenueSelected(ActionListener listener) {
        this.onVenueSelected = listener;
    }
    
    /**
     * Wrapper class for venue combo box items
     */
    private static class VenueOption {
        final Venue venue;
        final String displayText;
        
        VenueOption(Venue venue, String displayText) {
            this.venue = venue;
            this.displayText = displayText;
        }
        
        @Override
        public String toString() {
            return displayText;
        }
    }
}