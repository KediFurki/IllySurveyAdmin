package com.comapp.illy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Session Listener - Monitors session lifecycle events
 * Tracks user sessions and provides statistics for monitoring
 * Session timeout: 30 minutes of inactivity
 */
@WebListener
public class SessionListener implements HttpSessionListener {
    private static final Logger logger = LogManager.getLogger(SessionListener.class);
    private static int activeSessions = 0;
    private static int totalSessionsCreated = 0;
    private static long applicationStartTime = System.currentTimeMillis();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions++;
        totalSessionsCreated++;
        
        String sessionId = se.getSession().getId();
        int maxInactiveInterval = 30 * 60; // 30 minutes
        
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║  NEW SESSION CREATED                                      ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("Session Details:");
        logger.info("  • Session ID: {}", sessionId);
        logger.info("  • Creation Time: {}", new java.util.Date(se.getSession().getCreationTime()));
        logger.info("  • Max Inactive Interval: {} minutes", maxInactiveInterval / 60);
        logger.info("  • Active Sessions: {}", activeSessions);
        logger.info("  • Total Sessions Since Startup: {}", totalSessionsCreated);
        
        // Set default session timeout to 30 minutes
        se.getSession().setMaxInactiveInterval(maxInactiveInterval);
        
        logger.debug("Session timeout configured: {} seconds", maxInactiveInterval);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions--;
        
        String sessionId = se.getSession().getId();
        String username = "Unknown";
        String userEmail = "";
        long sessionDuration = 0;
        
        try {
            long creationTime = se.getSession().getCreationTime();
            long destroyTime = System.currentTimeMillis();
            sessionDuration = (destroyTime - creationTime) / 1000; // seconds
            
            Object userNameObj = se.getSession().getAttribute("userName");
            Object userEmailObj = se.getSession().getAttribute("userEmail");
            
            if (userNameObj != null) {
                username = userNameObj.toString();
            }
            if (userEmailObj != null) {
                userEmail = userEmailObj.toString();
            }
            
            if (username.equals("Unknown")) {
                Object userObj = se.getSession().getAttribute("genesysUser");
                if (userObj != null) {
                    username = "Authenticated User (token exists)";
                }
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve session attributes during destruction", e);
        }
        
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║  SESSION DESTROYED                                        ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("Session Details:");
        logger.info("  • User: {}", username);
        if (!userEmail.isEmpty()) {
            logger.info("  • Email: {}", userEmail);
        }
        logger.info("  • Session ID: {}", sessionId);
        logger.info("  • Session Duration: {} minutes ({} seconds)", sessionDuration / 60, sessionDuration);
        logger.info("  • Remaining Active Sessions: {}", activeSessions);
        logger.info("  • Total Sessions Since Startup: {}", totalSessionsCreated);
        
        // Calculate application uptime
        long uptime = (System.currentTimeMillis() - applicationStartTime) / 1000;
        logger.info("  • Application Uptime: {} hours {} minutes", uptime / 3600, (uptime % 3600) / 60);
        
        if (activeSessions == 0) {
            logger.info("⚠ No active sessions remaining - All users logged out");
        }
    }

    /**
     * Get the number of currently active sessions
     */
    public static int getActiveSessions() {
        return activeSessions;
    }
    
    /**
     * Get total number of sessions created since application start
     */
    public static int getTotalSessionsCreated() {
        return totalSessionsCreated;
    }
    
    /**
     * Get application uptime in seconds
     */
    public static long getApplicationUptime() {
        return (System.currentTimeMillis() - applicationStartTime) / 1000;
    }
}
