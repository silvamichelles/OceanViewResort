package com.oceanview.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Security filter – protects the dashboard page and all /api/* endpoints.
 * Unauthenticated requests are redirected to the login page (or 401 for XHR).
 */
@WebFilter(urlPatterns = {"/dashboard.html", "/api/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (loggedIn) {
            chain.doFilter(req, res);
            return;
        }

        // Distinguish XHR from normal browser navigation
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Session expired. Please log in.\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/index.html?error=session");
        }
    }
}
