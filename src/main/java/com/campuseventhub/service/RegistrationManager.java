package com.campuseventhub.service;

import com.campuseventhub.model.event.Registration;
import com.campuseventhub.model.event.RegistrationStatus;
import com.campuseventhub.persistence.DataManager;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.time.LocalDateTime;

public class RegistrationManager {
    private Map<String, Registration> registrations;
    private Map<String, List<Registration>> registrationsByEvent;
    private Map<String, List<Registration>> registrationsByAttendee;
    
    public RegistrationManager() {
        this.registrations = new ConcurrentHashMap<>();
        this.registrationsByEvent = new ConcurrentHashMap<>();
        this.registrationsByAttendee = new ConcurrentHashMap<>();
        loadRegistrationsFromPersistence();
    }
    
    public Registration createRegistration(String eventId, String attendeeId) {
        if (eventId == null || attendeeId == null) {
            throw new IllegalArgumentException("Event ID and Attendee ID cannot be null");
        }
        
        // Check if attendee is already registered for this event
        Registration existingRegistration = findRegistration(attendeeId, eventId);
        if (existingRegistration != null && existingRegistration.getStatus() != RegistrationStatus.CANCELLED) {
            return null; // Already registered
        }
        
        Registration registration = new Registration(attendeeId, eventId);
        registration.confirmRegistration();
        registrations.put(registration.getRegistrationId(), registration);
        registrationsByEvent.computeIfAbsent(eventId, k -> new ArrayList<>()).add(registration);
        registrationsByAttendee.computeIfAbsent(attendeeId, k -> new ArrayList<>()).add(registration);
        
        saveRegistrationsToPersistence();
        return registration;
    }
    
    public boolean cancelRegistration(String registrationId) {
        Registration registration = registrations.get(registrationId);
        if (registration != null) {
            registration.setStatus(RegistrationStatus.CANCELLED);
            registration.setCancellationTime(LocalDateTime.now());
            saveRegistrationsToPersistence();
            return true;
        }
        return false;
    }
    
    public List<Registration> getEventRegistrations(String eventId) {
        return registrationsByEvent.getOrDefault(eventId, new ArrayList<>());
    }
    
    public List<Registration> getAttendeeRegistrations(String attendeeId) {
        return registrationsByAttendee.getOrDefault(attendeeId, new ArrayList<>());
    }
    
    public Registration findRegistration(String attendeeId, String eventId) {
        List<Registration> attendeeRegs = registrationsByAttendee.getOrDefault(attendeeId, new ArrayList<>());
        return attendeeRegs.stream()
            .filter(reg -> reg.getEventId().equals(eventId))
            .findFirst()
            .orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    private void loadRegistrationsFromPersistence() {
        try {
            Object registrationsData = DataManager.loadData("registrations.ser");
            if (registrationsData instanceof Map) {
                Map<String, Registration> loadedRegistrations = (Map<String, Registration>) registrationsData;
                for (Registration reg : loadedRegistrations.values()) {
                    registrations.put(reg.getRegistrationId(), reg);
                    registrationsByEvent.computeIfAbsent(reg.getEventId(), k -> new ArrayList<>()).add(reg);
                    registrationsByAttendee.computeIfAbsent(reg.getAttendeeId(), k -> new ArrayList<>()).add(reg);
                }
                System.out.println("RegistrationManager: Successfully loaded " + loadedRegistrations.size() + " registrations");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("RegistrationManager: No existing registration data found: " + e.getMessage());
        }
    }
    
    private void saveRegistrationsToPersistence() {
        try {
            System.out.println("RegistrationManager: Attempting to save " + registrations.size() + " registrations to persistence");
            DataManager.saveData("registrations.ser", new ConcurrentHashMap<>(registrations));
            System.out.println("RegistrationManager: Successfully saved registrations to persistence");
        } catch (IOException e) {
            System.err.println("RegistrationManager: Failed to save registrations to persistence: " + e.getMessage());
            e.printStackTrace();
        }
    }
}