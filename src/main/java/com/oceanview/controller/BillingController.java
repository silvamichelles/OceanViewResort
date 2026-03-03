package com.oceanview.controller;

import com.oceanview.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.util.Map;

public class BillingController extends BaseController {

    // --- Stats Labels ---
    @FXML private Label lblPendingAmount, lblCollectedAmount;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbStatusFilter;

    // --- Table Components ---
    @FXML private TableView<Map<String, String>> tblBilling; // Using a Map for simple display
    @FXML private TableColumn<Map<String, String>, String> colBillId, colResNo, colGuestName, colTotal, colPaid, colBalance, colDueDate, colStatus;

    @FXML
    public void initialize() {
        setupGlobalShortcuts(tblBilling);
        
        // Column Data Binding
        colBillId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("id")));
        colResNo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("res")));
        colGuestName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("name")));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("total")));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get("status")));

        cmbStatusFilter.setItems(FXCollections.observableArrayList("All", "Paid", "Pending"));
        
        loadBillingData();
    }

    private void loadBillingData() {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "SELECT b.bill_id, r.reservation_number, g.guest_name, b.total_amount " +
                     "FROM bills b " +
                     "JOIN reservations r ON b.res_id = r.res_id " +
                     "JOIN guests g ON r.guest_id = g.guest_id";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            double totalCollected = 0;
            while (rs.next()) {
                Map<String, String> row = new java.util.HashMap<>();
                row.put("id", rs.getString("bill_id"));
                row.put("res", rs.getString("reservation_number"));
                row.put("name", rs.getString("guest_name"));
                row.put("total", "LKR " + rs.getString("total_amount"));
                row.put("status", "Paid"); // Since it's in the bill table
                list.add(row);
                totalCollected += rs.getDouble("total_amount");
            }
            tblBilling.setItems(list);
            lblCollectedAmount.setText(String.format("LKR %,.2f", totalCollected));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void printReport(ActionEvent event) {
        System.out.println("Generating Billing Report PDF...");
        // Logic to export TableView to PDF using iText or standard PrinterJob
    }
}