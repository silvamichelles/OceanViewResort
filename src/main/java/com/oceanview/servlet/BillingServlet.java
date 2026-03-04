package com.oceanview.servlet;

import com.oceanview.db.DBConnection;
import com.oceanview.service.BillingService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Billing endpoint.
 *
 * GET /api/billing          – list all bill records (joined with reservation + guest data)
 * GET /api/billing/{billId} – get a single bill for invoice display
 * POST /api/billing/calc    – stateless invoice calculation via BillingService
 *
 * Session guarded by AuthFilter.
 */
@WebServlet("/api/billing/*")
public class BillingServlet extends BaseApiServlet {

    private final BillingService billingService = new BillingService();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String path = req.getPathInfo(); // null | "/{id}" | "/calc"

        if (path != null && path.startsWith("/calc")) {
            handleCalc(req, resp);
            return;
        }

        if (path != null && path.length() > 1) {
            // Single bill invoice
            try {
                int billId = Integer.parseInt(path.substring(1));
                Map<String, Object> bill = fetchBillById(billId);
                if (bill == null) writeError(resp, 404, "Bill not found.");
                else              writeJson(resp, bill);
            } catch (NumberFormatException e) {
                writeError(resp, 400, "Invalid bill ID.");
            }
            return;
        }

        // Full list
        writeJson(resp, fetchAllBills());
    }

    // ── POST /api/billing/calc ─────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handleCalc(req, resp);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void handleCalc(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String totalStr = req.getParameter("roomTotal");
        if (totalStr == null) {
            writeError(resp, 400, "Missing 'roomTotal' parameter."); return;
        }
        try {
            double roomTotal = Double.parseDouble(totalStr);
            Map<String, Double> invoice = billingService.calculateInvoice(roomTotal);
            writeJson(resp, invoice);
        } catch (NumberFormatException e) {
            writeError(resp, 400, "Invalid roomTotal value.");
        }
    }

    private List<Map<String, Object>> fetchAllBills() throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT b.bill_id, b.total_nights, b.total_amount, b.billing_date, " +
                     "r.reservation_number, r.check_in_date, r.check_out_date, " +
                     "g.guest_name, g.contact_number, rm.room_type, rm.rate_per_night " +
                     "FROM bills b " +
                     "JOIN reservations r ON b.res_id = r.res_id " +
                     "JOIN guests g       ON r.guest_id = g.guest_id " +
                     "JOIN rooms rm       ON r.room_id = rm.room_id " +
                     "ORDER BY b.bill_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(buildBillMap(rs, false));
            }
        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
        return list;
    }

    private Map<String, Object> fetchBillById(int billId) throws IOException {
        String sql = "SELECT b.bill_id, b.total_nights, b.total_amount, b.billing_date, " +
                     "r.reservation_number, r.check_in_date, r.check_out_date, " +
                     "g.guest_name, g.contact_number, rm.room_type, rm.rate_per_night " +
                     "FROM bills b " +
                     "JOIN reservations r ON b.res_id = r.res_id " +
                     "JOIN guests g       ON r.guest_id = g.guest_id " +
                     "JOIN rooms rm       ON r.room_id = rm.room_id " +
                     "WHERE b.bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return buildBillMap(rs, true);
            }
        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
        return null;
    }

    private Map<String, Object> buildBillMap(ResultSet rs, boolean withCalc) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("billId",            rs.getInt("bill_id"));
        m.put("reservationNumber", rs.getString("reservation_number"));
        m.put("guestName",         rs.getString("guest_name"));
        m.put("contact",           rs.getString("contact_number"));
        m.put("roomType",          rs.getString("room_type"));
        m.put("ratePerNight",      rs.getDouble("rate_per_night"));
        m.put("totalNights",       rs.getInt("total_nights"));
        m.put("totalAmount",       rs.getDouble("total_amount"));
        m.put("checkIn",           rs.getString("check_in_date"));
        m.put("checkOut",          rs.getString("check_out_date"));
        m.put("billingDate",       rs.getString("billing_date"));

        if (withCalc) {
            // Enrich with the BillingService breakdown
            Map<String, Double> calc = billingService.calculateInvoice(rs.getDouble("total_amount"));
            m.put("subtotal",        calc.get("subtotal"));
            m.put("tax",             calc.get("tax"));
            m.put("serviceCharge",   calc.get("serviceCharge"));
            m.put("grandTotal",      calc.get("total"));
        }
        return m;
    }
}
