package com.comapp.illy;

import java.util.Properties;

public class GenesysConfig {
    
    // Using getter methods instead of constants to allow dynamic configuration
    
    public static String getRegion() {
        return getProperty("genesys.region", "mypurecloud.de");
    }

    public static String getClientId() {
        return getProperty("genesys.client.id", null);
    }

    public static String getClientSecret() {
        return getProperty("genesys.client.secret", null);
    }

    public static String getRedirectUri() {
        return getProperty("genesys.redirect.uri", "http://localhost:8080/IllySurveyAdmin/oauth/callback");
    }

    // Helper method: retrieves data from ConfigServlet
    private static String getProperty(String key, String defaultValue) {
        Properties props = ConfigServlet.getProperties();
        if (props != null) {
            return props.getProperty(key, defaultValue);
        }
        return defaultValue;
    }
}