package com.oceanview.controller;

import com.oceanview.model.Room;
import com.oceanview.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class RoomsController extends BaseController {

    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, String> colRoomNo, colType, colStatus;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TextField txtRoomNumber, txtPrice;
    @FXML private ComboBox<String> cmbRoomType;
    @FXML private Label lblRoomStatus;

    private final RoomService roomService = new RoomService();
    private ObservableList<Room> roomData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Safety Guard: Check if table exists before setting up shortcuts
        if (tblRooms != null) {
            setupGlobalShortcuts(tblRooms);
            
            // Safety Guard: Bind Columns only if they were found in FXML
            if (colRoomNo != null) colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("type"));
            if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            
            loadRoomData();
        }

        if (cmbRoomType != null) {
            cmbRoomType.setItems(FXCollections.observableArrayList("Standard", "Deluxe", "Suite", "Penthouse"));
        }
    }

    private void loadRoomData() {
        if (tblRooms == null) return;
        List<Room> rooms = roomService.getAllRooms();
        roomData.setAll(rooms);
        tblRooms.setItems(roomData);
    }

    // FXML Error එක (LoadException) නැති කිරීමට මෙය අලුතින් එකතු කරන ලදී
    @FXML
    public void showAddRoom(ActionEvent event) {
        System.out.println("Add Room Action Triggered!");
        // ඔබට වෙනත් කවුළුවක් (Pane එකක්) පෙන්වීමට අවශ්‍ය නම් මෙතැන කේතය ලියන්න
    }

    @FXML
    public void handleAddRoom(ActionEvent event) {
        try {
            String roomNo = txtRoomNumber.getText();
            String type = cmbRoomType.getValue();
            double price = Double.parseDouble(txtPrice.getText());

            boolean success = roomService.addRoom(roomNo, type, price);

            if (success) {
                if (lblRoomStatus != null) {
                    lblRoomStatus.setText("Room added successfully!");
                    lblRoomStatus.setStyle("-fx-text-fill: green;");
                }
                clearForm();
                loadRoomData();
            } else if (lblRoomStatus != null) {
                lblRoomStatus.setText("Error: Room number exists.");
                lblRoomStatus.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            if (lblRoomStatus != null) lblRoomStatus.setText("Error: Invalid input.");
        }
    }

    private void clearForm() {
        if (txtRoomNumber != null) txtRoomNumber.clear();
        if (txtPrice != null) txtPrice.clear();
        if (cmbRoomType != null) cmbRoomType.getSelectionModel().clearSelection();
    }
}