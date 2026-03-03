package com.oceanview.controller;

import com.oceanview.service.UserService;
import com.oceanview.util.NavigationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent; // Correct import for Label clicks
import javafx.stage.Stage;
import java.io.IOException; // අනිවාර්යයෙන්ම තිබිය යුතු import එක
import java.util.concurrent.CompletableFuture;

/**
 * Handles User Authentication as required by Task 1 of the Assessment Brief.
 * Uses Asynchronous processing to prevent UI freezing during DB lookups.
 */
public class LoginController extends BaseController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final UserService userService = new UserService();

    /**
     * Handles the "Sign In" button action.
     * Implements basic validation and background thread execution (LO II).
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Basic Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter all fields!");
            return;
        }

        // UX: Provide immediate feedback
        lblError.setStyle("-fx-text-fill: #0066cc;");
        lblError.setText("Authenticating...");
        btnLogin.setDisable(true); 

        // Task B: Distributed logic simulation using CompletableFuture
        CompletableFuture.supplyAsync(() -> userService.login(username, password))
            .thenAccept(isValidUser -> {
                // Return to JavaFX Thread to update UI
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);

                    if (isValidUser) {
                        Stage stage = (Stage) btnLogin.getScene().getWindow();
                        
                        // මෙතැනට try-catch එකතු කර ඇත
                        try {
                            // අපගේ නව NavigationUtil එකට ගැලපෙන පරිදි නම වෙනස් කර ඇත
                            NavigationUtil.navigateTo("Dashboard.fxml");
                        } catch (IOException e) {
                            showError("Failed to load Dashboard!");
                            e.printStackTrace();
                        }
                        
                    } else {
                        showError("Invalid Username or Password!");
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    showError("Connection error. Please try again later.");
                    ex.printStackTrace(); 
                });
                return null;
            });
    }

    /**
     * Navigates to the Signup screen when the "Sign Up" label is clicked.
     * Linked to onMouseClicked in login.fxml.
     */
    @FXML
    void goToSignup(MouseEvent event) {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        
        // මෙතැනට try-catch එකතු කර ඇත
        try {
            // අපගේ නව NavigationUtil එකට ගැලපෙන පරිදි නම වෙනස් කර ඇත
            NavigationUtil.navigateTo("Signup.fxml");
        } catch (IOException e) {
            showError("Failed to load Signup page!");
            e.printStackTrace();
        }
    }

    /**
     * Helper method for consistent error messaging as per the 'user-friendly' requirement.
     */
    private void showError(String message) {
        lblError.setStyle("-fx-text-fill: red;");
        lblError.setText(message);
    }
}