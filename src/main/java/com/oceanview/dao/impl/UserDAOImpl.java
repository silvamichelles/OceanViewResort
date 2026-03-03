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
}