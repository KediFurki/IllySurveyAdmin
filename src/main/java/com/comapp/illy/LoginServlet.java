package com.comapp.illy;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(LoginServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String remoteAddr = request.getRemoteAddr();
        logger.info("Login request received from IP: {}", remoteAddr);
        
        try {
            // Using GenesysConfig getter methods instead of direct field access
            String region = GenesysConfig.getRegion();
            String clientId = GenesysConfig.getClientId();
            String redirectUri = GenesysConfig.getRedirectUri();
            
            if (clientId == null || clientId.isEmpty()) {
                logger.error("Genesys Client ID is not configured");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                  "System configuration error. Please contact administrator.");
                return;
            }
            
            String loginUrl = "https://login." + region + "/oauth/authorize" +
                    "?client_id=" + clientId +
                    "&response_type=code" +
                    "&redirect_uri=" + redirectUri;
            
            logger.info("Redirecting to Genesys OAuth - Region: {}, Redirect URI: {}", region, redirectUri);
            logger.debug("Full OAuth URL: {}", loginUrl.replace(clientId, "***CLIENT_ID***"));
            
            response.sendRedirect(loginUrl);
        } catch (Exception e) {
            logger.error("Error during login redirect", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                              "An error occurred during login. Please try again.");
        }
    }
}