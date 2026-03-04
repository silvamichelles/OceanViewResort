package com.oceanview.servlet;

import com.oceanview.db.DBConnection;
import com.oceanview.model.Room;
import com.oceanview.service.RoomService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for Room management.
 *
 * GET    /api/rooms             – list all rooms
 * POST   /api/rooms             – add a new room
 * PUT    /api/rooms/{id}/status – update room status (Available / Booked / Maintenance)
 * DELETE /api/rooms/{id}        – remove a room
 *
 * Session guarded by AuthFilter.
 */
@WebServlet("/api/rooms/*")
public class RoomServlet extends BaseApiServlet {

    private final RoomService roomService = new RoomService();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Room r : roomService.getAllRooms()) out.add(toMap(r));
        writeJson(resp, out);
    }

    // ── POST ──────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String roomNumber = req.getParameter("roomNumber");
        String type       = req.getParameter("type");
        String priceStr   = req.getParameter("price");

        try {
            double price = Double.parseDouble(priceStr);
            boolean ok = roomService.addRoom(roomNumber, type, price);
            if (ok) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                writeSuccess(resp, "Room " + roomNumber + " added successfully.");
            } else {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Validation failed – check room number, type, and price > 0.");
            }
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid price value.");
        }
    }

    // ── PUT ───────────────────────────────────────────────────────────────────
    // Path: /api/rooms/{id}/status  or  /api/rooms/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo(); // "/{id}/status" or "/{id}"
        if (path == null || path.length() < 2) {
            writeError(resp, 400, "Missing room ID in path."); return;
        }
        String[] parts = path.replaceFirst("^/", "").split("/");
        String roomId = parts[0];
        String newStatus = req.getParameter("status");

        if (newStatus == null || newStatus.trim().isEmpty()) {
            writeError(resp, 400, "Missing 'status' parameter."); return;
        }

        boolean ok = roomService.getAllRooms().stream()
                .anyMatch(r -> r.getRoomNumber().equals(roomId));
        if (!ok) { writeError(resp, 404, "Room not found."); return; }

        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.trim());
            ps.setString(2, roomId);
            int rows = ps.executeUpdate();
            if (rows > 0) writeSuccess(resp, "Room " + roomId + " status updated to " + newStatus + ".");
            else writeError(resp, 404, "Room not found.");
        } catch (SQLException e) {
            writeError(resp, 500, e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            writeError(resp, 400, "Missing room ID."); return;
        }
        String roomId = path.substring(1).split("/")[0];
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            int rows = ps.executeUpdate();
            if (rows > 0) writeSuccess(resp, "Room " + roomId + " deleted.");
            else writeError(resp, 404, "Room not found.");
        } catch (SQLException e) {
            writeError(resp, 500, "Cannot delete – room may have active reservations.");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Room r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("roomId",     r.getRoomNumber());
        m.put("type",       r.getType());
        m.put("price",      r.getPrice());
        m.put("status",     r.getStatus());
        m.put("floor",      r.getFloor());
        m.put("amenities",  r.getAmenities());
        return m;
    }
}
