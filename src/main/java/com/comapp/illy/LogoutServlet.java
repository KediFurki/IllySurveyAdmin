package com.comapp.illy;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

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
        String sessionId = null;
        
        if (session != null) {
            sessionId = session.getId();
            String username = "Unknown";
            String userEmail = "";
            
            // Try to get user info before invalidating session
            try {
                accessToken = (String) session.getAttribute("genesysUser");
                
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
                
                logger.info("Logout initiated - User: {}, Email: {}, SessionID: {}, IP: {}", 
                           username, userEmail, sessionId, remoteAddr);
            } catch (Exception e) {
                logger.warn("Could not retrieve username during logout - SessionID: {}", sessionId, e);
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
        
        // Logout from Genesys if we have an access token
        if (accessToken != null && !accessToken.isEmpty()) {
            logger.info("Attempting to revoke Genesys token - SessionID: {}", sessionId);
            try {
                logoutFromGenesys(accessToken);
            } catch (Exception e) {
                logger.warn("Failed to logout from Genesys, continuing with local logout - SessionID: {}", sessionId, e);
            }
        } else {
            logger.debug("No access token to revoke");
        }
        
        // Redirect to login page with logout message
        logger.info("Logout completed successfully, redirecting to login page - SessionID: {}", sessionId);
        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=success");
    }
    
    /**
     * Logout from Genesys Cloud by revoking the access token
     */
    private void logoutFromGenesys(String accessToken) {
        logger.debug("Starting Genesys token revocation process");
        
        try {
            String region = GenesysConfig.getRegion();
            String tokenRevokeUrl = "https://login." + region + "/oauth/revoke";
            
            logger.debug("Token revocation URL: {}", tokenRevokeUrl);
            
            String clientId = GenesysConfig.getClientId();
            String clientSecret = GenesysConfig.getClientSecret();
            
            if (clientId == null || clientSecret == null) {
                logger.warn("Cannot revoke Genesys token - credentials not configured (ClientID: {}, ClientSecret: {})",
                           (clientId == null ? "null" : "exists"), (clientSecret == null ? "null" : "exists"));
                return;
            }
            
            logger.debug("Preparing token revocation request");
            
            // Prepare form data for token revocation
            String formData = "token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                            "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                            "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
            
            HttpRequest revokeRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenRevokeUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
            
            logger.debug("Sending token revocation request to Genesys");
            HttpResponse<String> revokeResponse = httpClient.send(revokeRequest, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = revokeResponse.statusCode();
            logger.debug("Token revocation response status: {}", statusCode);
            
            if (statusCode == 200) {
                logger.info("Successfully revoked Genesys access token - Region: {}", region);
            } else {
                String responseBody = revokeResponse.body();
                logger.warn("Genesys token revocation returned non-200 status: {} - Response: {}", 
                           statusCode, responseBody);
            }
            
        } catch (InterruptedException e) {
            logger.warn("Token revocation request interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error revoking Genesys token - Error type: {}, Message: {}", 
                        e.getClass().getSimpleName(), e.getMessage());
            logger.debug("Token revocation error details", e);
        }
    }
}
