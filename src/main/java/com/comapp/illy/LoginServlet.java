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
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║  ILLY SURVEY ADMIN - User Login Initiated                 ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("Login request details:");
        logger.info("  • Source IP: {}", remoteAddr);
        logger.info("  • Referer: {}", referer != null ? referer : "Direct access");
        logger.info("  • User Agent: {}", userAgent != null ? userAgent.substring(0, Math.min(100, userAgent.length())) : "Unknown");
        
        try {
            // Using GenesysConfig getter methods instead of direct field access
            String region = GenesysConfig.getRegion();
            String clientId = GenesysConfig.getClientId();
            String redirectUri = GenesysConfig.getRedirectUri();
            
            logger.info("Genesys OAuth Configuration:");
            logger.info("  • Region: {}", region);
            logger.info("  • Redirect URI: {}", redirectUri);
            logger.info("  • Client ID: {}***", clientId != null && clientId.length() > 8 ? clientId.substring(0, 8) : "N/A");
            
            if (clientId == null || clientId.isEmpty()) {
                logger.error("✗ CONFIGURATION ERROR - Genesys Client ID is not configured!");
                logger.error("Please check C:/IllySurvey/Config/IllySurveyAdmin.properties file");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                  "System configuration error. Please contact administrator.");
                return;
            }
            
            String loginUrl = "https://login." + region + "/oauth/authorize" +
                    "?client_id=" + clientId +
                    "&response_type=code" +
                    "&redirect_uri=" + redirectUri;
            
            logger.info("OAuth 2.0 Authorization Flow Starting:");
            logger.info("  • Authorization Server: https://login.{}", region);
            logger.info("  • Grant Type: Authorization Code");
            logger.info("  • Expected Callback: {}", redirectUri);
            
            logger.debug("Full OAuth URL (sanitized): {}", loginUrl.replace(clientId, "***CLIENT_ID***"));
            
            logger.info("→ Redirecting user to Genesys Cloud login page...");
            logger.info("User will authenticate via Genesys and return to callback endpoint");
            
            response.sendRedirect(loginUrl);
            
            logger.info("✓ Redirect successful - User sent to Genesys OAuth provider");
            
        } catch (Exception e) {
            logger.error("✗ CRITICAL ERROR during login redirect process", e);
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                              "An error occurred during login. Please try again.");
        }
    }
}