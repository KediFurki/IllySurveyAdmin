package com.comapp.illy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

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
    
    // Track recently processed PUT requests to prevent duplicates
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> recentPutRequests = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final long DUPLICATE_REQUEST_WINDOW_MS = 2000; // 2 seconds

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
            
            // Debug: log response structure for analysis
            if (apiResponse.statusCode() == 200) {
                try {
                    org.json.JSONObject responseObj = new org.json.JSONObject(apiResponse.body());
                    if (responseObj.has("entities") && responseObj.getJSONArray("entities").length() > 0) {
                        org.json.JSONObject firstEntity = responseObj.getJSONArray("entities").getJSONObject(0);
                        logger.info("===== FIRST ENTITY STRUCTURE FROM GENESYS =====");
                        logger.info("Entity keys: {}", String.join(", ", firstEntity.keySet()));
                        logger.info("Full entity JSON: {}", firstEntity.toString(2));
                        logger.info("Key field value: '{}'", firstEntity.optString("key", "[NO KEY FIELD]"));
                        logger.info("================================================");
                    }
                } catch (Exception e) {
                    logger.error("Could not parse response structure: {}", e.getMessage(), e);
                }
            }
            
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

        // Get the key from query parameter to create a unique request identifier
        String key = request.getParameter("key");
        String requestId = "PUT_" + key + "_" + System.currentTimeMillis();
        
        // Check if this is a duplicate request (same key updated within 2 seconds)
        if (key != null && !key.isEmpty()) {
            String recentKey = "PUT_" + key;
            Long lastRequestTime = recentPutRequests.get(recentKey);
            long currentTime = System.currentTimeMillis();
            
            if (lastRequestTime != null && (currentTime - lastRequestTime) < DUPLICATE_REQUEST_WINDOW_MS) {
                logger.warn("Duplicate PUT request detected for key: {} - skipping to prevent conflicts", key);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"status\": \"duplicate\", \"message\": \"Request already being processed\"}");
                return;
            }
            
            // Record this request time
            recentPutRequests.put(recentKey, currentTime);
        }
        
        // Clean up old entries (keep cache lean)
        long now = System.currentTimeMillis();
        recentPutRequests.entrySet().removeIf(entry -> (now - entry.getValue()) > DUPLICATE_REQUEST_WINDOW_MS);

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

        // Extract key from query parameter first
        // Note: key was already extracted above for duplicate request check
        // Just use the value already obtained
        
        if (key == null || key.trim().isEmpty()) {
            // Fallback: try to extract from request body
            try {
                JSONObject tempObject = new JSONObject(jsonBody);
                key = tempObject.optString("key", "").trim();
                logger.debug("Key extracted from request body: {}", key);
            } catch (Exception e) {
                logger.debug("Could not extract key from body");
                key = null;
            }
            
            if (key == null || key.isEmpty()) {
                logger.error("Row key missing in update request - not in query param or body");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Row key is required (provide as query parameter ?key=xyz or in body)\"}");
                return;
            }
        } else {
            key = key.trim();
            logger.debug("Key extracted from query parameter: {}", key);
        }

        // Parse JSON body to get new value
        String newValue = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonBody);
            // Get new value - try multiple field names
            newValue = jsonObject.optString("value", "");
            if (newValue.isEmpty()) {
                newValue = jsonObject.optString("Stringa", "");
            }
            if (newValue.isEmpty()) {
                newValue = jsonObject.optString("text", "");
            }
            if (newValue.isEmpty()) {
                logger.error("No value to update - missing in request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Value field required\"}");
                return;
            }
        } catch (Exception e) {
            logger.error("Failed to parse JSON body", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        logger.info("Updating DataTable row: {} in table: {}", key, dataTableId);
        logger.info("Using DELETE + POST pattern (Genesys doesn't support PUT/PATCH)");
        logger.debug("Key: {}, New value: {}", key, newValue);

        try {
            // Step 1: DELETE the old row
            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
            String deleteUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows/%s", 
                                             region, dataTableId, encodedKey);
            
            logger.info("Step 1: Deleting old row");
            HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

            HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
            logger.info("Delete response: Status {}", deleteResponse.statusCode());
            
            if (deleteResponse.statusCode() == 404) {
                logger.warn("Row not found for deletion (404) - will create as new row: {}", key);
                // 404 is OK, row might not exist yet
            } else if (deleteResponse.statusCode() < 200 || deleteResponse.statusCode() >= 300) {
                logger.error("Failed to delete old row: Status {} - {}", deleteResponse.statusCode(), deleteResponse.body());
                // Continue anyway - maybe row doesn't exist
            } else {
                logger.info("Row deleted successfully");
                // Small delay to allow Genesys to process deletion
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Step 2: POST the new row with updated value
            // IMPORTANT: For Genesys Data Table API:
            // - "key" should be in BOTH body and query parameter (for compatibility)
            // - "Stringa" goes in body as the text data field
            JSONObject newRowPayload = new JSONObject();
            newRowPayload.put("key", key);  // Include key in body for safety
            newRowPayload.put("Stringa", newValue);
            
            String postUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows?key=%s", 
                                          region, dataTableId, URLEncoder.encode(key, StandardCharsets.UTF_8));
            
            logger.info("Step 2: Creating new row with updated value");
            logger.debug("New row payload: {}", newRowPayload.toString());
            logger.debug("Post URL with key parameter: {}", postUrl);
            
            HttpResponse<String> postResponse = null;
            int statusCode = 0;
            String responseBody = "";
            int retryCount = 0;
            int maxRetries = 2;
            
            // Retry logic for 409 conflicts (key not unique)
            while (retryCount <= maxRetries) {
                HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(postUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(newRowPayload.toString()))
                    .build();

                postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
                statusCode = postResponse.statusCode();
                responseBody = postResponse.body();
                
                logger.info("POST attempt {} of {}: Status {}", retryCount + 1, maxRetries + 1, statusCode);
                
                if (statusCode == 409 && retryCount < maxRetries) {
                    logger.warn("Conflict detected (409) - key may not be unique yet. Retrying after delay... (attempt {}/{})", 
                               retryCount + 1, maxRetries);
                    try {
                        Thread.sleep(300); // Wait 300ms before retry
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    retryCount++;
                } else {
                    break; // Success or max retries exceeded
                }
            }
            
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("genesysUser") == null) {
            logger.warn("Unauthorized DataTable create attempt - no valid session");
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
        logger.info("Received create request for DataTable");
        logger.debug("Request body: {}", jsonBody);

        // Parse and validate input JSON
        JSONObject inputObject;
        JSONObject genesysPayload = new JSONObject();
        
        try {
            inputObject = new JSONObject(jsonBody);
            
            // Extract key (required)
            String key = inputObject.optString("key", "").trim();
            if (key == null || key.isEmpty()) {
                logger.error("Row key missing in create request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Row key is required and cannot be empty\"}");
                return;
            }
            
            // Add key to Genesys payload
            genesysPayload.put("key", key);
            
            // Extract value/text - map to Genesys "Stringa" field (the question text)
            String questionText = inputObject.optString("value", "").trim();
            if (questionText.isEmpty()) {
                // Try alternate field names
                questionText = inputObject.optString("text", "").trim();
            }
            if (questionText.isEmpty()) {
                questionText = inputObject.optString("Stringa", "").trim();
            }
            if (questionText.isEmpty()) {
                logger.error("Question text missing in create request");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Question text (value/text/Stringa) is required and cannot be empty\"}");
                return;
            }
            
            // Add question text to Genesys payload with correct field name
            genesysPayload.put("Stringa", questionText);
            
            logger.info("Mapped input fields to Genesys schema: key='{}', Stringa='{}'", key, questionText.substring(0, Math.min(50, questionText.length())));
            
        } catch (Exception e) {
            logger.error("Failed to parse JSON body", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format: " + e.getMessage() + "\"}");
            return;
        }

        String apiUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows", 
                                       region, dataTableId);

        String payloadToSend = genesysPayload.toString();
        
        logger.info("Creating new DataTable row in table: {}", dataTableId);
        logger.debug("API URL: {}", apiUrl);
        logger.debug("Genesys payload: {}", payloadToSend);

        try {
            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadToSend))
                .build();

            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = apiResponse.statusCode();
            String responseBody = apiResponse.body();
            
            logger.info("Genesys API Response: Status {}", statusCode);
            logger.debug("Response body: {}", responseBody);

            response.setStatus(statusCode);
            response.setContentType("application/json");
            
            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Successfully created new row with key: {}", genesysPayload.optString("key"));
                response.getWriter().write(responseBody);
            } else {
                logger.error("Genesys API error: Status {} - {}", statusCode, responseBody);
                response.getWriter().write(responseBody);
            }

        } catch (Exception e) {
            logger.error("Error creating DataTable row", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("genesysUser") == null) {
            logger.warn("Unauthorized DataTable delete attempt - no valid session");
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

        // Extract key from query parameter
        String key = request.getParameter("key");

        if (key == null || key.trim().isEmpty()) {
            logger.error("Row key missing in delete request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Row key is required and cannot be empty\"}");
            return;
        }
        
        // Trim the key
        key = key.trim();

        // URL encode the key to handle special characters (spaces, etc.)
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String apiUrl = String.format("https://api.%s/api/v2/flows/datatables/%s/rows/%s", 
                                       region, dataTableId, encodedKey);

        logger.info("Deleting DataTable row: {} from table: {}", key, dataTableId);
        logger.debug("API URL: {}", apiUrl);

        try {
            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

            HttpResponse<String> apiResponse = httpClient.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = apiResponse.statusCode();
            String responseBody = apiResponse.body();
            
            logger.info("Genesys API Response: Status {}", statusCode);
            logger.debug("Response body: {}", responseBody);

            response.setStatus(statusCode);
            response.setContentType("application/json");
            
            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Successfully deleted row: {}", key);
                response.getWriter().write("{\"success\": true, \"message\": \"Row deleted successfully\"}");
            } else {
                logger.error("Genesys API error: Status {} - {}", statusCode, responseBody);
                response.getWriter().write(responseBody);
            }

        } catch (Exception e) {
            logger.error("Error deleting DataTable row: {}", key, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}