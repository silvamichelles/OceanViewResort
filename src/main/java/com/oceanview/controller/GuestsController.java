package com.oceanview.controller;

import com.oceanview.dao.impl.GuestDAOImpl; // DAO එකතු කළා
import com.oceanview.model.Guest;
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
import java.util.List;

public class GuestsController extends BaseController {

    // --- Inputs & Filters ---
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbNationality, cmbGuestStatus;
    
    // --- Table Components ---
    @FXML private TableView<Guest> tblGuests;
    @FXML private TableColumn<Guest, Integer> colGuestId;
    @FXML private TableColumn<Guest, String> colFullName, colContact, colEmail, colNic, colNationality, colTotalStays, colLastStay, colActions;
    
    // --- Stats Labels ---
    @FXML private Label lblResultCount, lblTotalGuests, lblLoggedInUser;

    // Data Management
    private final GuestDAOImpl guestDAO = new GuestDAOImpl(); // Database සම්බන්ධතාවය සඳහා
    private ObservableList<Guest> masterData = FXCollections.observableArrayList();
    private FilteredList<Guest> filteredData;

    @FXML
    public void initialize() {
        // 1. වගුවේ තීරු (Columns) වලට Model එකේ දත්ත සම්බන්ධ කිරීම
        if (colGuestId != null) colGuestId.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        if (colFullName != null) colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (colContact != null) colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colNic != null) colNic.setCellValueFactory(new PropertyValueFactory<>("nic"));
        if (colNationality != null) colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));

        // 2. Dropdowns සැකසීම
        if (cmbNationality != null) {
            cmbNationality.setItems(FXCollections.observableArrayList("All Nationalities", "Local", "Foreign"));
            cmbNationality.getSelectionModel().selectFirst();
        }
        if (cmbGuestStatus != null) {
            cmbGuestStatus.setItems(FXCollections.observableArrayList("All Statuses", "Checked In", "Checked Out"));
            cmbGuestStatus.getSelectionModel().selectFirst();
        }

        // 3. දත්ත ගබඩාවෙන් දත්ත ලබාගෙන වගුවට දැමීම
        loadGuestData();

        // 4. සෙවුම් (Search) පහසුකම සැකසීම
        setupSearch();
    }

    private void loadGuestData() {
        try {
            // DAO හරහා දත්ත ගබඩාවෙන් සියලුම Guests ලබා ගැනීම
            List<Guest> guests = guestDAO.getAllGuests();
            masterData.setAll(guests);
            
            // Stats Label එක යාවත්කාලීන කිරීම
            if (lblTotalGuests != null) {
                lblTotalGuests.setText(String.valueOf(masterData.size()));
            }
        } catch (Exception e) {
            System.err.println("දත්ත ලබා ගැනීමේ දෝෂයක්: " + e.getMessage());
        }
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(guest -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (guest.getFullName().toLowerCase().contains(lowerCaseFilter)) return true;
                    if (guest.getContact() != null && guest.getContact().contains(newValue)) return true;
                    return false;
                });
                updateResultCount();
            });
        }

        if (tblGuests != null) {
            tblGuests.setItems(filteredData);
        }
        updateResultCount();
    }

    private void updateResultCount() {
        if (lblResultCount != null) {
            lblResultCount.setText("Showing " + filteredData.size() + " results");
        }
    }

    @FXML
    public void applyFilters(ActionEvent event) {
        // අනාගතයේදී වැඩිදුර Filter කිරීම් මෙතැනට එක් කළ හැක
        System.out.println("Filters applied.");
    }

    @FXML
    public void clearFilters(ActionEvent event) {
        if (txtSearch != null) txtSearch.clear();
        cmbNationality.getSelectionModel().selectFirst();
        cmbGuestStatus.getSelectionModel().selectFirst();
    }

    @FXML
    public void exportToCSV(ActionEvent event) {
        if (tblGuests == null || tblGuests.getScene() == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Guests to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("Guests_Report.csv");

        File file = fileChooser.showSaveDialog(tblGuests.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Guest ID,Full Name,Contact,Address");
                for (Guest guest : masterData) {
                    writer.println(guest.getGuestId() + "," + guest.getFullName() + "," + guest.getContact() + "," + guest.getAddress());
                }
                new Alert(Alert.AlertType.INFORMATION, "Export Successful!").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Export Failed!").showAndWait();
            }
        }
    }
}