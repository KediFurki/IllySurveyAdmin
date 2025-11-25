package com.comapp.illy;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(MainServlet.class);
    private SurveyDAO dao = new SurveyDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String typeFilter = request.getParameter("type");
            String scoreFilter = request.getParameter("score");
            String audioFilter = request.getParameter("audio");

            // Validate and set default dates
            if (startDate == null || startDate.trim().isEmpty()) {
                startDate = LocalDate.now().minusDays(30).toString();
            } else {
                startDate = validateDateFormat(startDate);
            }
            
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = LocalDate.now().toString();
            } else {
                endDate = validateDateFormat(endDate);
            }

            logger.info("Admin request - Action: {}, Start: {}, End: {}, Type: {}, Score: {}, Audio: {}", 
                       action, startDate, endDate, typeFilter, scoreFilter, audioFilter);

            List<SurveyBean> data = dao.getSurveys(startDate, endDate);
            
            // Apply filters
            data = applyFilters(data, typeFilter, scoreFilter, audioFilter);

            // CSV EXPORT
            if ("export".equals(action)) {
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=Illy_Survey_" + System.currentTimeMillis() + ".csv");
                
                PrintWriter out = response.getWriter();
                out.println("Data,ID Interazione,Telefono,Tipo,Voto,Audio");
                
                for (SurveyBean s : data) {
                    String date = s.getDate() != null ? s.getDate() : "";
                    String convId = s.getConversationId() != null ? s.getConversationId() : "";
                    String phone = s.getCustomerPhone() != null ? s.getCustomerPhone() : "";
                    String type = s.getType() != null ? s.getType() : "";
                    String score = s.getScore() != null ? s.getScore() : "N/A";
                    String audio = s.getHasAudio() != null ? s.getHasAudio() : "";
                    
                    out.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", date, convId, phone, type, score, audio);
                }
                out.flush();
                logger.info("CSV export completed - {} records", data.size());
                return;
            }

            // HTML VIEW
            request.setAttribute("reportList", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);
            request.setAttribute("typeFilter", typeFilter != null ? typeFilter : "");
            request.setAttribute("scoreFilter", scoreFilter != null ? scoreFilter : "");
            request.setAttribute("audioFilter", audioFilter != null ? audioFilter : "");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error processing admin request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing your request");
        }
    }
    
    /**
     * Apply filters to survey data
     */
    private List<SurveyBean> applyFilters(List<SurveyBean> data, String typeFilter, String scoreFilter, String audioFilter) {
        List<SurveyBean> filtered = new ArrayList<>();
        
        for (SurveyBean bean : data) {
            // Type filter
            if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equals(bean.getType())) {
                continue;
            }
            
            // Score filter
            if (scoreFilter != null && !scoreFilter.isEmpty()) {
                if (scoreFilter.equals("none")) {
                    // Show only records without score
                    if (bean.getScore() != null && !bean.getScore().isEmpty()) {
                        continue;
                    }
                } else if (scoreFilter.equals("0-5")) {
                    // Show low scores (0-5)
                    try {
                        int score = Integer.parseInt(bean.getScore() != null ? bean.getScore() : "-1");
                        if (score < 0 || score > 5) {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else if (scoreFilter.equals("6-10")) {
                    // Show high scores (6-10) - only if score exists
                    try {
                        int score = Integer.parseInt(bean.getScore() != null ? bean.getScore() : "-1");
                        if (score < 6 || score > 10) {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            
            // Audio filter
            if (audioFilter != null && !audioFilter.isEmpty()) {
                boolean hasAudio = "true".equalsIgnoreCase(bean.getHasAudio());
                if (audioFilter.equals("yes") && !hasAudio) {
                    continue;
                }
                if (audioFilter.equals("no") && hasAudio) {
                    continue;
                }
            }
            
            filtered.add(bean);
        }
        
        return filtered;
    }
    
    // Validate date format (YYYY-MM-DD)
    private String validateDateFormat(String date) {
        try {
            LocalDate.parse(date);
            return date;
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format: {}", date);
            return LocalDate.now().toString();
        }
    }
}