package com.oceanview.controller;

import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardController extends BaseController {

    // --- Table Components ---
    @FXML private TableView<Reservation> tblRecentReservations;
    @FXML private TableColumn<Reservation, String> colResNo, colGuestName, colRoomType, colStatus;
    @FXML private TableColumn<Reservation, LocalDate> colCheckIn, colCheckOut;
    
    // --- Search Components ---
    @FXML private TextField txtSearch; 
    
    // --- Top Summary Cards ---
    @FXML private Label lblTotalReservations, lblCheckedIn, lblRevenue;
    
    // --- Room Status Overview Cards ---
    @FXML private Label lblAvailableRooms; 
    @FXML private Label lblRoomAvailable, lblRoomOccupied, lblRoomMaintenance;
    
    // --- Today's Activity Cards ---
    @FXML private Label lblCheckinsToday, lblPendingPayments;
    
    // --- User Info ---
    @FXML private Label lblLoggedInUser;

    private final ReservationDAOImpl reservationDAO = new ReservationDAOImpl();
    private ObservableList<Reservation> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Activate global F1-F12 shortcuts for this specific window
        setupGlobalShortcuts(tblRecentReservations);

        // 2. Bind Table Columns to the Reservation model properties
        colResNo.setCellValueFactory(new PropertyValueFactory<>("reservationNumber"));
        colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set logged in user dynamically
        if(lblLoggedInUser != null) {
            lblLoggedInUser.setText("Admin");
        }

        // 3. Load Data and setup search
        loadDashboardData();
        setupSearch();
    }

    private void loadDashboardData() {
        // Fetch recent reservations and update table data
        List<Reservation> list = reservationDAO.getRecentReservations();
        masterData.setAll(list);
        tblRecentReservations.setItems(masterData);

        // Fetch aggregated statistics from the database and update cards
        Map<String, String> stats = reservationDAO.getDashboardStats();
        
        // Populate Top Row Cards safely
        if (lblTotalReservations != null) lblTotalReservations.setText(stats.getOrDefault("total", "0"));
        if (lblCheckedIn != null) lblCheckedIn.setText(stats.getOrDefault("booked", "0"));
        if (lblRevenue != null) lblRevenue.setText(stats.getOrDefault("revenue", "LKR 0K"));

        // Populate Room Status Overview
        String available = stats.getOrDefault("available", "0");
        String booked = stats.getOrDefault("booked", "0");
        
        if (lblRoomAvailable != null) lblRoomAvailable.setText(available + " rooms");
        if (lblAvailableRooms != null) lblAvailableRooms.setText(available); // Top card
        if (lblRoomOccupied != null) lblRoomOccupied.setText(booked + " rooms");
        
        // Placeholder values for data pending DB triggers
        if (lblRoomMaintenance != null) lblRoomMaintenance.setText("2 rooms"); 
        if (lblCheckinsToday != null) lblCheckinsToday.setText("5 Guests");    
        if (lblPendingPayments != null) lblPendingPayments.setText("1 Bills"); 
    }

    /**
     * Applies a FilteredList to the masterData to enable real-time search.
     */
    private void setupSearch() {
        FilteredList<Reservation> filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(reservation -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                
                // Search by Reservation Number or Guest Name
                if (reservation.getReservationNumber().toLowerCase().contains(lowerCaseFilter)) return true;
                if (reservation.getGuestName().toLowerCase().contains(lowerCaseFilter)) return true;
                
                return false; 
            });
        });

        // Display the filtered results in the table
        tblRecentReservations.setItems(filteredData);
    }

    /**
     * Overrides the printReport method in BaseController to actually print the TableView.
     * Demonstrates advanced JavaFX capabilities for Assessment Task B.
     */
    @FXML
    @Override
    public void printReport(ActionEvent event) {
        System.out.println("Preparing to print report...");
        
        // Create a new JavaFX PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
        
        if (job != null) {
            // Show the native OS Print Dialog (Allows selecting physical printer or 'Print to PDF')
            boolean showDialog = job.showPrintDialog(tblRecentReservations.getScene().getWindow());
            
            if (showDialog) {
                // Print the actual TableView UI node
                boolean success = job.printPage(tblRecentReservations);
                if (success) {
                    job.endJob(); // Finish and send to printer
                    System.out.println("Print job completed successfully.");
                } else {
                    System.out.println("Print job failed to render page.");
                }
            } else {
                System.out.println("Print job cancelled by user.");
            }
        } else {
            System.out.println("No printer setup found on this system.");
        }
    }
}