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
        String userEmail = request.getSession(false) != null && request.getSession(false).getAttribute("userEmail") != null ? 
                          request.getSession(false).getAttribute("userEmail").toString() : "";
        
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║  ADMIN DASHBOARD REQUEST                                  ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("Request Details:");
        logger.info("  • User: {} ({})", userName, userEmail);
        logger.info("  • Session ID: {}", sessionId);
        logger.info("  • IP Address: {}", remoteAddr);
        logger.info("  • Request Method: {}", request.getMethod());
        logger.info("  • Request URI: {}", request.getRequestURI());
        
        try {
            long requestStartTime = System.currentTimeMillis();
            
            String action = request.getParameter("action");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String typeFilter = request.getParameter("type");
            String scoreFilter = request.getParameter("score");
            String audioFilter = request.getParameter("audio");

            logger.info("━━━ Request Parameters ━━━");
            logger.info("  • Action: {}", action != null ? action : "view (default)");
            logger.info("  • Start Date: {}", startDate != null ? startDate : "not provided (will use default)");
            logger.info("  • End Date: {}", endDate != null ? endDate : "not provided (will use default)");
            
            if (typeFilter != null || scoreFilter != null || audioFilter != null) {
                logger.info("  • Filters Active:");
                if (typeFilter != null) logger.info("    - Type: {}", typeFilter);
                if (scoreFilter != null) logger.info("    - Score: {}", scoreFilter);
                if (audioFilter != null) logger.info("    - Audio: {}", audioFilter);
            } else {
                logger.info("  • Filters: None applied");
            }

            // Validate and set default dates
            String originalStartDate = startDate;
            String originalEndDate = endDate;
            
            if (startDate == null || startDate.trim().isEmpty()) {
                startDate = LocalDate.now().minusDays(30).toString();
                logger.info("  • Start date auto-set to: {} (last 30 days)", startDate);
            } else {
                String validated = validateDateFormat(startDate);
                if (!validated.equals(startDate)) {
                    logger.warn("  • Start date corrected: {} → {}", startDate, validated);
                }
                startDate = validated;
            }
            
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = LocalDate.now().toString();
                logger.info("  • End date auto-set to: {} (today)", endDate);
            } else {
                String validated = validateDateFormat(endDate);
                if (!validated.equals(endDate)) {
                    logger.warn("  • End date corrected: {} → {}", endDate, validated);
                }
                endDate = validated;
            }

            logger.info("━━━ Querying Database for Survey Data ━━━");
            logger.info("  • Date Range: {} to {} ({} days)", 
                       startDate, endDate, 
                       java.time.temporal.ChronoUnit.DAYS.between(
                           LocalDate.parse(startDate), LocalDate.parse(endDate)));
            logger.info("  • User: {}", userName);

            long dbStartTime = System.currentTimeMillis();
            List<SurveyBean> data = dao.getSurveys(startDate, endDate);
            long dbEndTime = System.currentTimeMillis();
            long dbQueryTime = dbEndTime - dbStartTime;
            
            int totalRecords = data.size();
            logger.info("✓ Database query completed successfully");
            logger.info("  • Records Retrieved: {}", totalRecords);
            logger.info("  • Query Time: {}ms", dbQueryTime);
            
            if (dbQueryTime > 3000) {
                logger.warn("  ⚠ SLOW QUERY DETECTED - Consider optimizing or adding indexes");
            }
            
            if (totalRecords == 0) {
                logger.warn("  ⚠ NO DATA FOUND for date range {} to {}", startDate, endDate);
            }
            
            // Apply filters
            logger.info("━━━ Applying User Filters ━━━");
            int preFilterCount = data.size();
            data = applyFilters(data, typeFilter, scoreFilter, audioFilter);
            int postFilterCount = data.size();
            
            if (preFilterCount != postFilterCount) {
                int filteredOut = preFilterCount - postFilterCount;
                double filterPercentage = (filteredOut * 100.0) / preFilterCount;
                
                logger.info("  • Records Before Filters: {}", preFilterCount);
                logger.info("  • Records After Filters: {}", postFilterCount);
                logger.info("  • Filtered Out: {} ({:.1f}%)", filteredOut, filterPercentage);
                
                if (postFilterCount == 0) {
                    logger.warn("  ⚠ ALL RECORDS FILTERED OUT - User will see empty result");
                }
            } else {
                logger.info("  • No records filtered (filters not active or match all records)");
            }

            // CSV EXPORT
            if ("export".equals(action)) {
                long exportStartTime = System.currentTimeMillis();
                String filename = "Illy_Survey_" + System.currentTimeMillis() + ".csv";
                
                logger.info("╔═══════════════════════════════════════════════════════════╗");
                logger.info("║  CSV EXPORT INITIATED                                     ║");
                logger.info("╚═══════════════════════════════════════════════════════════╝");
                logger.info("Export Details:");
                logger.info("  • User: {} ({})", userName, userEmail);
                logger.info("  • Filename: {}", filename);
                logger.info("  • Total Records: {}", data.size());
                logger.info("  • Date Range: {} to {}", startDate, endDate);
                logger.info("  • Filters Applied: {}", (typeFilter != null || scoreFilter != null || audioFilter != null));
                
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                
                PrintWriter out = response.getWriter();
                out.println("Data,ID Interazione,Telefono,Tipo,Voto,Audio");
                
                int exportedRecords = 0;
                int recordsWithAudio = 0;
                int recordsWithScore = 0;
                
                for (SurveyBean s : data) {
                    String date = s.getDate() != null ? s.getDate() : "";
                    String convId = s.getConversationId() != null ? s.getConversationId() : "";
                    String phone = s.getCustomerPhone() != null ? s.getCustomerPhone() : "";
                    String type = s.getType() != null ? s.getType() : "";
                    String score = s.getScore() != null ? s.getScore() : "N/A";
                    String audio = s.getHasAudio() != null ? s.getHasAudio() : "";
                    
                    out.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", date, convId, phone, type, score, audio);
                    exportedRecords++;
                    
                    if ("true".equalsIgnoreCase(audio)) recordsWithAudio++;
                    if (score != null && !score.equals("N/A")) recordsWithScore++;
                }
                out.flush();
                
                long exportTime = System.currentTimeMillis() - exportStartTime;
                long totalRequestTime = System.currentTimeMillis() - requestStartTime;
                
                logger.info("✓ CSV Export Completed Successfully");
                logger.info("Export Statistics:");
                logger.info("  • Total Records Exported: {}", exportedRecords);
                logger.info("  • Records with Audio: {}", recordsWithAudio);
                logger.info("  • Records with Score: {}", recordsWithScore);
                logger.info("  • Export Time: {}ms", exportTime);
                logger.info("  • Total Request Time: {}ms", totalRequestTime);
                logger.info("  • File Size (approx): {} KB", (exportedRecords * 100) / 1024);
                logger.info("═══════════════════════════════════════════════════════════");
                return;
            }

            // HTML VIEW
            logger.info("━━━ Rendering HTML Dashboard View ━━━");
            logger.info("  • View Type: Survey Dashboard");
            logger.info("  • Records to Display: {}", data.size());
            
            request.setAttribute("reportList", data);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);
            request.setAttribute("typeFilter", typeFilter != null ? typeFilter : "");
            request.setAttribute("scoreFilter", scoreFilter != null ? scoreFilter : "");
            request.setAttribute("audioFilter", audioFilter != null ? audioFilter : "");
            
            long totalRequestTime = System.currentTimeMillis() - requestStartTime;
            
            logger.info("✓ Request Processing Completed");
            logger.info("Performance Metrics:");
            logger.info("  • Database Query: {}ms", dbQueryTime);
            logger.info("  • Total Processing: {}ms", totalRequestTime);
            logger.info("  • User: {}", userName);
            logger.info("  • Records Displayed: {}", data.size());
            
            if (totalRequestTime > 5000) {
                logger.warn("  ⚠ SLOW REQUEST - Total time: {}ms exceeds 5 seconds", totalRequestTime);
            }
            
            logger.info("→ Forwarding to index.jsp for rendering");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            logger.debug("Page rendered successfully");
            
        } catch (Exception e) {
            logger.error("╔═══════════════════════════════════════════════════════════╗");
            logger.error("║  ERROR PROCESSING ADMIN REQUEST                           ║");
            logger.error("╚═══════════════════════════════════════════════════════════╝");
            logger.error("Error Details:");
            logger.error("  • User: {}", userName);
            logger.error("  • Session ID: {}", sessionId);
            logger.error("  • IP Address: {}", remoteAddr);
            logger.error("  • Error Type: {}", e.getClass().getName());
            logger.error("  • Error Message: {}", e.getMessage());
            logger.error("  • Cause: {}", e.getCause() != null ? e.getCause().getMessage() : "None");
            logger.error("Full Stack Trace:", e);
            
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