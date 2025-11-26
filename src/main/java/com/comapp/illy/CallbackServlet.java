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

        if (code == null || code.trim().isEmpty()) {
            logger.warn("Authorization code not received from Genesys");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Hata: Genesys'ten yetki kodu gelmedi.");
            return;
        }

        try {
            String tokenUrl = "https://login." + GenesysConfig.getRegion() + "/oauth/token";

            // GÜNCELLEME: Getter metotlarını kullanıyoruz
            String clientId = GenesysConfig.getClientId();
            String clientSecret = GenesysConfig.getClientSecret();
            String redirectUri = GenesysConfig.getRedirectUri();
            
            // Validate credentials
            if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
                logger.error("Genesys credentials are not configured");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Sistem yapılandırma hatası. Lütfen yöneticinize başvurun.");
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

            HttpRequest authRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());

            if (authResponse.statusCode() == 200) {
                JSONObject json = new JSONObject(authResponse.body());
                String accessToken = json.getString("access_token");

                HttpSession session = request.getSession();
                session.setAttribute("genesysUser", accessToken);
                
                logger.info("User successfully authenticated via Genesys OAuth");
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                logger.error("OAuth token request failed with status: {}", authResponse.statusCode());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Login Başarısız! HTTP Kodu: " + authResponse.statusCode());
            }

        } catch (InterruptedException e) {
            logger.error("OAuth request interrupted", e);
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("İşlem kesildi.");
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth callback", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Sistem Hatası. Lütfen yöneticinize başvurun.");
        }
    }
}