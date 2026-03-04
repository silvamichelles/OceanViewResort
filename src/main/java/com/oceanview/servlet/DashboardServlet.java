package com.oceanview.servlet;

import com.oceanview.service.ReservationService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * GET /api/dashboard
 * Returns aggregated KPI statistics (reservations, room occupancy, revenue).
 * Session is enforced by AuthFilter.
 */
@WebServlet("/api/dashboard")
public class DashboardServlet extends BaseApiServlet {

    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Map<String, String> stats = reservationService.getDashboardStats();
        // Inject the logged-in username so the UI can greet the user
        stats.put("username", (String) req.getSession(false).getAttribute("user"));
        writeJson(resp, stats);
    }
}
