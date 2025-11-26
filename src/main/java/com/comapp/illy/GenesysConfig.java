package com.comapp.illy;

import java.util.Properties;

public class GenesysConfig {
    
    // Sabitleri siliyoruz, yerine Getter metotları koyuyoruz
    
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

    // Yardımcı Metot: ConfigServlet'ten veriyi çeker
    private static String getProperty(String key, String defaultValue) {
        Properties props = ConfigServlet.getProperties();
        if (props != null) {
            return props.getProperty(key, defaultValue);
        }
        return defaultValue;
    }
}