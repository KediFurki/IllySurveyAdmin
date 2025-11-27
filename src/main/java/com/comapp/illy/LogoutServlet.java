package com.comapp.illy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    private final HttpClient httpClient = HttpClient.newHttpClient();

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
        
        // Invalidate token from Genesys if we have both token and userId
        if (accessToken != null && !accessToken.isEmpty() && userId != null && !userId.isEmpty()) {
            logger.info("Attempting to invalidate Genesys token - UserID: {}, SessionID: {}", userId, sessionId);
            try {
                invalidateGenesysToken(accessToken, userId);
            } catch (Exception e) {
                logger.warn("Failed to invalidate Genesys token, continuing with local logout - SessionID: {}", sessionId, e);
            }
        } else {
            logger.debug("No token or userId to invalidate - Token exists: {}, UserID exists: {}", 
                        (accessToken != null && !accessToken.isEmpty()), (userId != null && !userId.isEmpty()));
        }
        
        // Redirect to login page with logout message
        logger.info("Logout completed successfully, redirecting to login page - SessionID: {}", sessionId);
        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=success");
    }
    
    /**
     * Invalidate token from Genesys Cloud by deleting it via API
     * Uses DELETE /api/v2/tokens/{userId} endpoint
     * This is more effective than OAuth revoke as it deletes the token completely
     * Based on: https://api.mypurecloud.de/api/v2/tokens/{userId}
     */
    private void invalidateGenesysToken(String accessToken, String userId) {
        logger.debug("Starting Genesys token invalidation process for userId: {}", userId);
        
        try {
            String region = GenesysConfig.getRegion();
            // Use Genesys API to delete the token directly
            String tokenDeleteUrl = "https://api." + region + "/api/v2/tokens/" + userId;
            
            logger.debug("Token delete URL: {}", tokenDeleteUrl);
            logger.info("Invalidating token via DELETE {} for userId: {}", tokenDeleteUrl, userId);
            
            // Make DELETE request to Genesys API
            HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenDeleteUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
            
            logger.debug("Sending DELETE request to Genesys API");
            HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = deleteResponse.statusCode();
            logger.debug("Token deletion response status: {}", statusCode);
            
            // Genesys returns 200 or 204 for successful deletion
            if (statusCode == 200) {
                logger.info("Successfully invalidated Genesys token (200 OK) - UserID: {}, Region: {}", userId, region);
            } else if (statusCode == 204) {
                // 204 No Content is also success
                logger.info("Successfully invalidated Genesys token (204 No Content) - UserID: {}, Region: {}", userId, region);
            } else if (statusCode == 404) {
                // Token already deleted or doesn't exist
                logger.warn("Token not found (404) - may already be deleted - UserID: {}", userId);
            } else {
                String responseBody = deleteResponse.body();
                logger.warn("Token invalidation returned status: {} - UserID: {}, Response: {}", 
                           statusCode, userId, responseBody);
            }
            
        } catch (InterruptedException e) {
            logger.warn("Token invalidation request interrupted - UserID: {}", userId, e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error invalidating Genesys token - UserID: {}, Error type: {}, Message: {}", 
                        userId, e.getClass().getSimpleName(), e.getMessage());
            logger.debug("Token invalidation error details", e);
        }
    }
}
