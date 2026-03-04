package com.oceanview.servlet;

import com.oceanview.db.DBConnection;
import com.oceanview.model.Reservation;
import com.oceanview.service.ReservationService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint for Reservation management.
 *
 * GET    /api/reservations         – list recent reservations (JSON array)
 * GET    /api/reservations/rooms   – list available room IDs
 * POST   /api/reservations         – create a new reservation
 * DELETE /api/reservations/{resNo} – cancel a reservation
 *
 * Session guarded by AuthFilter.
 */
@WebServlet("/api/reservations/*")
public class ReservationServlet extends BaseApiServlet {

    private final ReservationService reservationService = new ReservationService();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo(); // null | "/rooms"

        if ("/rooms".equals(path)) {
            writeJson(resp, reservationService.getAvailableRoomIds());
            return;
        }

        // Convert Reservation objects → plain Maps for safe JSON (LocalDate → String)
        List<Map<String, Object>> out = new ArrayList<>();
        for (Reservation r : reservationService.getRecentReservations()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("reservationNumber", r.getReservationNumber());
            m.put("guestName",         r.getGuestName());
            m.put("contact",           r.getContact());
            m.put("roomType",          r.getRoomType());
            m.put("checkIn",           r.getCheckInDate()  != null ? r.getCheckInDate().toString()  : "");
            m.put("checkOut",          r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "");
            m.put("nights",            r.getNights());
            m.put("amount",            r.getAmount());
            m.put("status",            r.getStatus());
            out.add(m);
        }
        writeJson(resp, out);
    }

    // ── POST ──────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String resNo    = req.getParameter("resNo");
        String guestId  = req.getParameter("guestId");
        String roomId   = req.getParameter("roomId");
        String checkIn  = req.getParameter("checkIn");
        String checkOut = req.getParameter("checkOut");

        // Auto-generate reservation number if blank
        if (resNo == null || resNo.trim().isEmpty()) {
            resNo = "RES-" + System.currentTimeMillis();
        }

        try {
            boolean ok = reservationService.saveReservation(
                    resNo.trim(),
                    Integer.parseInt(guestId),
                    Integer.parseInt(roomId),
                    checkIn, checkOut);
            if (ok) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                writeSuccess(resp, "Reservation " + resNo + " created successfully.");
            } else {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Validation failed – check dates and room availability.");
            }
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid guest or room ID.");
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo(); // "/RES-123"
        if (path == null || path.length() < 2) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing reservation number in path.");
            return;
        }
        String resNo = path.substring(1); // strip leading "/"
        String sql = "DELETE FROM reservations WHERE reservation_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resNo);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                writeSuccess(resp, "Reservation " + resNo + " deleted.");
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Reservation not found.");
            }
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
