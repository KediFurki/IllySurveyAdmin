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

import org.json.JSONObject; // org.json kütüphanesi hala gerekli

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/oauth/callback")
public class CallbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Java 21'in modern HTTP İstemcisi
    private final HttpClient httpClient = HttpClient.newHttpClient();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");

        if (code == null) {
            response.getWriter().write("Hata: Genesys'ten yetki kodu gelmedi.");
            return;
        }

        try {
            // 1. Token URL'i
            String tokenUrl = "https://login." + GenesysConfig.REGION_DOMAIN + "/oauth/token";

            // 2. Auth Header Hazırla (Basic Auth)
            String auth = GenesysConfig.CLIENT_ID + ":" + GenesysConfig.CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 3. Body Verisini Hazırla (x-www-form-urlencoded formatında)
            Map<String, String> params = Map.of(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", GenesysConfig.REDIRECT_URI
            );
            
            // Map'i "key=value&key2=value2" formatına çeviren modern akış
            String formBody = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

            // 4. İsteği Oluştur (Java 21 Tarzı)
            HttpRequest authRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            // 5. İsteği Gönder ve Cevabı Al
            HttpResponse<String> authResponse = httpClient.send(authRequest, HttpResponse.BodyHandlers.ofString());

            if (authResponse.statusCode() == 200) {
                // 6. JSON'u Parçala
                JSONObject json = new JSONObject(authResponse.body());
                String accessToken = json.getString("access_token");

                // 7. Oturumu Başlat
                HttpSession session = request.getSession();
                session.setAttribute("genesysUser", accessToken);

                // 8. Admin Paneline Yönlendir
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                // Hata Durumu
                response.getWriter().write("Login Başarısız! HTTP Kod: " + authResponse.statusCode() + " Cevap: " + authResponse.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.getWriter().write("İşlem kesildi.");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Sistem Hatası: " + e.getMessage());
        }
    }
}