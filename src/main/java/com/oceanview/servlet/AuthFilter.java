package com.oceanview.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Security filter – protects the dashboard page and all /api/* endpoints.
 * Unauthenticated requests are redirected to the login page (or 401 for XHR).
 */
@WebFilter(urlPatterns = {"/dashboard.html", "/api/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op — no resources to acquire
    }

    @Override
    public void destroy() {
        // no-op — no resources to release
    }

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
