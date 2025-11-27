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
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("ILLY SURVEY ADMIN - Authentication Filter Initialization");
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Protected Endpoints:");
        logger.info("  - /admin (Main survey dashboard)");
        logger.info("  - /index.jsp (Survey results page)");
        logger.info("  - / (Root application)");
        logger.info("Authentication Method: Genesys Cloud OAuth 2.0");
        logger.info("Session Timeout: 30 minutes");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String requestURI = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        
        logger.debug("━━━ Authentication Check ━━━");
        logger.debug("Request: {} {} from IP: {}", method, requestURI, remoteAddr);
        logger.debug("User-Agent: {}", userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) + "..." : "Unknown");

        // Set cache control headers to prevent caching of protected pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        logger.debug("Anti-cache headers applied to prevent browser caching of sensitive data");
        
        // Check if user is authenticated
        boolean hasSession = (session != null);
        boolean hasGenesysToken = hasSession && session.getAttribute("genesysUser") != null;
        boolean isLoggedIn = hasSession && hasGenesysToken;
        
        if (isLoggedIn) {
            String userName = session.getAttribute("userName") != null ? 
                             session.getAttribute("userName").toString() : "Unknown";
            String userEmail = session.getAttribute("userEmail") != null ? 
                              session.getAttribute("userEmail").toString() : "";
            String sessionId = session.getId();
            
            logger.info("✓ ACCESS GRANTED - User: '{}' ({}), Session: {}, Accessing: {}, IP: {}", 
                       userName, userEmail, sessionId, requestURI, remoteAddr);
            
            // Log session age for monitoring
            long now = System.currentTimeMillis();
            long creationTime = session.getCreationTime();
            long sessionAge = (now - creationTime) / 1000; // seconds
            logger.debug("Session age: {} seconds ({} minutes)", sessionAge, sessionAge / 60);
            
            chain.doFilter(req, res);
        } else {
            // Log detailed reason for access denial
            if (!hasSession) {
                logger.warn("✗ ACCESS DENIED - No active session - Endpoint: {}, IP: {}", requestURI, remoteAddr);
            } else if (!hasGenesysToken) {
                logger.warn("✗ ACCESS DENIED - Session exists but no Genesys token found - Session: {}, Endpoint: {}, IP: {}", 
                           session.getId(), requestURI, remoteAddr);
            }
            
            logger.info("Redirecting unauthenticated user to login page - From: {}, IP: {}", requestURI, remoteAddr);
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
    
    @Override
    public void destroy() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("ILLY SURVEY ADMIN - Authentication Filter Shutdown");
        logger.info("═══════════════════════════════════════════════════════════");
    }
}