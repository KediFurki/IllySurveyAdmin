package com.comapp.illy;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    // ...existing code...

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

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

            logger.info("Admin request - Action: {}, Start: {}, End: {}", action, startDate, endDate);

            List<SurveyBean> data = dao.getSurveys(startDate, endDate);

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
                    String score = s.getScore() != null ? s.getScore() : "";
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
            request.getRequestDispatcher("index.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error processing admin request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing your request");
        }
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