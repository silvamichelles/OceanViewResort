package com.oceanview.servlet;

import com.oceanview.service.UserService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User management REST endpoint.
 *
 * GET    /api/users        – list all users
 * POST   /api/users        – register a new staff member
 * DELETE /api/users/{id}   – remove a user account
 *
 * Session guarded by AuthFilter.
 */
@WebServlet("/api/users/*")
public class UserServlet extends BaseApiServlet {

    private final UserService userService = new UserService();

    // ── GET ──────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<Map<String, Object>> users = userService.getAllUsers();
        writeJson(resp, users);
    }

    // ── POST ─────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String role     = req.getParameter("role");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Username and password are required.");
            return;
        }

        boolean ok = userService.register(username.trim(), password, role);
        if (ok) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeSuccess(resp, "User '" + username.trim() + "' created successfully.");
        } else {
            writeError(resp, HttpServletResponse.SC_CONFLICT,
                    "Could not create user – username may already exist.");
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo(); // "/{id}"
        if (path == null || path.length() < 2) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Missing user ID in path.");
            return;
        }
        try {
            int userId = Integer.parseInt(path.substring(1));
            boolean ok = userService.deleteUser(userId);
            if (ok) writeSuccess(resp, "User deleted.");
            else    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
        }
    }
}
