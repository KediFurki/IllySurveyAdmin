package com.comapp.illy;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loginUrl = "https://login." + GenesysConfig.REGION_DOMAIN + "/oauth/authorize" +
                "?client_id=" + GenesysConfig.CLIENT_ID +
                "&response_type=code" +
                "&redirect_uri=" + GenesysConfig.REDIRECT_URI;
        
        response.sendRedirect(loginUrl);
    }
}