package com.oceanview.model;

public class Room {
    private String roomNumber;
    private String type;
    private double price;
    private String status;
    private String floor;
    private String amenities;

    // Empty constructor for frameworks
    public Room() {}

    // Main constructor used by DAO
    public Room(String roomNumber, String type, double price, String status) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = status;
        this.floor = "1st"; // Default values
        this.amenities = "AC, WiFi, TV"; 
    }

    // Constructor for adding new rooms (Task 2)
    public Room(String roomNumber, String type, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = "Available";
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFloor() { return floor; }
    public String getAmenities() { return amenities; }
}