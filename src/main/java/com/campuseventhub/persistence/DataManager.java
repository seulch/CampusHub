// =============================================================================
// PERSISTENCE LAYER
// =============================================================================

package com.campuseventhub.persistence;

import com.campuseventhub.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Central data management for serialization and file operations.
 */
public class DataManager {
    // Use absolute path to src/main/resources/data/ to ensure consistency
    private static final String DATA_DIRECTORY = getDataDirectory();
    private static final String BACKUP_DIRECTORY = getDataDirectory() + "backup/";
    
    static {
        initializeDataDirectory();
    }
    
    /**
     * Determines the correct data directory path for consistent file storage
     */
    private static String getDataDirectory() {
        String userDir = System.getProperty("user.dir");
        String srcPath = userDir + "/src/main/resources/data/";
        String targetPath = userDir + "/target/classes/data/";
        
        // Check if we're running from maven (target path exists) or source
        java.io.File targetDir = new java.io.File(targetPath);
        java.io.File srcDir = new java.io.File(srcPath);
        
        if (targetDir.exists()) {
            System.out.println("DataManager: Using target data directory: " + targetPath);
            return targetPath;
        } else if (srcDir.exists()) {
            System.out.println("DataManager: Using source data directory: " + srcPath);
            return srcPath;
        } else {
            // Default to source path and create if needed
            System.out.println("DataManager: Creating source data directory: " + srcPath);
            return srcPath;
        }
    }
    
    /**
     * Saves object data to file using Java serialization
     */
    public static void saveData(String filename, Object data) throws IOException {
        if (filename == null || data == null) {
            throw new IllegalArgumentException("Filename and data cannot be null");
        }
        
        if (filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        
        Path filePath = Paths.get(DATA_DIRECTORY + filename);
        
        // Ensure directory exists
        FileUtil.ensureDirectoryExists(filePath.getParent());
        
        // Create backup before saving (only for critical files)
        try {
            if (FileUtil.fileExists(filePath) && (filename.equals("users.ser") || filename.equals("events.ser"))) {
                System.out.println("DataManager: Creating backup before saving " + filename);
                createBackup();
            }
        } catch (Exception e) {
            System.err.println("DataManager: Warning - Could not create backup for " + filename + ": " + e.getMessage());
        }
        
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filePath.toFile()))) {
            out.writeObject(data);
            System.out.println("DataManager: Successfully saved " + filename);
        } catch (IOException e) {
            System.err.println("DataManager: Failed to save " + filename + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Loads object data from file using Java deserialization
     */
    public static Object loadData(String filename) throws IOException, ClassNotFoundException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        Path filePath = Paths.get(DATA_DIRECTORY + filename);
        
        if (!FileUtil.fileExists(filePath)) {
            System.out.println("DataManager: No existing data file found: " + filename);
            return null; // File doesn't exist, return null instead of throwing exception
        }
        
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            Object data = in.readObject();
            System.out.println("DataManager: Successfully loaded " + filename);
            return data;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("DataManager: Failed to load " + filename + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates a backup of all data files
     */
    public static void createBackup() throws IOException {
        Path dataDir = Paths.get(DATA_DIRECTORY);
        Path backupDir = Paths.get(BACKUP_DIRECTORY);
        
        // Ensure backup directory exists
        FileUtil.ensureDirectoryExists(backupDir);
        
        // Create timestamped backup directory
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path timestampedBackupDir = backupDir.resolve("backup_" + timestamp);
        FileUtil.ensureDirectoryExists(timestampedBackupDir);
        
        // Copy all .ser files from data directory to backup
        File[] dataFiles = dataDir.toFile().listFiles((dir, name) -> name.endsWith(".ser"));
        if (dataFiles != null) {
            for (File file : dataFiles) {
                try {
                    String content = FileUtil.readFile(file.toPath());
                    FileUtil.writeFile(timestampedBackupDir.resolve(file.getName()), content);
                } catch (IOException e) {
                    // Log error but continue with other files
                    System.err.println("Failed to backup file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Restores data from a specific backup
     */
    public static boolean restoreFromBackup(String backupFile) {
        try {
            Path backupPath = Paths.get(BACKUP_DIRECTORY + backupFile);
            if (!FileUtil.fileExists(backupPath)) {
                return false;
            }
            
            // Copy backup file to data directory
            String content = FileUtil.readFile(backupPath);
            Path dataPath = Paths.get(DATA_DIRECTORY + backupFile);
            FileUtil.writeFile(dataPath, content);
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to restore from backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initializes data directory structure
     */
    public static void initializeDataDirectory() {
        try {
            FileUtil.ensureDirectoryExists(Paths.get(DATA_DIRECTORY));
            FileUtil.ensureDirectoryExists(Paths.get(BACKUP_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Failed to initialize data directories: " + e.getMessage());
        }
    }
    
    /**
     * Gets the path to a data file
     */
    public static Path getDataFilePath(String filename) {
        return Paths.get(DATA_DIRECTORY + filename);
    }
    
    /**
     * Checks if a data file exists
     */
    public static boolean dataFileExists(String filename) {
        return FileUtil.fileExists(getDataFilePath(filename));
    }
    
    /**
     * Deletes a data file
     */
    public static boolean deleteDataFile(String filename) {
        return FileUtil.deleteFile(getDataFilePath(filename));
    }
}