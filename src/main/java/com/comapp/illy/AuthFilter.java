package com.comapp.illy;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

// Filter all requests to "/admin", "/index.jsp", and "/" endpoints
@WebFilter(urlPatterns = { "/admin", "/index.jsp", "/" })
public class AuthFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized - Protecting endpoints: /admin, /index.jsp, /");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String requestURI = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        
        logger.debug("AuthFilter checking request: {} from IP: {}", requestURI, remoteAddr);

        // Set cache control headers to prevent caching of protected pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // Check if user is authenticated
        boolean isLoggedIn = (session != null && session.getAttribute("genesysUser") != null);
        
        if (isLoggedIn) {
            String userName = session.getAttribute("userName") != null ? 
                             session.getAttribute("userName").toString() : "Unknown";
            logger.debug("Authenticated user '{}' accessing: {}", userName, requestURI);
            chain.doFilter(req, res);
        } else {
            logger.warn("Unauthorized access attempt to: {} from IP: {} - Redirecting to login", 
                       requestURI, remoteAddr);
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
    
    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}