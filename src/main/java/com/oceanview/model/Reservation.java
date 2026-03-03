package com.oceanview.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private String reservationNumber;
    private String guestName;
    private String contact;      // අලුතින් එකතු කළා
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;         // අලුතින් එකතු කළා
    private double amount;       // අලුතින් එකතු කළා
    private String status;

    public Reservation(String resNum, String guest, String contact, String room, double pricePerNight, LocalDate in, LocalDate out, String status) {
        this.reservationNumber = resNum;
        this.guestName = guest;
        this.contact = contact;
        this.roomType = room;
        this.checkInDate = in;
        this.checkOutDate = out;
        this.status = status;
        
        // දින ගණන (Nights) සහ මුළු මුදල (Amount) ස්වයංක්‍රීයව ගණනය කිරීම
        if (in != null && out != null) {
            this.nights = ChronoUnit.DAYS.between(in, out);
            if (this.nights < 0) this.nights = 0;
        } else {
            this.nights = 0;
        }
        this.amount = this.nights * pricePerNight;
    }

    // Getters
    public String getReservationNumber() { return reservationNumber; }
    public String getGuestName() { return guestName; }
    public String getContact() { return contact; }
    public String getRoomType() { return roomType; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public long getNights() { return nights; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
}