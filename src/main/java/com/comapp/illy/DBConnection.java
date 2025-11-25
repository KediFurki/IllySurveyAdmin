package com.comapp.illy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {
    private static final Logger logger = LogManager.getLogger(DBConnection.class);
    
    // Local DB Connection Info
    private static final String URL = "jdbc:postgresql://localhost:5432/analyzer";
    private static final String USER = "postgres";
    private static final String PASS = "Kedi345";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            logger.debug("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL Driver not found.", e);
        } catch (SQLException e) {
            logger.error("Connection failed.", e);
        }
        return conn;
    }
}