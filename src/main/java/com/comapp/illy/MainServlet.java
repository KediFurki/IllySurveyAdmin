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
        String remoteAddr = request.getRemoteAddr();
        String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "no-session";
        String userName = request.getSession(false) != null && request.getSession(false).getAttribute("userName") != null ? 
                         request.getSession(false).getAttribute("userName").toString() : "Unknown";
        
        logger.info("Admin page request - User: {}, SessionID: {}, IP: {}", userName, sessionId, remoteAddr);
        
        try {
            String action = request.getParameter("action");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String typeFilter = request.getParameter("type");
            String scoreFilter = request.getParameter("score");
            String audioFilter = request.getParameter("audio");

            logger.debug("Request parameters - Action: {}, Dates: {} to {}, Filters - Type: {}, Score: {}, Audio: {}", 
                        action, startDate, endDate, typeFilter, scoreFilter, audioFilter);

            // Validate and set default dates
            String originalStartDate = startDate;
            String originalEndDate = endDate;
            
            if (startDate == null || startDate.trim().isEmpty()) {
                startDate = LocalDate.now().minusDays(30).toString();
                logger.debug("Start date not provided, using default: {}", startDate);
            } else {
                startDate = validateDateFormat(startDate);
                logger.debug("Start date validated: {} -> {}", originalStartDate, startDate);
            }
            
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = LocalDate.now().toString();
                logger.debug("End date not provided, using default: {}", endDate);
            } else {
                endDate = validateDateFormat(endDate);
                logger.debug("End date validated: {} -> {}", originalEndDate, endDate);
            }

            logger.info("Fetching surveys - User: {}, Action: {}, Date range: {} to {}, Filters applied: {}", 
                       userName, action != null ? action : "view", startDate, endDate, 
                       (typeFilter != null || scoreFilter != null || audioFilter != null));

            long dbStartTime = System.currentTimeMillis();
            List<SurveyBean> data = dao.getSurveys(startDate, endDate);
            long dbEndTime = System.currentTimeMillis();
            
            int totalRecords = data.size();
            logger.info("Database query completed - Records retrieved: {}, Time: {}ms", 
                       totalRecords, (dbEndTime - dbStartTime));
            
            // Apply filters
            int preFilterCount = data.size();
            data = applyFilters(data, typeFilter, scoreFilter, audioFilter);
            int postFilterCount = data.size();
            
            if (preFilterCount != postFilterCount) {
                logger.info("Filters applied - Before: {}, After: {}, Filtered out: {}", 
                           preFilterCount, postFilterCount, (preFilterCount - postFilterCount));
            }

            // CSV EXPORT
            if ("export".equals(action)) {
                logger.info("CSV export initiated - User: {}, Records: {}", userName, data.size());
                
                response.setContentType("text/csv; charset=UTF-8");
                String filename = "Illy_Survey_" + System.currentTimeMillis() + ".csv";
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                
                logger.debug("CSV filename: {}", filename);
                
                PrintWriter out = response.getWriter();
                out.println("Data,ID Interazione,Telefono,Tipo,Voto,Audio");
                
                int exportedRecords = 0;
                for (SurveyBean s : data) {
                    String date = s.getDate() != null ? s.getDate() : "";
                    String convId = s.getConversationId() != null ? s.getConversationId() : "";
                    String phone = s.getCustomerPhone() != null ? s.getCustomerPhone() : "";
                    String type = s.getType() != null ? s.getType() : "";
                    String score = s.getScore() != null ? s.getScore() : "N/A";
                    String audio = s.getHasAudio() != null ? s.getHasAudio() : "";
                    
                    out.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", date, convId, phone, type, score, audio);
                    exportedRecords++;
                }
                out.flush();
                
                logger.info("CSV export completed successfully - User: {}, File: {}, Records: {}", 
                           userName, filename, exportedRecords);
                return;
            }

            // HTML VIEW
            logger.debug("Preparing HTML view with {} records", data.size());
            
            request.setAttribute("reportList", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);
            request.setAttribute("typeFilter", typeFilter != null ? typeFilter : "");
            request.setAttribute("scoreFilter", scoreFilter != null ? scoreFilter : "");
            request.setAttribute("audioFilter", audioFilter != null ? audioFilter : "");
            
            logger.info("Admin page rendering - User: {}, Records displayed: {}", userName, data.size());
            
            request.getRequestDispatcher("index.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error processing admin request - User: {}, SessionID: {}, IP: {}", 
                        userName, sessionId, remoteAddr, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "An error occurred while processing your request");
        }
    }
    
    /**
     * Apply filters to survey data
     */
    private List<SurveyBean> applyFilters(List<SurveyBean> data, String typeFilter, String scoreFilter, String audioFilter) {
        logger.debug("Applying filters - Input records: {}, Type: {}, Score: {}, Audio: {}", 
                    data.size(), typeFilter, scoreFilter, audioFilter);
        
        List<SurveyBean> filtered = new ArrayList<>();
        int typeFiltered = 0;
        int scoreFiltered = 0;
        int audioFiltered = 0;
        
        for (SurveyBean bean : data) {
            boolean passedFilters = true;
            
            // Type filter
            if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equals(bean.getType())) {
                typeFiltered++;
                passedFilters = false;
                continue;
            }
            
            // Score filter
            if (scoreFilter != null && !scoreFilter.isEmpty()) {
                if (scoreFilter.equals("none")) {
                    // Show only records without score
                    if (bean.getScore() != null && !bean.getScore().isEmpty()) {
                        scoreFiltered++;
                        passedFilters = false;
                        continue;
                    }
                } else if (scoreFilter.equals("0-5")) {
                    // Show low scores (0-5)
                    try {
                        int score = Integer.parseInt(bean.getScore() != null ? bean.getScore() : "-1");
                        if (score < 0 || score > 5) {
                            scoreFiltered++;
                            passedFilters = false;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        scoreFiltered++;
                        passedFilters = false;
                        continue;
                    }
                } else if (scoreFilter.equals("6-10")) {
                    // Show high scores (6-10) - only if score exists
                    try {
                        int score = Integer.parseInt(bean.getScore() != null ? bean.getScore() : "-1");
                        if (score < 6 || score > 10) {
                            scoreFiltered++;
                            passedFilters = false;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        scoreFiltered++;
                        passedFilters = false;
                        continue;
                    }
                }
            }
            
            // Audio filter
            if (audioFilter != null && !audioFilter.isEmpty()) {
                boolean hasAudio = "true".equalsIgnoreCase(bean.getHasAudio());
                if (audioFilter.equals("yes") && !hasAudio) {
                    audioFiltered++;
                    passedFilters = false;
                    continue;
                }
                if (audioFilter.equals("no") && hasAudio) {
                    audioFiltered++;
                    passedFilters = false;
                    continue;
                }
            }
            
            filtered.add(bean);
        }
        
        logger.debug("Filter results - Output: {}, Filtered by Type: {}, Score: {}, Audio: {}", 
                    filtered.size(), typeFiltered, scoreFiltered, audioFiltered);
        
        return filtered;
    }
    
    // Validate date format (YYYY-MM-DD)
    private String validateDateFormat(String date) {
        try {
            LocalDate.parse(date);
            logger.debug("Date format validation passed: {}", date);
            return date;
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format received: {} - Using current date", date);
            String currentDate = LocalDate.now().toString();
            logger.debug("Replaced invalid date with: {}", currentDate);
            return currentDate;
        }
    }
}