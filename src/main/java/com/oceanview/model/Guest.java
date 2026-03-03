package com.oceanview.model;

/**
 * Model class representing a Guest entity.
 * Maps directly to the 'guests' table in the database.
 */
public class Guest {
    
    private int guestId;
    private String firstName;
    private String lastName;
    private String contact;
    private String email;
    private String nic; // NIC or Passport number
    private String nationality;
    private String address;

    // Default constructor (required by many Java frameworks)
    public Guest() {
    }

    // Parameterized constructor for easy object creation from the Database
    public Guest(int guestId, String firstName, String lastName, String contact, 
                 String email, String nic, String nationality, String address) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.email = email;
        this.nic = nic;
        this.nationality = nationality;
        this.address = address;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getGuestId() { 
        return guestId; 
    }
    
    public void setGuestId(int guestId) { 
        this.guestId = guestId; 
    }

    public String getFirstName() { 
        return firstName; 
    }
    
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

    public String getLastName() { 
        return lastName; 
    }
    
    public void setLastName(String lastName) { 
        this.lastName = lastName; 
    }

    /**
     * Automatically combines first and last name.
     * The JavaFX TableView PropertyValueFactory("fullName") automatically calls this!
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getContact() { 
        return contact; 
    }
    
    public void setContact(String contact) { 
        this.contact = contact; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getNic() { 
        return nic; 
    }
    
    public void setNic(String nic) { 
        this.nic = nic; 
    }

    public String getNationality() { 
        return nationality; 
    }
    
    public void setNationality(String nationality) { 
        this.nationality = nationality; 
    }

    public String getAddress() { 
        return address; 
    }
    
    public void setAddress(String address) { 
        this.address = address; 
    }
}