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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GenesysConfig.CLIENT_ID yerine GenesysConfig.getClientId() kullanıyoruz
        String loginUrl = "https://login." + GenesysConfig.getRegion() + "/oauth/authorize" +
                "?client_id=" + GenesysConfig.getClientId() +
                "&response_type=code" +
                "&redirect_uri=" + GenesysConfig.getRedirectUri();
        
        response.sendRedirect(loginUrl);
    }
}