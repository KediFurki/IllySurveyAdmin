package com.comapp.illy;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleLogout(request, response);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String remoteAddr = request.getRemoteAddr();
        logger.info("Logout request received from IP: {}", remoteAddr);
        
        HttpSession session = request.getSession(false);
        String accessToken = null;
        String userId = null;
        String sessionId = null;
        
        if (session != null) {
            sessionId = session.getId();
            String username = "Unknown";
            String userEmail = "";
            
            // Try to get user info before invalidating session
            try {
                accessToken = (String) session.getAttribute("genesysUser");
                userId = (String) session.getAttribute("userId");
                
                Object userNameObj = session.getAttribute("userName");
                Object userEmailObj = session.getAttribute("userEmail");
                
                if (userNameObj != null) {
                    username = userNameObj.toString();
                }
                if (userEmailObj != null) {
                    userEmail = userEmailObj.toString();
                }
                
                if (username.equals("Unknown") && accessToken != null) {
                    username = "User with token: " + accessToken.substring(0, Math.min(10, accessToken.length())) + "...";
                }
                
                logger.info("Logout initiated - User: {}, Email: {}, UserID: {}, SessionID: {}, IP: {}", 
                           username, userEmail, userId, sessionId, remoteAddr);
            } catch (Exception e) {
                logger.warn("Could not retrieve user info during logout - SessionID: {}", sessionId, e);
            }
            
            logger.debug("Invalidating session: {}", sessionId);
            
            // Invalidate the session
            session.invalidate();
            
            logger.info("Session invalidated successfully - User: {}, SessionID: {}", username, sessionId);
        } else {
            logger.warn("Logout attempted but no active session found - IP: {}", remoteAddr);
        }
        
        // Set cache control headers to prevent back button access
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        logger.debug("Cache control headers set for logout response");
        
        // EMBEDDED APP MODE: Do NOT invalidate Genesys session
        // This allows the user to log back in without re-entering credentials
        logger.info("Embedded App Mode: Skipping Genesys token invalidation to maintain SSO session");
        
        // Redirect to login page with logout=true flag to prevent auto-login
        logger.info("Logout completed successfully, redirecting to login page - SessionID: {}", sessionId);
        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=true");
    }
}
