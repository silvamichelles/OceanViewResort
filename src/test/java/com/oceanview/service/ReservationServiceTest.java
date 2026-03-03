package com.oceanview.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {

    private final ReservationService service = new ReservationService();

    @Test
    public void testInvalidCheckOutDate() {
        // පරීක්ෂණය 01: Check-out දිනය Check-in දිනයට වඩා පෙර වීම
        boolean result = service.saveReservation("RES-TEST-01", 1, 1, "2026-10-20", "2026-10-15");
        assertFalse(result, "Check-out date must be after check-in!");
    }

    @Test
    public void testPastCheckInDate() {
        // පරීක්ෂණය 02: අතීත දිනයන් සඳහා වෙන්කරවා ගැනීම් සිදු කිරීම
        boolean result = service.saveReservation("RES-TEST-02", 1, 1, "2020-01-01", "2020-01-10");
        assertFalse(result, "Cannot create reservations for past dates!");
    }

    @Test
    public void testEmptyReservationData() {
        // පරීක්ෂණය 03: අත්‍යවශ්‍ය දත්ත (Reservation Number) ලබා නොදීම
        boolean result = service.saveReservation("", 1, 1, "2026-12-01", "2026-12-10");
        assertFalse(result, "Should not allow empty reservation numbers!");
    }
}