package com.oceanview.controller;

import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class ReservationsController extends BaseController {

    // --- Table Components ---
    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<Reservation, String> colResNo, colGuestName, colContact, colRoomType, colStatus, colActions;
    @FXML private TableColumn<Reservation, LocalDate> colCheckIn, colCheckOut;
    @FXML private TableColumn<Reservation, Integer> colNights;
    @FXML private TableColumn<Reservation, Double> colAmount;

    // --- Search & Filter Components ---
    @FXML private TextField txtSearch, txtDateFrom, txtDateTo;
    @FXML private ComboBox<String> cmbStatusFilter, cmbRoomTypeFilter, cmbRowsPerPage;
    
    // --- Labels & Stats ---
    @FXML private Label lblStatTotal, lblStatCheckedIn, lblStatReserved, lblStatCheckedOut, lblStatCancelled, lblResultCount;

    // --- Data Management ---
    private final ReservationDAOImpl reservationDAO = new ReservationDAOImpl();
    private ObservableList<Reservation> masterData = FXCollections.observableArrayList();
    private FilteredList<Reservation> filteredData;
    
    // --- Pagination State ---
    private int currentPage = 1;
    private int rowsPerPage = 20;

    @FXML
    public void initialize() {
        // 2. Map Table Columns to Model fields
        colResNo.setCellValueFactory(new PropertyValueFactory<>("reservationNumber"));
        colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // අලුතින් එකතු කළ තීරු (Columns) 3
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colNights.setCellValueFactory(new PropertyValueFactory<>("nights"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        // 1. Setup Global Shortcuts
        setupGlobalShortcuts(tblReservations);

        // 2. Map Table Columns to Model fields
        colResNo.setCellValueFactory(new PropertyValueFactory<>("reservationNumber"));
        colGuestName.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // Contact, Nights, Amount, Actions will remain blank until added to model/database

        // 3. Initialize Dropdowns
        cmbStatusFilter.setItems(FXCollections.observableArrayList("All Statuses", "Booked", "Available", "Cancelled"));
        cmbRoomTypeFilter.setItems(FXCollections.observableArrayList("All Room Types", "Standard", "Deluxe", "Suite", "Single", "Double"));
        cmbRowsPerPage.setItems(FXCollections.observableArrayList("10", "20", "50", "100"));
        
        cmbStatusFilter.getSelectionModel().selectFirst();
        cmbRoomTypeFilter.getSelectionModel().selectFirst();
        cmbRowsPerPage.getSelectionModel().select("20");

        // 4. Set Listeners
        txtSearch.textProperty().addListener((obs, oldText, newText) -> executeFilters());
        cmbRowsPerPage.valueProperty().addListener((obs, oldVal, newVal) -> {
            rowsPerPage = Integer.parseInt(newVal);
            currentPage = 1;
            updatePagination();
        });

        // 5. Fetch Data and Setup UI
        loadTableData();
    }

    private void loadTableData() {
        // Fetch from DB
        List<Reservation> list = reservationDAO.getRecentReservations(); 
        masterData.setAll(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        
        updateStats();
        updatePagination();
    }

    // ==========================================
    // FILTERING LOGIC
    // ==========================================

    @FXML 
    public void applyFilters(ActionEvent event) { 
        executeFilters(); 
    }

    private void executeFilters() {
        filteredData.setPredicate(res -> {
            boolean match = true;

            // 1. Search Bar Match (Res No or Name)
            String search = txtSearch.getText().toLowerCase();
            if (!search.isEmpty()) {
                match = res.getReservationNumber().toLowerCase().contains(search) ||
                        res.getGuestName().toLowerCase().contains(search);
            }

            // 2. Status Match
            String status = cmbStatusFilter.getValue();
            if (status != null && !status.equals("All Statuses")) {
                match = match && res.getStatus().equalsIgnoreCase(status);
            }

            // 3. Room Type Match
            String roomType = cmbRoomTypeFilter.getValue();
            if (roomType != null && !roomType.equals("All Room Types")) {
                match = match && res.getRoomType().equalsIgnoreCase(roomType);
            }

            // 4. Date Range Match
            try {
                if (!txtDateFrom.getText().isEmpty()) {
                    LocalDate from = LocalDate.parse(txtDateFrom.getText());
                    if (res.getCheckInDate() != null) {
                        match = match && !res.getCheckInDate().isBefore(from);
                    }
                }
                if (!txtDateTo.getText().isEmpty()) {
                    LocalDate to = LocalDate.parse(txtDateTo.getText());
                    if (res.getCheckOutDate() != null) {
                        match = match && !res.getCheckOutDate().isAfter(to);
                    }
                }
            } catch (Exception e) {
                // Ignore invalid date typing until fully typed
            }

            return match;
        });

        // Reset to page 1 after filtering and update UI
        currentPage = 1;
        updatePagination();
        lblResultCount.setText("Showing " + filteredData.size() + " results");
    }

    @FXML 
    public void clearFilters(ActionEvent event) { 
        txtSearch.clear();
        txtDateFrom.clear();
        txtDateTo.clear();
        cmbStatusFilter.getSelectionModel().selectFirst();
        cmbRoomTypeFilter.getSelectionModel().selectFirst();
        
        filteredData.setPredicate(p -> true); // Reset predicate
        currentPage = 1;
        updatePagination();
        lblResultCount.setText("Showing " + filteredData.size() + " results");
    }

    // ==========================================
    // PAGINATION LOGIC
    // ==========================================

    private void updatePagination() {
        int totalItems = filteredData.size();
        int totalPages = (int) Math.ceil((double) totalItems / rowsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, totalItems);

        // Bind only the current slice of data to the table
        tblReservations.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
    }

    @FXML 
    public void prevPage(ActionEvent event) { 
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML 
    public void nextPage(ActionEvent event) { 
        int totalPages = (int) Math.ceil((double) filteredData.size() / rowsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    @FXML 
    public void showPage(ActionEvent event) { 
        Button btn = (Button) event.getSource();
        try {
            currentPage = Integer.parseInt(btn.getText());
            updatePagination();
        } catch (NumberFormatException e) {
            // Ignore if user clicks a non-number button like "..."
        }
    }

    // ==========================================
    // STATS & EXPORT
    // ==========================================

    private void updateStats() {
        int total = masterData.size();
        int checkedIn = 0, reserved = 0, checkedOut = 0, cancelled = 0;

        for (Reservation r : masterData) {
            String s = r.getStatus();
            if (s == null) continue;
            
            if (s.equalsIgnoreCase("Booked") || s.equalsIgnoreCase("Checked In")) checkedIn++;
            else if (s.equalsIgnoreCase("Reserved")) reserved++;
            else if (s.equalsIgnoreCase("Available") || s.equalsIgnoreCase("Checked Out")) checkedOut++;
            else if (s.equalsIgnoreCase("Cancelled")) cancelled++;
        }

        lblStatTotal.setText(String.valueOf(total));
        lblStatCheckedIn.setText(String.valueOf(checkedIn));
        lblStatReserved.setText(String.valueOf(reserved));
        lblStatCheckedOut.setText(String.valueOf(checkedOut));
        lblStatCancelled.setText(String.valueOf(cancelled));
        lblResultCount.setText("Showing " + filteredData.size() + " results");
    }

    @FXML 
    public void exportToCSV(ActionEvent event) { 
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Reservations to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("Reservations_Report_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(tblReservations.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Headers
                writer.println("Reservation No,Guest Name,Room Type,Check-In,Check-Out,Status");
                
                // Write visible data to file
                for (Reservation res : filteredData) {
                    writer.println(
                        res.getReservationNumber() + "," +
                        res.getGuestName() + "," +
                        res.getRoomType() + "," +
                        (res.getCheckInDate() != null ? res.getCheckInDate() : "") + "," +
                        (res.getCheckOutDate() != null ? res.getCheckOutDate() : "") + "," +
                        res.getStatus()
                    );
                }
            } catch (Exception e) {
                System.out.println("Error saving file: " + e.getMessage());
            }
        }
    }
}