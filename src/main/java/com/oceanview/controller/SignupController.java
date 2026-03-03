package com.oceanview.controller;

import com.oceanview.service.UserService;
import com.oceanview.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException; // අනිවාර්යයෙන්ම තිබිය යුතු import එක

public class SignupController extends BaseController {

    @FXML private TextField txtRegUsername;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtRegConfirmPassword;
    @FXML private Label lblRegError;
    @FXML private Button btnSignup;

    private final UserService userService = new UserService();

    @FXML
    void handleSignup(ActionEvent event) {
        String username = txtRegUsername.getText().trim();
        String password = txtRegPassword.getText();
        String confirmPassword = txtRegConfirmPassword.getText();

        // 1. Basic Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        // 2. Password Matching Check
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        // 3. Register the user (Defaulting role to 'Staff')
        boolean success = userService.registerUser(username, password, "Staff");

        if (success) {
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Account Created");
            alert.setHeaderText(null);
            alert.setContentText("Staff account registered successfully!");
            alert.showAndWait();

            // Redirect to Login page
            goToLogin(null);
        } else {
            showError("Signup failed. Username might already exist.");
        }
    }

    @FXML
    void goToLogin(MouseEvent event) {
        Stage stage = (Stage) btnSignup.getScene().getWindow();
        
        // අලුත් NavigationUtil එකට ගැලපෙන පරිදි සහ IOException පාලනය කිරීමට try-catch එකතු කර ඇත
        try {
            NavigationUtil.navigateTo("login.fxml");
        } catch (IOException e) {
            showError("Failed to load Login page!");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblRegError.setText(message);
        lblRegError.setStyle("-fx-text-fill: #e74c3c;"); // Consistent red color
    }
}