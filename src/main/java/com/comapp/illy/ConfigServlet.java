package com.comapp.illy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(urlPatterns = "/config", loadOnStartup = 1)
public class ConfigServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Initialize logger as null, will be initialized after configuration
    private static Logger logger;
    
    private static Properties properties = new Properties();
    
    // Configuration file path location
    private static final String CONFIG_PATH = "C:/IllySurvey/Config/IllySurveyAdmin.properties";

    @Override
    public void init() throws ServletException {
        // Log4j not ready yet, use System.out for output
        System.out.println("===========================================================");
        System.out.println("   ILLY SURVEY ADMIN - Application Startup");
        System.out.println("===========================================================");
        System.out.println("ConfigServlet initializing...");
        System.out.println("Configuration file location: " + CONFIG_PATH);

        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            long startTime = System.currentTimeMillis();
            
            properties.load(input);
            int propertyCount = properties.size();
            
            System.out.println("[+] Configuration file loaded successfully");
            System.out.println("  - Total properties loaded: " + propertyCount);
            
            // 1. Get Log4j XML file path from properties
            String logConfigPath = properties.getProperty("log4j2-properties-location");
            
            if (logConfigPath != null && !logConfigPath.isEmpty()) {
                System.out.println("  - Log4j2 configuration path: " + logConfigPath);
                
                // Convert file path to secure URI format
                File logFile = new File(logConfigPath);
                
                if (logFile.exists()) {
                    System.out.println("  - Log4j2 XML file found: " + logFile.getAbsolutePath());
                    System.out.println("  - File size: " + logFile.length() + " bytes");
                    
                    // Convert "C:/..." to "file:/C:/..." format to avoid errors
                    Configurator.initialize(null, logFile.toURI().toString());
                    
                    // Now logger can be safely initialized
                    logger = LogManager.getLogger(ConfigServlet.class);
                    
                    logger.info("===========================================================");
                    logger.info("   ILLY SURVEY ADMIN - Logging System Active");
                    logger.info("===========================================================");
                    logger.info("[+] Log4j2 configured successfully");
                    logger.info("  - XML Configuration: {}", logFile.getAbsolutePath());
                    logger.info("  - File size: {} bytes", logFile.length());
                    logger.info("  - Last modified: {}", new java.util.Date(logFile.lastModified()));
                } else {
                    System.err.println("[-] ERROR: Log4j2 XML file not found!");
                    System.err.println("  Expected location: " + logConfigPath);
                    System.err.println("  Absolute path checked: " + logFile.getAbsolutePath());
                    System.err.println("  Please ensure the file exists and path is correct");
                }

            } else {
                System.err.println("[-] WARNING: 'log4j2-properties-location' property not found!");
                System.err.println("  Logging may not work properly");
                System.err.println("  Please add log4j2-properties-location to " + CONFIG_PATH);
            }
            
            if(logger != null) {
                logger.info("===========================================================");
                logger.info("   Application Configuration Summary");
                logger.info("===========================================================");
                logger.info("Configuration loaded from: {}", CONFIG_PATH);
                logger.info("Total properties: {}", propertyCount);
                
                // Log important configuration values (without sensitive data)
                logger.info("Key configurations:");
                logger.info("  - Genesys Region: {}", properties.getProperty("genesys-region", "NOT SET"));
                logger.info("  - Redirect URI: {}", properties.getProperty("genesys-redirect-uri", "NOT SET"));
                logger.info("  - Database Host: {}", properties.getProperty("db-host", "NOT SET"));
                logger.info("  - Database Name: {}", properties.getProperty("db-name", "NOT SET"));
                
                // Warn about missing critical properties
                String[] criticalProps = {"genesys-client-id", "genesys-client-secret", "db-host", "db-name", "db-username"};
                for (String prop : criticalProps) {
                    if (properties.getProperty(prop) == null || properties.getProperty(prop).isEmpty()) {
                        logger.warn("[-] MISSING CRITICAL PROPERTY: {} - Application may not function correctly!", prop);
                    }
                }
                
                long loadTime = System.currentTimeMillis() - startTime;
                logger.info("Configuration loading completed in {}ms", loadTime);
                logger.info("===========================================================");
            }

        } catch (IOException e) {
            System.err.println("[-] CRITICAL ERROR: Failed to load configuration file!");
            System.err.println("  File path: " + CONFIG_PATH);
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  Please ensure:");
            System.err.println("    1. File exists at the specified location");
            System.err.println("    2. File has read permissions");
            System.err.println("    3. File is in correct properties format");
            
            if(logger != null) {
                logger.error("[-] FATAL: Configuration file could not be loaded", e);
                logger.error("  Expected location: {}", CONFIG_PATH);
                logger.error("  IO Error: {}", e.getMessage());
            }
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}