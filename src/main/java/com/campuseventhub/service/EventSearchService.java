package com.campuseventhub.service;

import com.campuseventhub.model.event.Event;
import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.event.EventSearchCriteria;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class EventSearchService {
    
    public List<Event> searchEvents(List<Event> allEvents, EventSearchCriteria criteria) {
        if (criteria == null) {
            return new ArrayList<>(allEvents);
        }
        
        return allEvents.stream()
            .filter(event -> matchesCriteria(event, criteria))
            .collect(Collectors.toList());
    }
    
    public List<Event> getEventsByType(List<Event> allEvents, EventType eventType) {
        return allEvents.stream()
            .filter(event -> event.getEventType() == eventType)
            .collect(Collectors.toList());
    }
    
    public List<Event> getEventsByOrganizer(List<Event> allEvents, String organizerId) {
        return allEvents.stream()
            .filter(event -> event.getOrganizerId().equals(organizerId))
            .collect(Collectors.toList());
    }
    
    public List<Event> getUpcomingEvents(List<Event> allEvents) {
        LocalDateTime now = LocalDateTime.now();
        return allEvents.stream()
            .filter(event -> event.getStartDateTime().isAfter(now))
            .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
            .collect(Collectors.toList());
    }
    
    public List<Event> getAvailableEvents(List<Event> allEvents) {
        return allEvents.stream()
            .filter(event -> event.isRegistrationOpen())
            .collect(Collectors.toList());
    }
    
    private boolean matchesCriteria(Event event, EventSearchCriteria criteria) {
        if (criteria.getEventType() != null && event.getEventType() != criteria.getEventType()) {
            return false;
        }
        
        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            String keyword = criteria.getKeyword().toLowerCase();
            if (!event.getTitle().toLowerCase().contains(keyword) && 
                !event.getDescription().toLowerCase().contains(keyword)) {
                return false;
            }
        }
        
        if (criteria.getStartDate() != null && event.getStartDateTime().isBefore(criteria.getStartDate())) {
            return false;
        }
        
        if (criteria.getEndDate() != null && event.getEndDateTime().isAfter(criteria.getEndDate())) {
            return false;
        }
        
        if (criteria.getOrganizerId() != null && !event.getOrganizerId().equals(criteria.getOrganizerId())) {
            return false;
        }
        
        return true;
    }
}