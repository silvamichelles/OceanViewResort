package com.oceanview.dao.impl;

import com.oceanview.dao.UserDAO;
import com.oceanview.db.DBConnection;
import java.sql.*;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next(); // දත්ත තිබේ නම් true යවයි
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    @Override
    public boolean register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getAllUsers() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        String sql = "SELECT user_id, username, role FROM users ORDER BY user_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("userId",   rs.getInt("user_id"));
                m.put("username", rs.getString("username"));
                m.put("role",     rs.getString("role"));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}