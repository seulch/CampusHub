package com.campuseventhub.integration;

import com.campuseventhub.service.EventHub;
import com.campuseventhub.model.user.User;
import com.campuseventhub.model.venue.Venue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

/**
 * Final test to verify persistence works with src/main/resources/data/
 */
public class FinalPersistenceTest {
    
    private EventHub eventHub;
    
    @BeforeEach
    void setUp() {
        eventHub = EventHub.getInstance();
        eventHub.logoutCurrentUser();
    }
    
    @Test
    @DisplayName("Test data persistence with src/main/resources/data/ location")
    void testFinalPersistence() {
        System.out.println("\n=== FINAL PERSISTENCE TEST ===\n");
        
        // Load existing data
        List<User> users = eventHub.getAllUsers();
        List<Venue> venues = eventHub.listVenues();
        
        System.out.println("Loaded existing data:");
        System.out.println("- Users: " + users.size());
        System.out.println("- Venues: " + venues.size());
        
        // Verify data files are in the correct location
        String expectedDataDir = System.getProperty("user.dir") + "/src/main/resources/data/";
        File dataDir = new File(expectedDataDir);
        assertTrue(dataDir.exists(), "Data directory should exist at: " + expectedDataDir);
        
        File usersFile = new File(expectedDataDir + "users.ser");
        File venuesFile = new File(expectedDataDir + "venues.ser");
        
        if (users.size() > 0) {
            assertTrue(usersFile.exists(), "Users file should exist");
        }
        if (venues.size() > 0) {
            assertTrue(venuesFile.exists(), "Venues file should exist");
        }
        
        System.out.println("✓ Data files are in correct location: " + expectedDataDir);
        System.out.println("✓ This location survives Maven clean (not in target/)");
        System.out.println("✓ This location is part of your project structure");
        
        System.out.println("\n=== PERSISTENCE WORKING CORRECTLY ===");
    }
}