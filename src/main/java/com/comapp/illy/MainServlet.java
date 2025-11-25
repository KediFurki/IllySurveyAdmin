package com.comapp.illy;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SurveyDAO dao = new SurveyDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        // Default Dates
        if (startDate == null || startDate.isEmpty()) startDate = LocalDate.now().minusDays(30).toString();
        if (endDate == null || endDate.isEmpty()) endDate = LocalDate.now().toString();

        List<SurveyBean> data = dao.getSurveys(startDate, endDate);

        // --- CSV EXPORT LOGIC ---
        if ("export".equals(action)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=Illy_Survey.csv");
            PrintWriter out = response.getWriter();
            out.println("Data,ID Interazione,Telefono,Tipo,Voto,Audio"); // Italian CSV Header
            for (SurveyBean s : data) {
                out.printf("%s,%s,%s,%s,%s,%s%n", 
                    s.getDate(), s.getConversationId(), s.getCustomerPhone(), 
                    (s.getType()!=null?s.getType():""), (s.getScore()!=null?s.getScore():""), (s.getHasAudio()!=null?s.getHasAudio():""));
            }
            return; // Stop execution
        }

        // --- HTML VIEW ---
        request.setAttribute("reportList", data);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}