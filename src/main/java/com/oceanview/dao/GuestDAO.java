package com.oceanview.dao;

import com.oceanview.model.Guest;
import java.util.List;

public interface GuestDAO {
    boolean addGuest(Guest guest);
    List<Guest> getAllGuests();
    Guest getGuestById(int id);
    boolean updateGuest(Guest guest);
    boolean deleteGuest(int id);
}