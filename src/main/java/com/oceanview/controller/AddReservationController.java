package com.oceanview.controller;

// අත්‍යවශ්‍ය Imports එකතු කිරීම (මෙය අනිවාර්යයි)
import com.oceanview.model.Guest;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.service.ReservationService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AddReservationController extends BaseController {

    // --- Inputs & UI Components ---
    @FXML private TextField txtFirstName, txtLastName, txtContact, txtNic, txtEmail, txtNationality;
    @FXML private TextArea txtAddress, txtSpecialRequests;
    @FXML private ComboBox<String> cmbRoomType, cmbGuests;
    @FXML private Label summRoomType, summCheckIn, summCheckOut, summNights, summRate, summTotal, lblStatus;

    // --- Services & DAOs (මෙම පේළි දෙක අනිවාර්යයෙන් තිබිය යුතුයි) ---
    private final ReservationService reservationService = new ReservationService();
    private final GuestDAOImpl guestDAO = new GuestDAOImpl(); 
    
    private double currentRate = 0.0;

    @FXML
    public void initialize() {
        if (cmbRoomType != null) {
            cmbRoomType.setItems(FXCollections.observableArrayList("Standard", "Deluxe", "Suite", "Ocean View", "Penthouse"));
        }
        if (cmbGuests != null) {
            cmbGuests.setItems(FXCollections.observableArrayList("1 Adult", "2 Adults", "Family"));
        }
    }

    @FXML
    public void onRoomTypeSelected(ActionEvent event) {
        String selected = cmbRoomType.getValue();
        if (selected != null) {
            summRoomType.setText(selected);
            switch (selected) {
                case "Standard":   currentRate = 8500; break;
                case "Deluxe":     currentRate = 12000; break;
                case "Suite":      currentRate = 18500; break;
                case "Ocean View": currentRate = 22000; break;
                case "Penthouse":  currentRate = 35000; break;
            }
            summRate.setText(String.format("LKR %,.0f", currentRate));
            calculateTotal();
        }
    }

    @FXML
    public void onDateChanged(ActionEvent event) {
        LocalDate in = dpCheckIn != null ? dpCheckIn.getValue() : null;
        LocalDate out = dpCheckOut != null ? dpCheckOut.getValue() : null;
        
        if (in != null) summCheckIn.setText(in.toString());
        if (out != null) summCheckOut.setText(out.toString());
        
        if (in != null && out != null) {
            long nights = ChronoUnit.DAYS.between(in, out);
            summNights.setText(String.valueOf(nights > 0 ? nights : 0));
            calculateTotal();
        }
    }

    private void calculateTotal() {
        try {
            long nights = Long.parseLong(summNights.getText());
            summTotal.setText(String.format("LKR %,.2f", nights * currentRate));
        } catch (Exception e) { 
            summTotal.setText("LKR 0.00"); 
        }
    }

    @FXML
    public void handleSaveReservation(ActionEvent event) {
        // 1. Basic Validation
        if (txtFirstName.getText().isEmpty() || txtContact.getText().isEmpty() || cmbRoomType.getValue() == null) {
            lblStatus.setText("Error: Fill required fields!");
            lblStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        // 2. අමුත්තාගේ දත්ත සහිත Guest Object එක සෑදීම
        Guest newGuest = new Guest(0, 
                                   txtFirstName.getText(), 
                                   txtLastName.getText(), 
                                   txtContact.getText(), 
                                   txtEmail.getText(), 
                                   txtNic.getText(), 
                                   txtNationality.getText(), 
                                   txtAddress.getText());

        // 3. Guest ව ලියාපදිංචි කර ID එක ලබා ගැනීම
        int generatedGuestId = guestDAO.addGuestAndGetId(newGuest);

        if (generatedGuestId != -1) {
            // 4. Reservation එක සිදු කිරීම (Room ID එක 1 ලෙස දැනට ලබා දී ඇත)
            boolean success = reservationService.saveReservation(
                "RES-" + System.currentTimeMillis(), 
                generatedGuestId, 
                1, 
                dpCheckIn.getValue().toString(), 
                dpCheckOut.getValue().toString()
            );

            if (success) {
                lblStatus.setText("Success: Reservation Saved!");
                lblStatus.setStyle("-fx-text-fill: green;");
                handleClear(null);
            } else {
                lblStatus.setText("Error: Could not save reservation.");
            }
        } else {
            lblStatus.setText("Error: Guest registration failed.");
        }
    }

    @FXML
    public void handleClear(ActionEvent event) {
        txtFirstName.clear(); txtLastName.clear(); txtContact.clear();
        txtNic.clear(); txtEmail.clear(); txtNationality.clear();
        txtAddress.clear(); txtSpecialRequests.clear();
        if (dpCheckIn != null) dpCheckIn.setValue(null);
        if (dpCheckOut != null) dpCheckOut.setValue(null);
        lblStatus.setText("");
    }
    
    // FXML අනිවාර්යයෙන්ම ඉල්ලන DatePicker විචල්‍යයන්
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
}