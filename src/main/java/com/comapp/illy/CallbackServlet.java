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

    // Modern HTTP Client using Java 21
    private final HttpClient httpClient = HttpClient.newHttpClient();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");

        if (code == null || code.trim().isEmpty()) {
            logger.warn("Authorization code not received from Genesys");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Error: Authorization code not received from Genesys.");
            return;
        }

        try {
            // 1. Token URL
            String tokenUrl = "https://login." + GenesysConfig.REGION_DOMAIN + "/oauth/token";

            // 2. Prepare Auth Header (Basic Auth)
            String auth = GenesysConfig.CLIENT_ID + ":" + GenesysConfig.CLIENT_SECRET;
            
            if ((GenesysConfig.CLIENT_ID == null || GenesysConfig.CLIENT_ID.isEmpty()) || 
                (GenesysConfig.CLIENT_SECRET == null || GenesysConfig.CLIENT_SECRET.isEmpty())) {
                logger.error("Genesys credentials are not configured");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("System configuration error. Please contact administrator.");
                return;
            }
            
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 3. Prepare Request Body (x-www-form-urlencoded format)
            Map<String, String> params = Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", GenesysConfig.REDIRECT_URI
            );
            
            // Convert Map to "key=value&key2=value2" format
            String formBody = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

            // 4. Build Request (Java 21 Style)
            HttpRequest authRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            logger.info("Sending OAuth token request to Genesys");

            // 5. Send Request and Get Response
            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());

            if (authResponse.statusCode() == 200) {
                // 6. Parse JSON
                JSONObject json = new JSONObject(authResponse.body());
                String accessToken = json.getString("access_token");

                // 7. Start Session
                HttpSession session = request.getSession();
                session.setAttribute("genesysUser", accessToken);
                
                logger.info("User successfully authenticated and session created");

                // 8. Redirect to Admin Panel
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                logger.error("OAuth token request failed with status: {}", authResponse.statusCode());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Login Failed! HTTP Status: " + authResponse.statusCode());
            }

        } catch (InterruptedException e) {
            logger.error("OAuth request interrupted", e);
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Operation was interrupted.");
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth callback", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("System Error: Contact administrator.");
        }
    }
}