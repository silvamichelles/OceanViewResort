package com.oceanview.dao.impl;

import com.oceanview.dao.RoomDAO;
import com.oceanview.db.DBConnection;
import com.oceanview.model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {

    @Override
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_id, room_type, rate_per_night, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, room.getRoomNumber());
            pst.setString(2, room.getType());
            pst.setDouble(3, room.getPrice());
            pst.setString(4, room.getStatus());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Room(
                    rs.getString("room_id"),
                    rs.getString("room_type"),
                    rs.getDouble("rate_per_night"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Room getRoomByNumber(String roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, roomNumber);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Room(rs.getString("room_id"), rs.getString("room_type"), rs.getDouble("rate_per_night"), rs.getString("status"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean updateRoomStatus(String roomNumber, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setString(2, roomNumber);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}