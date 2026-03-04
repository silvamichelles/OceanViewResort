package com.oceanview.service;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.model.Guest;

import java.util.List;

/**
 * Service layer for Guest management.
 * Applies business-rule validation before delegating to the GuestDAO.
 */
public class GuestService {

    private final GuestDAO guestDAO;

    public GuestService() {
        this.guestDAO = new GuestDAOImpl();
    }

    public List<Guest> getAllGuests() {
        return guestDAO.getAllGuests();
    }

    public Guest getGuestById(int id) {
        return guestDAO.getGuestById(id);
    }

    public boolean addGuest(String firstName, String lastName, String contact, String address) {
        if (firstName == null || firstName.trim().isEmpty()) return false;
        if (contact == null || contact.trim().isEmpty()) return false;
        Guest g = new Guest(0, firstName.trim(), lastName == null ? "" : lastName.trim(),
                contact.trim(), "", "", "", address == null ? "" : address.trim());
        return guestDAO.addGuest(g);
    }

    public boolean updateGuest(int id, String firstName, String lastName, String contact, String address) {
        if (firstName == null || firstName.trim().isEmpty()) return false;
        Guest g = new Guest(id, firstName.trim(), lastName == null ? "" : lastName.trim(),
                contact == null ? "" : contact.trim(), "", "", "",
                address == null ? "" : address.trim());
        return guestDAO.updateGuest(g);
    }

    public boolean deleteGuest(int id) {
        return guestDAO.deleteGuest(id);
    }
}
