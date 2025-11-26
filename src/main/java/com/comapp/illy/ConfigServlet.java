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
        System.out.println("ConfigServlet starting...");

        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            properties.load(input);
            
            // 1. Get Log4j XML file path from properties
            String logConfigPath = properties.getProperty("log4j2-properties-location");
            
            if (logConfigPath != null && !logConfigPath.isEmpty()) {
                
                // Convert file path to secure URI format
                File logFile = new File(logConfigPath);
                
                if (logFile.exists()) {
                    // Convert "C:/..." to "file:/C:/..." format to avoid errors
                    Configurator.initialize(null, logFile.toURI().toString());
                    
                    // Now logger can be safely initialized
                    logger = LogManager.getLogger(ConfigServlet.class);
                    logger.info("Log4j configured successfully. XML Path: " + logFile.getAbsolutePath());
                } else {
                    System.err.println("ERROR: Specified Log file does not exist on disk: " + logConfigPath);
                }

            } else {
                System.err.println("WARNING: 'log4j2-properties-location' not found in properties file!");
            }
            
            if(logger != null) logger.info("All settings loaded. Config Path: " + CONFIG_PATH);

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load configuration file: " + e.getMessage());
            if(logger != null) {
                logger.error("Error while loading configuration", e);
            }
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}