package com.comapp.illy;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/oauth/callback")
public class CallbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(CallbackServlet.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        String remoteAddr = request.getRemoteAddr();

        logger.info("OAuth callback received from IP: {}", remoteAddr);
        
        if (error != null) {
            logger.error("OAuth error received: {} - Description: {}", error, request.getParameter("error_description"));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("OAuth Error: " + error);
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            logger.warn("Authorization code not received from Genesys - IP: {}", remoteAddr);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Authorization code not received from Genesys.");
            return;
        }
        
        logger.debug("Authorization code received (length: {})", code.length());

        try {
            String tokenUrl = "https://login." + GenesysConfig.getRegion() + "/oauth/token";

            // Use getter methods from GenesysConfig
            String clientId = GenesysConfig.getClientId();
            String clientSecret = GenesysConfig.getClientSecret();
            String redirectUri = GenesysConfig.getRedirectUri();
            
            // Validate credentials
            if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
                logger.error("Genesys credentials are not configured - ClientID empty: {}, ClientSecret empty: {}", 
                           (clientId == null || clientId.isEmpty()), (clientSecret == null || clientSecret.isEmpty()));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("System configuration error. Please contact your administrator.");
                return;
            }

            logger.debug("Requesting access token from: {}", tokenUrl);

            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            Map<String, String> params = Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", redirectUri
            );
            
            String formBody = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

            HttpRequest authRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            logger.debug("Sending token request to Genesys");
            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());
            logger.debug("Token response received with status: {}", authResponse.statusCode());

            if (authResponse.statusCode() == 200) {
                JSONObject json = new JSONObject(authResponse.body());
                String accessToken = json.getString("access_token");
                logger.debug("Access token received successfully (length: {})", accessToken.length());

                HttpSession session = request.getSession();
                String sessionId = session.getId();
                session.setAttribute("genesysUser", accessToken);
                logger.info("Access token stored in session: {}", sessionId);
                
                // Try to get user information and store in session
                try {
                    String userInfoUrl = "https://api." + GenesysConfig.getRegion() + "/api/v2/users/me";
                    logger.debug("Fetching user info from: {}", userInfoUrl);
                    
                    HttpRequest userRequest = HttpRequest.newBuilder()
                        .uri(URI.create(userInfoUrl))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();
                    
                    HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
                    logger.debug("User info response status: {}", userResponse.statusCode());
                    
                    if (userResponse.statusCode() == 200) {
                        JSONObject userInfo = new JSONObject(userResponse.body());
                        String userName = userInfo.optString("name", "User");
                        String userEmail = userInfo.optString("email", "");
                        String userId = userInfo.optString("id", "");
                        
                        session.setAttribute("userName", userName);
                        session.setAttribute("userEmail", userEmail);
                        
                        logger.info("User authentication successful - Name: {}, Email: {}, UserID: {}, SessionID: {}", 
                                   userName, userEmail, userId, sessionId);
                    } else {
                        logger.warn("Could not retrieve user info (status: {}), continuing with token only", 
                                   userResponse.statusCode());
                        logger.info("User successfully authenticated via Genesys OAuth (SessionID: {})", sessionId);
                    }
                } catch (Exception e) {
                    logger.warn("Error retrieving user information, continuing with token only - Error: {}", e.getMessage());
                    logger.debug("User info retrieval error details", e);
                    logger.info("User successfully authenticated via Genesys OAuth (SessionID: {})", sessionId);
                }
                
                logger.info("Redirecting authenticated user to admin panel");
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                String responseBody = authResponse.body();
                logger.error("OAuth token request failed - Status: {}, Response: {}", 
                           authResponse.statusCode(), responseBody);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Login Failed! HTTP Status: " + authResponse.statusCode());
            }

        } catch (InterruptedException e) {
            logger.error("OAuth request interrupted from IP: {}", remoteAddr, e);
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Operation was interrupted.");
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth callback from IP: {}", remoteAddr, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("System Error. Please contact your administrator.");
        }
    }
}