package com.comapp.illy;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// "/admin" ve "/index.jsp" sayfalarına giden herkesi kontrol et
// @WebFilter(urlPatterns = { "/admin", "/index.jsp", "/" })
public class AuthFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        // Kullanıcı zaten giriş yapmış mı?
        boolean isLoggedIn = (session != null && session.getAttribute("genesysUser") != null);
        
        // Giriş yapmışsa DEVAM ET, yapmamışsa LOGIN'e gönder
        if (isLoggedIn) {
            chain.doFilter(req, res);
        } else {
            // Login Servlet'e yönlendir
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
    
    public void init(FilterConfig f) throws ServletException {}
    public void destroy() {}
}