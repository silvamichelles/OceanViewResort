package com.oceanview.dao.impl;

import com.oceanview.db.DBConnection;
import com.oceanview.model.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDAOImpl {

    // ==========================================
    // වගුවට සහ Dashboard එකට දත්ත ලබා ගැනීම
    // ==========================================

    /**
     * Database එකේ JOIN Query එකක් පාවිච්චි කරලා අමුත්තාගේ සහ කාමරයේ විස්තර ලබා ගැනීම.
     * ORDER BY r.res_id DESC නිසා අලුත්ම දත්ත උඩින්ම පෙන්වයි.
     */
    public List<Reservation> getRecentReservations() {
        List<Reservation> list = new ArrayList<>();
        
        // අත්‍යවශ්‍යම දත්ත සියල්ල ලබාගන්නා SQL Query එක
        String sql = "SELECT r.reservation_number, g.guest_name, g.contact_number, " +
                     "rm.room_type, rm.price, r.check_in_date, r.check_out_date, rm.status " +
                     "FROM reservations r " +
                     "JOIN guests g ON r.guest_id = g.guest_id " +
                     "JOIN rooms rm ON r.room_id = rm.room_id " +
                     "ORDER BY r.res_id DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                // Reservation Model එකේ Constructor එකට දත්ත යැවීම
                list.add(new Reservation(
                    rs.getString("reservation_number"),
                    rs.getString("guest_name"),
                    rs.getString("contact_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price"),
                    rs.getDate("check_in_date").toLocalDate(),
                    rs.getDate("check_out_date").toLocalDate(),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) { 
            System.err.println("දත්ත ලබා ගැනීමේ දෝෂයක්: " + e.getMessage());
            e.printStackTrace(); 
        }
        return list;
    }

    /**
     * Dashboard එකේ පෙන්වන Total, Available, Booked සහ Revenue දත්ත ලබා ගැනීම.
     */
    public Map<String, String> getDashboardStats() {
        Map<String, String> stats = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM reservations) as total, " +
                     "(SELECT COUNT(*) FROM rooms WHERE status = 'Available') as available, " +
                     "(SELECT COUNT(*) FROM rooms WHERE status = 'Booked') as booked, " +
                     "(SELECT IFNULL(SUM(total_amount), 0) FROM bills) as revenue";
                     
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
             
            if (rs.next()) {
                stats.put("total", rs.getString("total"));
                stats.put("available", rs.getString("available"));
                stats.put("booked", rs.getString("booked"));
                
                double revenue = rs.getDouble("revenue");
                stats.put("revenue", String.format("LKR %.0fK", revenue / 1000));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return stats;
    }

    // ==========================================
    // අලුතින් Reservation එකක් එකතු කිරීමේ ක්‍රම
    // ==========================================

    /**
     * දැනට හිස්ව පවතින (Available) කාමර අංක ComboBox එකට ලබා ගැනීම.
     */
    public List<String> getAvailableRoomIds() {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT room_id FROM rooms WHERE status = 'Available'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(String.valueOf(rs.getInt("room_id")));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return rooms;
    }

    /**
     * පෝරමයේ (Form) දත්ත දත්ත ගබඩාවේ reservations table එකට ඇතුළත් කිරීම.
     */
    public boolean saveReservation(String resNo, int guestId, int roomId, String checkIn, String checkOut) {
        String sql = "INSERT INTO reservations (reservation_number, guest_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, resNo);
            pst.setInt(2, guestId);
            pst.setInt(3, roomId);
            pst.setString(4, checkIn);
            pst.setString(5, checkOut);

            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("දත්ත තැන්පත් කිරීමේ දෝෂයක්: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}