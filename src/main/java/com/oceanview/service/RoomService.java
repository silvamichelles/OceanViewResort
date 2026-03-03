package com.oceanview.service;

import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.model.Room;

import java.util.List;

/**
 * Service Layer (Business Logic) for Room Management.
 * Validates data before sending it to the Data Access Object (DAO).
 */
public class RoomService {

    // Programming to an Interface is a best practice in Java
    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAOImpl();
    }

    /**
     * Fetches all rooms from the database via the DAO.
     */
    public List<Room> getAllRooms() {
        return roomDAO.getAllRooms();
    }

    /**
     * Validates business rules before adding a new room to the database.
     * Task 2: Implementing proper business rules and validation.
     */
    public boolean addRoom(String roomNumber, String type, double price) {
        
        // --- BUSINESS LOGIC 1: Prevent Empty Data ---
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            System.out.println("Service Error: Room number cannot be empty.");
            return false;
        }

        if (type == null || type.trim().isEmpty()) {
            System.out.println("Service Error: Room type must be selected.");
            return false;
        }

        // --- BUSINESS LOGIC 2: Validate Price ---
        // Ensure staff cannot accidentally save a free or negative-priced room
        if (price <= 0) {
            System.out.println("Service Error: Room price must be greater than 0.");
            return false;
        }

        // If all validations pass, create the Room object and send it to the DAO
        Room newRoom = new Room(roomNumber, type, price);
        return roomDAO.addRoom(newRoom);
    }
}