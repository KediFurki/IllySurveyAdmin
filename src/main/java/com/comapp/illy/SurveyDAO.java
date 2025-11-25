package com.comapp.illy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        List<SurveyBean> list = new ArrayList<>();
        
        // Format dates for DB (Append time)
        String startParam = startDate + "T00:00:00.000Z";
        String endParam = endDate + "T23:59:59.999Z";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY)) {
            
            ps.setString(1, startParam);
            ps.setString(2, endParam);
            
            logger.info("Executing query from " + startDate + " to " + endDate);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SurveyBean(
                    rs.getString("conversationid"),
                    rs.getString("conversationstart"),
                    rs.getString("ani"),
                    rs.getString("surveyType"),
                    rs.getString("surveyScore"),
                    rs.getString("hasAudio")
                ));
            }
        } catch (Exception e) {
            logger.error("Error fetching data", e);
        }
        return list;
    }
}