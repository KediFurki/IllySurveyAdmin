package com.comapp.illy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SurveyDAO {
    private static final Logger logger = LogManager.getLogger(SurveyDAO.class);

    private static final String QUERY = 
        "SELECT c.conversationid, c.conversationstart, p.ani, " +
        "attr_t.value AS surveyType, attr_s.value AS surveyScore, attr_a.value AS hasAudio " +
        "FROM public.conversations c " +
        "INNER JOIN public.participants p ON c.conversationid = p.conversationid " +
        "LEFT JOIN public.attributes attr_t ON p.participantid = attr_t.participantid AND attr_t.key = 'SurveyType' " +
        "LEFT JOIN public.attributes attr_s ON p.participantid = attr_s.participantid AND attr_s.key = 'SurveyScore' " +
        "LEFT JOIN public.attributes attr_a ON p.participantid = attr_a.participantid AND attr_a.key = 'HasAudioFeedback' " +
        "WHERE p.partecipanttype = 'customer' " +
        "AND c.conversationstart >= ? AND c.conversationstart <= ? " +
        "ORDER BY c.conversationstart DESC";

    public List<SurveyBean> getSurveys(String startDate, String endDate) {
        logger.info("getSurveys called - Date range: {} to {}", startDate, endDate);
        
        List<SurveyBean> list = new ArrayList<>();
        
        // Format dates for database query (append time component)
        String startParam = startDate + "T00:00:00.000Z";
        String endParam = endDate + "T23:59:59.999Z";
        
        logger.debug("Database query parameters - Start: {}, End: {}", startParam, endParam);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            long startTime = System.currentTimeMillis();
            
            conn = DBConnection.getConnection();
            logger.debug("Database connection established");
            
            ps = conn.prepareStatement(QUERY);
            ps.setString(1, startParam);
            ps.setString(2, endParam);
            
            logger.debug("Executing survey query...");
            rs = ps.executeQuery();
            
            int recordCount = 0;
            while (rs.next()) {
                list.add(new SurveyBean(
                    rs.getString("conversationid"),
                    rs.getString("conversationstart"),
                    rs.getString("ani"),
                    rs.getString("surveyType"),
                    rs.getString("surveyScore"),
                    rs.getString("hasAudio")
                ));
                recordCount++;
            }
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            logger.info("Query executed successfully - Records: {}, Execution time: {}ms, Date range: {} to {}", 
                       recordCount, executionTime, startDate, endDate);
            
            if (recordCount == 0) {
                logger.warn("No survey records found for date range: {} to {}", startDate, endDate);
            } else if (executionTime > 5000) {
                logger.warn("Slow query detected - Execution time: {}ms for {} records", executionTime, recordCount);
            }
            
        } catch (SQLException e) {
            logger.error("Database error while fetching surveys - Date range: {} to {}, SQL State: {}, Error Code: {}", 
                        startDate, endDate, e.getSQLState(), e.getErrorCode(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching surveys - Date range: {} to {}", 
                        startDate, endDate, e);
        } finally {
            // Close resources in reverse order
            if (rs != null) {
                try {
                    rs.close();
                    logger.debug("ResultSet closed");
                } catch (SQLException e) {
                    logger.warn("Error closing ResultSet", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                    logger.debug("PreparedStatement closed");
                } catch (SQLException e) {
                    logger.warn("Error closing PreparedStatement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                    logger.debug("Database connection closed");
                } catch (SQLException e) {
                    logger.warn("Error closing database connection", e);
                }
            }
        }
        
        logger.debug("Returning {} survey records", list.size());
        return list;
    }
}