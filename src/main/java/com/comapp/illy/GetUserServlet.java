package com.comapp.illy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/getuser")
public class GetUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(GetUserServlet.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String remoteAddr = request.getRemoteAddr();
        
        logger.debug("GetUser API request from IP: {}", remoteAddr);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (session == null || session.getAttribute("genesysUser") == null) {
            logger.warn("Unauthorized GetUser request from IP: {} - No valid session", remoteAddr);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Not authenticated\"}");
            return;
        }
        
        String sessionId = session.getId();
        logger.debug("GetUser request for session: {}", sessionId);
        
        // First, check if user info is already in session (cached from login)
        String cachedName = (String) session.getAttribute("userName");
        String cachedEmail = (String) session.getAttribute("userEmail");
        
        if (cachedName != null && !cachedName.isEmpty()) {
            // Return cached user info
            JSONObject responseJson = new JSONObject();
            responseJson.put("name", cachedName);
            responseJson.put("email", cachedEmail != null ? cachedEmail : "");
            responseJson.put("username", "");
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseJson.toString());
            logger.debug("User info retrieved from session cache - User: {}, SessionID: {}", cachedName, sessionId);
            return;
        }
        
        logger.info("User info not in cache, fetching from Genesys API - SessionID: {}", sessionId);
        
        // If not in cache, fetch from API
        String accessToken = (String) session.getAttribute("genesysUser");
        
        try {
            // Get user info from Genesys API
            String userInfoUrl = "https://api." + GenesysConfig.getRegion() + "/api/v2/users/me";
            
            logger.debug("Requesting user info from: {}", userInfoUrl);
            
            HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(userInfoUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
            
            logger.debug("Genesys user info response status: {}", userResponse.statusCode());
            
            if (userResponse.statusCode() == 200) {
                JSONObject userInfo = new JSONObject(userResponse.body());
                
                // Extract user information
                String name = userInfo.optString("name", "User");
                String email = userInfo.optString("email", "");
                String username = userInfo.optString("username", "");
                String userId = userInfo.optString("id", "");
                
                // Cache in session for future requests
                session.setAttribute("userName", name);
                session.setAttribute("userEmail", email);
                
                logger.info("User info cached - Name: {}, Email: {}, UserID: {}, SessionID: {}", 
                           name, email, userId, sessionId);
                
                // Create simplified response
                JSONObject responseJson = new JSONObject();
                responseJson.put("name", name);
                responseJson.put("email", email);
                responseJson.put("username", username);
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(responseJson.toString());
                
                logger.info("User info retrieved and returned successfully - User: {}", name);
            } else {
                String responseBody = userResponse.body();
                logger.error("Failed to retrieve user info - Status: {}, Response: {}, SessionID: {}", 
                           userResponse.statusCode(), responseBody, sessionId);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to retrieve user information\"}");
            }
            
        } catch (InterruptedException e) {
            logger.error("User info request interrupted - SessionID: {}", sessionId, e);
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Request interrupted\"}");
        } catch (Exception e) {
            logger.error("Error retrieving user information - SessionID: {}", sessionId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }
}
