package com.comapp.illy;

import java.io.BufferedReader;
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

@WebServlet("/api/datatable")
public class DataTableServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(DataTableServlet.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("genesysUser") == null) {
            logger.warn("Unauthorized DataTable access attempt - no valid session");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String accessToken = (String) session.getAttribute("genesysUser");
        String dataTableId = GenesysConfig.getDataTableId();
        String region = GenesysConfig.getRegion();

        if (dataTableId == null || dataTableId.isEmpty()) {
            logger.error("DataTable ID not configured in properties");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"DataTable ID not configured\"}");
            return;
        }

        String apiUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows?pageSize=100&showbrief=false", 
                                       region, dataTableId);

        logger.info("Fetching DataTable rows from Genesys API: {}", apiUrl);

        try {
            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            
            logger.info("Genesys API Response: Status {}", apiResponse.statusCode());

            response.setStatus(apiResponse.statusCode());
            response.setContentType("application/json");
            response.getWriter().write(apiResponse.body());

        } catch (Exception e) {
            logger.error("Error fetching DataTable rows", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("genesysUser") == null) {
            logger.warn("Unauthorized DataTable update attempt - no valid session");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String accessToken = (String) session.getAttribute("genesysUser");
        String dataTableId = GenesysConfig.getDataTableId();
        String region = GenesysConfig.getRegion();

        if (dataTableId == null || dataTableId.isEmpty()) {
            logger.error("DataTable ID not configured in properties");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"DataTable ID not configured\"}");
            return;
        }

        // Read JSON body
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        String jsonBody = jsonBuffer.toString();
        logger.info("Received update request for DataTable");
        logger.debug("Request body: {}", jsonBody);

        // Extract key from JSON
        JSONObject jsonObject;
        String key;
        
        try {
            jsonObject = new JSONObject(jsonBody);
            key = jsonObject.optString("key", null);
        } catch (Exception e) {
            logger.error("Failed to parse JSON body", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        if (key == null || key.isEmpty()) {
            logger.error("Row key missing in update request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Row key is required\"}");
            return;
        }

        String apiUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows/%s", 
                                       region, dataTableId, key);

        logger.info("Updating DataTable row: {} in table: {}", key, dataTableId);
        logger.debug("API URL: {}", apiUrl);
        logger.debug("Payload: {}", jsonBody);

        try {
            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = apiResponse.statusCode();
            String responseBody = apiResponse.body();
            
            logger.info("Genesys API Response: Status {}", statusCode);
            logger.debug("Response body: {}", responseBody);

            response.setStatus(statusCode);
            response.setContentType("application/json");
            
            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Successfully updated row: {}", key);
                response.getWriter().write(responseBody);
            } else {
                logger.error("Genesys API error: Status {} - {}", statusCode, responseBody);
                response.getWriter().write(responseBody);
            }

        } catch (Exception e) {
            logger.error("Error updating DataTable row: {}", key, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
