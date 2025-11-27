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
        String userAgent = request.getHeader("User-Agent");

        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║  OAUTH CALLBACK - User Returning from Genesys Cloud      ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("Callback received from IP: {}", remoteAddr);
        logger.debug("User-Agent: {}", userAgent != null ? userAgent.substring(0, Math.min(100, userAgent.length())) : "Unknown");
        
        if (error != null) {
            String errorDesc = request.getParameter("error_description");
            logger.error("✗ OAUTH ERROR RECEIVED FROM GENESYS");
            logger.error("  • Error Code: {}", error);
            logger.error("  • Description: {}", errorDesc != null ? errorDesc : "No description provided");
            logger.error("  • Source IP: {}", remoteAddr);
            logger.error("User authentication failed - OAuth provider returned error");
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("OAuth Error: " + error);
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            logger.warn("✗ AUTHORIZATION CODE MISSING");
            logger.warn("  • Expected parameter 'code' not found in callback");
            logger.warn("  • Source IP: {}", remoteAddr);
            logger.warn("  • This may indicate a manual URL access or callback tampering");
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Authorization code not received from Genesys.");
            return;
        }
        
        logger.info("✓ Authorization code received successfully");
        logger.debug("  • Code length: {} characters", code.length());
        logger.debug("  • Code preview: {}...", code.substring(0, Math.min(10, code.length())));

        try {
            long authStartTime = System.currentTimeMillis();
            
            String tokenUrl = "https://login." + GenesysConfig.getRegion() + "/oauth/token";

            // Use getter methods from GenesysConfig
            String clientId = GenesysConfig.getClientId();
            String clientSecret = GenesysConfig.getClientSecret();
            String redirectUri = GenesysConfig.getRedirectUri();
            
            logger.info("━━━ Step 1: Exchanging Authorization Code for Access Token ━━━");
            logger.info("Token endpoint: {}", tokenUrl);
            logger.debug("Client ID: {}***", clientId != null && clientId.length() > 8 ? clientId.substring(0, 8) : "N/A");
            
            // Validate credentials
            if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
                logger.error("✗ CONFIGURATION ERROR - Genesys credentials missing!");
                logger.error("  • Client ID empty: {}", (clientId == null || clientId.isEmpty()));
                logger.error("  • Client Secret empty: {}", (clientSecret == null || clientSecret.isEmpty()));
                logger.error("  • Check configuration file: C:/IllySurvey/Config/IllySurveyAdmin.properties");
                
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("System configuration error. Please contact your administrator.");
                return;
            }

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

            logger.debug("Preparing token exchange request:");
            logger.debug("  • Grant Type: authorization_code");
            logger.debug("  • Redirect URI: {}", redirectUri);
            logger.debug("  • Authentication: Basic (Base64 encoded credentials)");

            HttpRequest authRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            logger.info("→ Sending token exchange request to Genesys...");
            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());
            
            int tokenStatusCode = authResponse.statusCode();
            long tokenRequestTime = System.currentTimeMillis() - authStartTime;
            
            logger.info("← Token exchange response received ({}ms)", tokenRequestTime);
            logger.debug("  • HTTP Status: {}", tokenStatusCode);

            if (authResponse.statusCode() == 200) {
                JSONObject json = new JSONObject(authResponse.body());
                String accessToken = json.getString("access_token");
                
                logger.info("✓ Access token obtained successfully");
                logger.debug("  • Token length: {} characters", accessToken.length());
                logger.debug("  • Token type: {}", json.optString("token_type", "Bearer"));
                logger.debug("  • Expires in: {} seconds", json.optInt("expires_in", 0));

                HttpSession session = request.getSession();
                String sessionId = session.getId();
                session.setAttribute("genesysUser", accessToken);
                
                logger.info("━━━ Step 2: Storing Access Token in HTTP Session ━━━");
                logger.info("✓ Token stored successfully");
                logger.debug("  • Session ID: {}", sessionId);
                logger.debug("  • Session creation time: {}", new java.util.Date(session.getCreationTime()));
                logger.debug("  • Session max inactive interval: {} minutes", session.getMaxInactiveInterval() / 60);
                
                // Try to get user information and store in session
                logger.info("━━━ Step 3: Retrieving User Information from Genesys ━━━");
                try {
                    long userInfoStartTime = System.currentTimeMillis();
                    String userInfoUrl = "https://api." + GenesysConfig.getRegion() + "/api/v2/users/me";
                    
                    logger.info("→ Requesting user information...");
                    logger.debug("  • API Endpoint: {}", userInfoUrl);
                    logger.debug("  • Using access token for authorization");
                    
                    HttpRequest userRequest = HttpRequest.newBuilder()
                        .uri(URI.create(userInfoUrl))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();
                    
                    HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
                    
                    long userInfoRequestTime = System.currentTimeMillis() - userInfoStartTime;
                    int userInfoStatus = userResponse.statusCode();
                    
                    logger.info("← User info response received ({}ms)", userInfoRequestTime);
                    logger.debug("  • HTTP Status: {}", userInfoStatus);
                    
                    if (userResponse.statusCode() == 200) {
                        JSONObject userInfo = new JSONObject(userResponse.body());
                        String userName = userInfo.optString("name", "User");
                        String userEmail = userInfo.optString("email", "");
                        String userId = userInfo.optString("id", "");
                        String department = userInfo.optString("department", "");
                        String title = userInfo.optString("title", "");
                        
                        session.setAttribute("userName", userName);
                        session.setAttribute("userEmail", userEmail);
                        session.setAttribute("userId", userId); // Store user ID for token invalidation
                        
                        logger.info("╔═══════════════════════════════════════════════════════════╗");
                        logger.info("║  AUTHENTICATION SUCCESSFUL                                ║");
                        logger.info("╚═══════════════════════════════════════════════════════════╝");
                        logger.info("User Profile:");
                        logger.info("  • Name: {}", userName);
                        logger.info("  • Email: {}", userEmail);
                        logger.info("  • User ID: {}", userId);
                        if (!department.isEmpty()) {
                            logger.info("  • Department: {}", department);
                        }
                        if (!title.isEmpty()) {
                            logger.info("  • Title: {}", title);
                        }
                        logger.info("Session Information:");
                        logger.info("  • Session ID: {}", sessionId);
                        logger.info("  • IP Address: {}", remoteAddr);
                        logger.info("  • Total Auth Time: {}ms", System.currentTimeMillis() - authStartTime);
                        logger.info("═══════════════════════════════════════════════════════════");
                    } else {
                        String responseBody = userResponse.body();
                        logger.warn("✗ Could not retrieve detailed user information");
                        logger.warn("  • Status Code: {}", userInfoStatus);
                        logger.warn("  • Response: {}", responseBody != null && responseBody.length() > 200 ? 
                                   responseBody.substring(0, 200) + "..." : responseBody);
                        logger.info("Continuing with basic authentication (token only)");
                        logger.info("Session ID: {}, IP: {}", sessionId, remoteAddr);
                    }
                } catch (Exception e) {
                    logger.warn("✗ Exception during user info retrieval", e);
                    logger.warn("  • Error Type: {}", e.getClass().getSimpleName());
                    logger.warn("  • Error Message: {}", e.getMessage());
                    logger.info("Continuing with basic authentication (token only)");
                    logger.debug("Full error details:", e);
                    logger.info("Session created successfully - SessionID: {}", sessionId);
                }
                
                logger.info("━━━ Step 4: Redirecting to Admin Dashboard ━━━");
                logger.info("→ Sending user to: {}/admin", request.getContextPath());
                response.sendRedirect(request.getContextPath() + "/admin");
                logger.info("✓ OAuth authentication flow completed successfully");
                
            } else {
                String responseBody = authResponse.body();
                
                logger.error("╔═══════════════════════════════════════════════════════════╗");
                logger.error("║  OAUTH TOKEN REQUEST FAILED                               ║");
                logger.error("╚═══════════════════════════════════════════════════════════╝");
                logger.error("Token Exchange Error:");
                logger.error("  • HTTP Status: {}", authResponse.statusCode());
                logger.error("  • Response Body: {}", responseBody != null && responseBody.length() > 500 ? 
                           responseBody.substring(0, 500) + "..." : responseBody);
                logger.error("  • Source IP: {}", remoteAddr);
                
                try {
                    JSONObject errorJson = new JSONObject(responseBody);
                    logger.error("  • Error: {}", errorJson.optString("error", "Unknown"));
                    logger.error("  • Error Description: {}", errorJson.optString("error_description", "No description"));
                } catch (Exception e) {
                    logger.debug("Could not parse error response as JSON", e);
                }
                
                logger.error("Common causes:");
                logger.error("  1. Invalid or expired authorization code");
                logger.error("  2. Incorrect client credentials");
                logger.error("  3. Mismatched redirect URI");
                logger.error("  4. Network connectivity issues");
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Login Failed! HTTP Status: " + authResponse.statusCode());
            }

        } catch (InterruptedException e) {
            logger.error("╔═══════════════════════════════════════════════════════════╗");
            logger.error("║  OAUTH REQUEST INTERRUPTED                                ║");
            logger.error("╚═══════════════════════════════════════════════════════════╝");
            logger.error("Thread Interruption Details:");
            logger.error("  • Source IP: {}", remoteAddr);
            logger.error("  • Error: {}", e.getMessage());
            logger.error("  • Thread: {}", Thread.currentThread().getName());
            logger.debug("Full stack trace:", e);
            
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Operation was interrupted.");
            
        } catch (Exception e) {
            logger.error("╔═══════════════════════════════════════════════════════════╗");
            logger.error("║  UNEXPECTED ERROR DURING OAUTH CALLBACK                   ║");
            logger.error("╚═══════════════════════════════════════════════════════════╝");
            logger.error("Exception Details:");
            logger.error("  • Type: {}", e.getClass().getName());
            logger.error("  • Message: {}", e.getMessage());
            logger.error("  • Source IP: {}", remoteAddr);
            logger.error("  • Cause: {}", e.getCause() != null ? e.getCause().getMessage() : "None");
            logger.error("Full stack trace:", e);
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("System Error. Please contact your administrator.");
        }
    }
}