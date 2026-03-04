package com.oceanview.servlet;

import com.oceanview.model.Guest;
import com.oceanview.service.GuestService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD endpoint for Guest management.
 *
 * GET    /api/guests       – list all guests
 * GET    /api/guests/{id}  – get single guest
 * POST   /api/guests       – add a new guest
 * PUT    /api/guests/{id}  – update an existing guest
 * DELETE /api/guests/{id}  – remove a guest (cascades to reservations)
 *
 * Session guarded by AuthFilter.
 */
@WebServlet("/api/guests/*")
public class GuestServlet extends BaseApiServlet {

    private final GuestService guestService = new GuestService();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo(); // null | "/{id}"

        if (path != null && path.length() > 1) {
            try {
                int id = Integer.parseInt(path.substring(1));
                Guest g = guestService.getGuestById(id);
                if (g == null) { writeError(resp, 404, "Guest not found."); return; }
                writeJson(resp, toMap(g));
            } catch (NumberFormatException e) {
                writeError(resp, 400, "Invalid guest ID.");
            }
            return;
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Guest g : guestService.getAllGuests()) out.add(toMap(g));
        writeJson(resp, out);
    }

    // ── POST ──────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String firstName = req.getParameter("firstName");
        String lastName  = req.getParameter("lastName");
        String contact   = req.getParameter("contact");
        String address   = req.getParameter("address");

        boolean ok = guestService.addGuest(firstName, lastName, contact, address);
        if (ok) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeSuccess(resp, "Guest added successfully.");
        } else {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Validation failed – first name and contact are required.");
        }
    }

    // ── PUT ───────────────────────────────────────────────────────────────────
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            writeError(resp, 400, "Missing guest ID in path."); return;
        }
        try {
            int id = Integer.parseInt(path.substring(1));
            String firstName = req.getParameter("firstName");
            String lastName  = req.getParameter("lastName");
            String contact   = req.getParameter("contact");
            String address   = req.getParameter("address");
            boolean ok = guestService.updateGuest(id, firstName, lastName, contact, address);
            if (ok) writeSuccess(resp, "Guest updated.");
            else    writeError(resp, 400, "Update failed – check input fields.");
        } catch (NumberFormatException e) {
            writeError(resp, 400, "Invalid guest ID.");
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            writeError(resp, 400, "Missing guest ID."); return;
        }
        try {
            int id = Integer.parseInt(path.substring(1));
            boolean ok = guestService.deleteGuest(id);
            if (ok) writeSuccess(resp, "Guest deleted.");
            else    writeError(resp, 404, "Guest not found.");
        } catch (NumberFormatException e) {
            writeError(resp, 400, "Invalid guest ID.");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Guest g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("guestId",   g.getGuestId());
        m.put("fullName",  g.getFullName());
        m.put("firstName", g.getFirstName());
        m.put("lastName",  g.getLastName());
        m.put("contact",   g.getContact());
        m.put("email",     g.getEmail());
        m.put("address",   g.getAddress());
        return m;
    }
}
