package com.comapp.illy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Session Listener - Monitors session lifecycle events
 * Logs when sessions are created and destroyed
 */
@WebListener
public class SessionListener implements HttpSessionListener {
    private static final Logger logger = LogManager.getLogger(SessionListener.class);
    private static int activeSessions = 0;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions++;
        logger.info("Session created. Session ID: {}. Active sessions: {}", 
                   se.getSession().getId(), activeSessions);
        
        // Set default session timeout to 30 minutes
        se.getSession().setMaxInactiveInterval(30 * 60);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions--;
        
        String sessionId = se.getSession().getId();
        String username = "Unknown";
        
        try {
            Object userNameObj = se.getSession().getAttribute("userName");
            if (userNameObj != null) {
                username = userNameObj.toString();
            } else {
                Object userObj = se.getSession().getAttribute("genesysUser");
                if (userObj != null) {
                    username = "User with token";
                }
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve session attributes", e);
        }
        
        logger.info("Session destroyed for user: {}. Session ID: {}. Active sessions: {}", 
                   username, sessionId, activeSessions);
    }

    /**
     * Get the number of currently active sessions
     */
    public static int getActiveSessions() {
        return activeSessions;
    }
}
