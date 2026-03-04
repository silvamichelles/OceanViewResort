package com.oceanview.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanview.service.ReservationService;
import com.oceanview.service.RoomService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Serves aggregated dashboard statistics as JSON.
 * GET /api/dashboard -> returns stats (total rooms, reservations, revenue, etc.)
 * Requires an authenticated session.
 */
@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ReservationService reservationService = new ReservationService();
    private final RoomService roomService = new RoomService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Map<String, String> stats = reservationService.getDashboardStats();

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(stats));
    }
}
