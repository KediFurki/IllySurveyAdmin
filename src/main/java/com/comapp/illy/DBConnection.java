package com.comapp.illy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Logger logger = LogManager.getLogger(DBConnection.class);
    // Static variable to hold database connection pool (DataSource) in memory
    private static volatile DataSource ds;

    // Method to lookup DataSource (only runs first time, then returns from memory)
    private static DataSource lookupDataSource() throws SQLException {
        if (ds != null) return ds;
        synchronized (DBConnection.class) {
            if (ds != null) return ds;
            try {
                // 1. Get JNDI name from settings loaded by ConfigServlet
                Properties props = ConfigServlet.getProperties();
                
                // If 'jndi.name' is not in file, use default 'jdbc/IllyDB'
                // This name must match the "name" in context.xml
                String jndiName = props.getProperty("jndi.name", "jdbc/IllyDB");

                // 2. Ask Tomcat JNDI Service: "Do you have a connection pool with this name?"
                InitialContext ic = new InitialContext();
                Context env = (Context) ic.lookup("java:comp/env");
                ds = (DataSource) env.lookup(jndiName);

                logger.info("DB: JNDI Connection Pool ('{}') successfully found.", jndiName);
                return ds;

            } catch (NamingException e) {
                logger.error("DB: JNDI name not found on server! Check context.xml file.", e);
                throw new SQLException("JNDI Lookup Error", e);
            } catch (Exception e) {
                logger.error("DB: General DataSource error", e);
                throw new SQLException("Cannot retrieve DataSource", e);
            }
        }
    }

    // Method that provides connection to external callers
    public static Connection getConnection() throws SQLException {
        try {
            DataSource dataSource = lookupDataSource();
            Connection conn = dataSource.getConnection();
            logger.debug("DB: New connection acquired from pool.");
            return conn;
        } catch (SQLException e) {
            logger.error("DB: Error while acquiring connection!", e);
            throw e;
        }
    }
}