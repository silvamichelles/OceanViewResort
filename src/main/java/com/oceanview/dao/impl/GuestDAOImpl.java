package com.oceanview.dao.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.db.DBConnection;
import com.oceanview.model.Guest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAOImpl implements GuestDAO {

    @Override
    public boolean addGuest(Guest guest) {
        String sql = "INSERT INTO guests (guest_name, address, contact_number) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, guest.getFirstName() + " " + guest.getLastName());
            pst.setString(2, guest.getAddress());
            pst.setString(3, guest.getContact());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Guest> getAllGuests() {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                // We split the full name from DB back into first/last for the model
                String fullName = rs.getString("guest_name");
                String[] names = fullName.split(" ", 2);
                String fName = names[0];
                String lName = (names.length > 1) ? names[1] : "";

                list.add(new Guest(
                    rs.getInt("guest_id"),
                    fName,
                    lName,
                    rs.getString("contact_number"),
                    "", // Email (add to DB later if needed)
                    "", // NIC (add to DB later if needed)
                    "", // Nationality (add to DB later if needed)
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Guest getGuestById(int id) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String fullName = rs.getString("guest_name");
                String[] names = fullName.split(" ", 2);
                return new Guest(rs.getInt("guest_id"), names[0], (names.length > 1 ? names[1] : ""), 
                                 rs.getString("contact_number"), "", "", "", rs.getString("address"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE guests SET guest_name = ?, address = ?, contact_number = ? WHERE guest_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, guest.getFirstName() + " " + guest.getLastName());
            pst.setString(2, guest.getAddress());
            pst.setString(3, guest.getContact());
            pst.setInt(4, guest.getGuestId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteGuest(int id) {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int addGuestAndGetId(Guest guest) {
    // Database එකේ Auto-increment වන නිසා guest_id එක SQL එකට අවශ්‍ය නැත
    String sql = "INSERT INTO guests (guest_name, contact_number, address) VALUES (?, ?, ?)";
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        pst.setString(1, guest.getFirstName() + " " + guest.getLastName());
        pst.setString(2, guest.getContact());
        pst.setString(3, guest.getAddress());
        
        int affectedRows = pst.executeUpdate();
        if (affectedRows > 0) {
            // අලුතින් හැදුණු ID එක ලබා ගැනීම
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1); 
            }
        }
    } catch (SQLException e) { 
        e.printStackTrace(); 
    }
    return -1; // දෝෂයක් වුවහොත් -1 ලබා දෙයි
}
}