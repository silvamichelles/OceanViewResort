package com.oceanview.dao;

import com.oceanview.model.Room;
import java.util.List;

public interface RoomDAO {
    boolean addRoom(Room room);
    List<Room> getAllRooms();
    Room getRoomByNumber(String roomNumber);
    boolean updateRoomStatus(String roomNumber, String status);
}