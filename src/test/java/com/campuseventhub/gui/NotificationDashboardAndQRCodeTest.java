package com.campuseventhub.gui;

import com.campuseventhub.model.event.EventType;
import com.campuseventhub.model.notification.Notification;
import com.campuseventhub.model.notification.NotificationType;
import com.campuseventhub.service.NotificationService;
import com.campuseventhub.util.QRCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for notification dashboard functionality and QR code generation
 */
public class NotificationDashboardAndQRCodeTest {
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void testNotificationServiceFunctionality() {
        String userId = "test-user-123";
        String message = "Welcome to Campus EventHub! Your registration is confirmed.";
        
        // Test sending notification
        notificationService.sendNotification(message, List.of(userId), NotificationType.EVENT_REGISTRATION_CONFIRMATION);
        
        // Test retrieving notifications
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        assertFalse(notifications.isEmpty());
        assertEquals(1, notifications.size());
        
        Notification notification = notifications.get(0);
        assertEquals(userId, notification.getRecipientId());
        assertEquals(NotificationType.EVENT_REGISTRATION_CONFIRMATION, notification.getType());
        assertTrue(notification.getMessage().contains("registration is confirmed"));
        
        // Test unread count
        int unreadCount = notificationService.getUnreadNotificationCount(userId);
        assertEquals(1, unreadCount);
        
        // Test marking as read
        notificationService.markNotificationAsRead(notification.getNotificationId());
        unreadCount = notificationService.getUnreadNotificationCount(userId);
        assertEquals(0, unreadCount);
        
        // Test clearing notifications
        notificationService.clearUserNotifications(userId);
        notifications = notificationService.getUserNotifications(userId);
        assertTrue(notifications.isEmpty());
    }
    
    @Test
    void testMultipleNotificationTypes() {
        String userId = "test-user-456";
        
        // Send different types of notifications
        notificationService.sendNotification("Event reminder: Workshop starts in 1 hour", 
            List.of(userId), NotificationType.EVENT_REMINDER);
            
        notificationService.sendNotification("You have been promoted from waitlist", 
            List.of(userId), NotificationType.WAITLIST_PROMOTION);
            
        notificationService.sendNotification("Event has been cancelled", 
            List.of(userId), NotificationType.EVENT_CANCELLATION);
        
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        assertEquals(3, notifications.size());
        
        // Verify different notification types
        boolean hasReminder = false, hasPromotion = false, hasCancellation = false;
        for (Notification notification : notifications) {
            switch (notification.getType()) {
                case EVENT_REMINDER:
                    hasReminder = true;
                    break;
                case WAITLIST_PROMOTION:
                    hasPromotion = true;
                    break;
                case EVENT_CANCELLATION:
                    hasCancellation = true;
                    break;
            }
        }
        
        assertTrue(hasReminder);
        assertTrue(hasPromotion);
        assertTrue(hasCancellation);
    }

    @Test
    void testQRCodeGeneration() {
        String eventId = "event-123";
        String registrationId = "reg-456";
        String attendeeId = "att-789";
        
        // Test event QR code generation
        String eventQRData = QRCodeGenerator.generateEventQRCode(eventId);
        assertNotNull(eventQRData);
        assertTrue(eventQRData.contains("EventID:" + eventId));
        assertTrue(eventQRData.contains("Timestamp:"));
        
        // Test event + registration QR code generation
        String eventRegQRData = QRCodeGenerator.generateEventQRCode(eventId, registrationId);
        assertNotNull(eventRegQRData);
        assertTrue(eventRegQRData.contains("EventID:" + eventId));
        assertTrue(eventRegQRData.contains("RegistrationID:" + registrationId));
        
        // Test check-in QR code generation
        String checkInQRData = QRCodeGenerator.generateCheckInQRCode(eventId, registrationId, attendeeId);
        assertNotNull(checkInQRData);
        assertTrue(checkInQRData.contains("CheckIn"));
        assertTrue(checkInQRData.contains("EventID:" + eventId));
        assertTrue(checkInQRData.contains("RegistrationID:" + registrationId));
        assertTrue(checkInQRData.contains("AttendeeID:" + attendeeId));
    }
    
    @Test
    void testQRCodeValidation() {
        String eventId = "event-123";
        String registrationId = "reg-456";
        
        String qrData = QRCodeGenerator.generateEventQRCode(eventId, registrationId);
        
        // Test validation
        assertTrue(QRCodeGenerator.validateQRCode(qrData, eventId));
        assertFalse(QRCodeGenerator.validateQRCode(qrData, "different-event"));
        assertFalse(QRCodeGenerator.validateQRCode(null, eventId));
        assertFalse(QRCodeGenerator.validateQRCode(qrData, null));
    }
    
    @Test
    void testQRCodeDataExtraction() {
        String eventId = "event-123";
        String registrationId = "reg-456";
        String attendeeId = "att-789";
        
        String qrData = QRCodeGenerator.generateCheckInQRCode(eventId, registrationId, attendeeId);
        
        // Test extracting event ID
        String extractedEventId = QRCodeGenerator.extractEventIdFromQR(qrData);
        assertEquals(eventId, extractedEventId);
        
        // Test extracting registration ID
        String extractedRegistrationId = QRCodeGenerator.extractRegistrationIdFromQR(qrData);
        assertEquals(registrationId, extractedRegistrationId);
        
        // Test with invalid data
        assertNull(QRCodeGenerator.extractEventIdFromQR("invalid-qr-data"));
        assertNull(QRCodeGenerator.extractRegistrationIdFromQR("invalid-qr-data"));
        assertNull(QRCodeGenerator.extractEventIdFromQR(null));
    }
    
    @Test
    void testQRCodeImageGeneration() {
        String content = "EventID:test-event|RegistrationID:test-reg|Timestamp:12345";
        
        try {
            // Test basic QR code image generation
            BufferedImage qrImage = QRCodeGenerator.generateQRCodeImage(content);
            assertNotNull(qrImage);
            assertTrue(qrImage.getWidth() > 0);
            assertTrue(qrImage.getHeight() > 0);
            
            // Test custom dimensions
            BufferedImage customQrImage = QRCodeGenerator.generateQRCodeImage(content, 300, 300);
            assertNotNull(customQrImage);
            assertEquals(300, customQrImage.getWidth());
            assertEquals(300, customQrImage.getHeight());
            
            // Test Base64 generation
            String base64QR = QRCodeGenerator.generateQRCodeBase64(content);
            assertNotNull(base64QR);
            assertFalse(base64QR.isEmpty());
            
        } catch (Exception e) {
            fail("QR code generation should not throw exceptions: " + e.getMessage());
        }
    }
    
    @Test
    void testInvalidQRCodeGeneration() {
        // Test with null/empty inputs
        assertThrows(IllegalArgumentException.class, () -> 
            QRCodeGenerator.generateEventQRCode(null));
        assertThrows(IllegalArgumentException.class, () -> 
            QRCodeGenerator.generateEventQRCode(""));
        assertThrows(IllegalArgumentException.class, () -> 
            QRCodeGenerator.generateEventQRCode("eventId", null));
        assertThrows(IllegalArgumentException.class, () -> 
            QRCodeGenerator.generateCheckInQRCode(null, "regId", "attId"));
    }
    
    @Test
    void testNotificationTemplateIntegration() {
        String userId = "template-test-user";
        
        // Test different notification types to ensure templates work
        NotificationType[] types = {
            NotificationType.EVENT_REMINDER,
            NotificationType.EVENT_REGISTRATION_CONFIRMATION,
            NotificationType.EVENT_UPDATE,
            NotificationType.EVENT_CANCELLATION,
            NotificationType.WAITLIST_PROMOTION,
            NotificationType.SYSTEM_ANNOUNCEMENT
        };
        
        for (NotificationType type : types) {
            String message = "Test message for " + type.getDisplayName();
            notificationService.sendNotification(message, List.of(userId), type);
        }
        
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        assertEquals(types.length, notifications.size());
        
        // Verify each notification has the correct type
        for (int i = 0; i < types.length; i++) {
            assertEquals(types[i], notifications.get(i).getType());
        }
    }
}