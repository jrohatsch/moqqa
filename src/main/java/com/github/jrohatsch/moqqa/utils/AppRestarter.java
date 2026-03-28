package com.github.jrohatsch.moqqa.utils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Utility class for safely restarting the entire Java application.
 */
public class AppRestarter {
    private static final Logger LOGGER = Logger.getLogger(AppRestarter.class.getSimpleName());

    /**
     * Restarts the application by starting a new JVM process.
     * Properly closes all resources before restarting.
     */
    public static void restartApplication() {
        try {
            // Get the Java executable path
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            
            // Get the JAR file path
            String jarPath = getJarPath();
            
            if (jarPath == null) {
                LOGGER.warning("Could not determine JAR path. Running in IDE mode.");
                // If running in IDE, restart via main method instead
                restartInIde();
                return;
            }
            
            // Build the command to restart the application
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", jarPath);
            
            // Redirect output and error streams
            pb.inheritIO();
            
            // Start the new process
            pb.start();
            
            // Exit the current application gracefully
            System.exit(0);
            
        } catch (IOException e) {
            LOGGER.severe("Failed to restart application: " + e.getMessage());
            throw new RuntimeException("Failed to restart application", e);
        }
    }

    /**
     * Gets the path to the currently running JAR file.
     * Returns null if the application is running from an IDE (not from a JAR).
     */
    private static String getJarPath() {
        try {
            // Get the location of the current class
            String classPath = AppRestarter.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            
            // Check if it's a JAR file
            if (classPath.endsWith(".jar")) {
                return classPath;
            }
        } catch (Exception e) {
            LOGGER.fine("Could not determine JAR path: " + e.getMessage());
        }
        return null;
    }

    /**
     * Restarts the application when running from an IDE.
     * This is a fallback approach for development.
     */
    private static void restartInIde() {
        LOGGER.info("Restarting application (IDE mode)...");
        
        // Use Runtime.exec to spawn a new JVM with the main method
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-cp",
                    classpath,
                    "com.github.jrohatsch.moqqa.Main"
            );
            
            pb.inheritIO();
            pb.start();
            
            System.exit(0);
        } catch (IOException e) {
            LOGGER.severe("Failed to restart in IDE mode: " + e.getMessage());
            throw new RuntimeException("Failed to restart application", e);
        }
    }
}

