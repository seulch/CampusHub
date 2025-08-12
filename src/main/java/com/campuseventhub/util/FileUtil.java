// =============================================================================
// UTILITY CLASSES
// =============================================================================

package com.campuseventhub.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for file operations.
 */
public class FileUtil {
    private FileUtil() {
        // Utility class
    }

    /**
     * Reads entire file content as string
     */
    public static String readFile(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("File does not exist: " + path);
        }
        return Files.readString(path);
    }
    
    /**
     * Writes content to file, creating directories if necessary
     */
    public static void writeFile(Path path, String content) throws IOException {
        if (path == null) {
            throw new IOException("Path cannot be null");
        }
        
        // Create parent directories if they don't exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        Files.writeString(path, content != null ? content : "");
    }
    
    /**
     * Checks if file exists at given path
     */
    public static boolean fileExists(Path path) {
        return path != null && Files.exists(path);
    }
    
    /**
     * Creates directory if it doesn't exist
     */
    public static void ensureDirectoryExists(Path dirPath) throws IOException {
        if (dirPath != null && !Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * Deletes file if it exists
     */
    public static boolean deleteFile(Path path) {
        if (path != null && Files.exists(path)) {
            try {
                Files.delete(path);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
}
