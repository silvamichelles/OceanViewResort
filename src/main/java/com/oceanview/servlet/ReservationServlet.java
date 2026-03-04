package com.oceanview.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanview.model.Reservation;
import com.oceanview.service.ReservationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * REST-style servlet for reservation management.
 *
 * GET  /api/reservations         -> lists recent reservations (JSON)
 * POST /api/reservations         -> creates a new reservation
 * GET  /api/reservations/rooms   -> lists available room IDs (JSON)
 *
 * All endpoints require an authenticated session.
 */
@WebServlet("/api/reservations/*")
public class ReservationServlet extends HttpServlet {

    private final ReservationService reservationService = new ReservationService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        String pathInfo = req.getPathInfo(); // null or "/rooms"

        resp.setContentType("application/json;charset=UTF-8");

        if ("/rooms".equals(pathInfo)) {
            // GET /api/reservations/rooms
            List<String> rooms = reservationService.getAvailableRoomIds();
            resp.getWriter().write(mapper.writeValueAsString(rooms));
        } else {
            // GET /api/reservations
            List<Reservation> list = reservationService.getRecentReservations();
            resp.getWriter().write(mapper.writeValueAsString(list));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        String resNo    = req.getParameter("resNo");
        String guestId  = req.getParameter("guestId");
        String roomId   = req.getParameter("roomId");
        String checkIn  = req.getParameter("checkIn");
        String checkOut = req.getParameter("checkOut");

        resp.setContentType("application/json;charset=UTF-8");

        try {
            boolean saved = reservationService.saveReservation(
                resNo,
                Integer.parseInt(guestId),
                Integer.parseInt(roomId),
                checkIn,
                checkOut
            );

            if (saved) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("{\"status\":\"success\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Validation failed\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid guestId or roomId\"}");
        }
    }

    private boolean isAuthenticated(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("loggedInUser") != null;
    }
}
