// =============================================================================
// UTILITY CLASSES
// =============================================================================

package com.campuseventhub.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

/**
 * Utility class for QR code generation using ZXing library.
 * Provides real QR code functionality for event and registration management.
 */
public class QRCodeGenerator {
    private static final int DEFAULT_QR_SIZE = 200;
    private static final int BORDER_SIZE = 1;
    
    private QRCodeGenerator() {
        // Utility class
    }
    
    /**
     * Generate a QR code string for an event with registration ID
     */
    public static String generateEventQRCode(String eventId, String registrationId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (registrationId == null || registrationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration ID cannot be null or empty");
        }
        
        // Create QR code data containing both event and registration information
        return String.format("EventID:%s|RegistrationID:%s|Timestamp:%d", 
                           eventId, registrationId, System.currentTimeMillis());
    }
    
    /**
     * Generate a QR code string for an event (without registration)
     */
    public static String generateEventQRCode(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        
        // Create QR code data for event information only
        return String.format("EventID:%s|Timestamp:%d", eventId, System.currentTimeMillis());
    }
    
    /**
     * Generate QR code for event check-in with registration details
     */
    public static String generateCheckInQRCode(String eventId, String registrationId, String attendeeId) {
        if (eventId == null || registrationId == null || attendeeId == null) {
            throw new IllegalArgumentException("Event ID, Registration ID, and Attendee ID cannot be null");
        }
        
        // Create comprehensive check-in QR code data
        return String.format("CheckIn|EventID:%s|RegistrationID:%s|AttendeeID:%s|Timestamp:%d", 
                           eventId, registrationId, attendeeId, System.currentTimeMillis());
    }
    
    /**
     * Validate a QR code against an event (simulated)
     */
    public static boolean validateQRCode(String qrCode, String eventId) {
        if (qrCode == null || eventId == null) {
            return false;
        }
        
        // Check if QR code contains the event ID in our format
        return qrCode.contains("EventID:" + eventId);
    }
    
    /**
     * Extract event ID from QR code
     */
    public static String extractEventIdFromQR(String qrCode) {
        if (qrCode == null) {
            return null;
        }
        
        // Extract EventID from our format: "EventID:value|..."
        String prefix = "EventID:";
        int startIndex = qrCode.indexOf(prefix);
        if (startIndex == -1) {
            return null;
        }
        
        startIndex += prefix.length();
        int endIndex = qrCode.indexOf('|', startIndex);
        if (endIndex == -1) {
            endIndex = qrCode.length();
        }
        
        return qrCode.substring(startIndex, endIndex);
    }
    
    /**
     * Extract registration ID from QR code
     */
    public static String extractRegistrationIdFromQR(String qrCode) {
        if (qrCode == null) {
            return null;
        }
        
        // Extract RegistrationID from our format: "...RegistrationID:value|..."
        String prefix = "RegistrationID:";
        int startIndex = qrCode.indexOf(prefix);
        if (startIndex == -1) {
            return null;
        }
        
        startIndex += prefix.length();
        int endIndex = qrCode.indexOf('|', startIndex);
        if (endIndex == -1) {
            endIndex = qrCode.length();
        }
        
        return qrCode.substring(startIndex, endIndex);
    }
    
    /**
     * Generate QR code image as BufferedImage
     */
    public static BufferedImage generateQRCodeImage(String content) throws WriterException {
        return generateQRCodeImage(content, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }
    
    /**
     * Generate QR code image as BufferedImage with custom dimensions
     */
    public static BufferedImage generateQRCodeImage(String content, int width, int height) throws WriterException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, BORDER_SIZE);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        
        return createImageFromBitMatrix(bitMatrix);
    }
    
    /**
     * Generate QR code as Base64 encoded string (for easy display in UI)
     */
    public static String generateQRCodeBase64(String content) throws WriterException, IOException {
        BufferedImage qrImage = generateQRCodeImage(content);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Save QR code image to file
     */
    public static String generateQRCodeImagePath(String content, String outputDir) throws WriterException, IOException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        
        BufferedImage qrImage = generateQRCodeImage(content);
        
        // Create output directory if it doesn't exist
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Generate filename based on content hash
        String hash = Integer.toHexString(content.hashCode());
        String filename = "qr_" + hash + ".png";
        File outputFile = new File(dir, filename);
        
        ImageIO.write(qrImage, "PNG", outputFile);
        
        return outputFile.getAbsolutePath();
    }
    
    /**
     * Create BufferedImage from BitMatrix
     */
    private static BufferedImage createImageFromBitMatrix(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        
        return image;
    }
}
