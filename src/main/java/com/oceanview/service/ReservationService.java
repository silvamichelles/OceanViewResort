package com.oceanview.service;

import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.model.Reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Service Layer (Business Logic).
 * Acts as the middleman between the UI Controllers and the Database DAOs.
 */
public class ReservationService {

    private final ReservationDAOImpl reservationDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAOImpl();
    }

    /**
     * Fetches recent reservations for tables.
     */
    public List<Reservation> getRecentReservations() {
        return reservationDAO.getRecentReservations();
    }

    /**
     * Fetches aggregated statistics for the dashboard.
     */
    public Map<String, String> getDashboardStats() {
        return reservationDAO.getDashboardStats();
    }

    /**
     * Fetches available rooms for the dropdown menu.
     */
    public List<String> getAvailableRoomIds() {
        return reservationDAO.getAvailableRoomIds();
    }

    /**
     * Saves a reservation ONLY IF it passes business logic validation.
     * Task 2: Implementing proper business rules and validation.
     */
    public boolean saveReservation(String resNo, int guestId, int roomId, String checkInStr, String checkOutStr) {
        
        // --- BUSINESS LOGIC 1: Prevent Empty Data ---
        if (resNo == null || resNo.trim().isEmpty() || checkInStr == null || checkOutStr == null) {
            System.out.println("Service Error: Missing required reservation data.");
            return false;
        }

        // --- BUSINESS LOGIC 2: Date Validation ---
        try {
            LocalDate checkIn = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);

            // A guest cannot check out before or on the same day they check in
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                System.out.println("Service Error: Check-out date must be AFTER check-in date.");
                return false; 
            }
            
            // A guest cannot book a date in the past
            if (checkIn.isBefore(LocalDate.now())) {
                System.out.println("Service Error: Cannot create reservations for past dates.");
                return false;
            }

        } catch (DateTimeParseException e) {
            System.out.println("Service Error: Invalid date format.");
            return false;
        }

        // If all business rules pass, send data to the DAO to save in the database
        return reservationDAO.saveReservation(resNo, guestId, roomId, checkInStr, checkOutStr);
    }
}