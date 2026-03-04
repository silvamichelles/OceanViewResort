package com.oceanview.servlet;

import com.oceanview.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authenticates staff members.
 * POST /login  – validates credentials and creates a session.
 * GET  /login  – redirects to the login page.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/index.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null ||
            username.trim().isEmpty() || password.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/index.html?error=empty");
            return;
        }

        if (userService.login(username.trim(), password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", username.trim());
            session.setMaxInactiveInterval(30 * 60); // 30 min timeout
            resp.sendRedirect(req.getContextPath() + "/dashboard.html");
        } else {
            resp.sendRedirect(req.getContextPath() + "/index.html?error=invalid");
        }
    }
}
