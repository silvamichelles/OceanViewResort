package com.oceanview.controller;

import com.oceanview.util.NavigationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;

public abstract class BaseController {

    /**
     * F1 - F12 කෙටිමං (Shortcuts) පද්ධතිය පුරා ක්‍රියාත්මක කිරීම.
     */
    protected void setupGlobalShortcuts(Node anyNodeOnScreen) {
        if (anyNodeOnScreen == null) return;

        Platform.runLater(() -> {
            if (anyNodeOnScreen.getScene() != null) {
                // පවතින Filter ඉවත් කර අලුතින් එකතු කිරීම (Duplicate වැළැක්වීමට)
                anyNodeOnScreen.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
                anyNodeOnScreen.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
            }
        });
    }

    private void handleGlobalKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case F1 -> { navigate("Dashboard.fxml"); event.consume(); }
            case F2 -> { navigate("Reservations.fxml"); event.consume(); }
            case F3 -> { navigate("AddReservation.fxml"); event.consume(); }
            case F4 -> { navigate("Guests.fxml"); event.consume(); }
            case F5 -> { navigate("Billing.fxml"); event.consume(); }
            case F6 -> { navigate("Rooms.fxml"); event.consume(); }
            case F11 -> { navigate("Help.fxml"); event.consume(); }
            case F12 -> { navigate("login.fxml"); event.consume(); }
            default -> {}
        }
    }

    // --- UI මෙනු බොත්තම් සඳහා (ActionEvents) ---
    @FXML public void showDashboard(ActionEvent event) { navigate("Dashboard.fxml"); }
    @FXML public void showReservations(ActionEvent event) { navigate("Reservations.fxml"); }
    @FXML public void showAddReservation(ActionEvent event) { navigate("AddReservation.fxml"); }
    @FXML public void showGuests(ActionEvent event) { navigate("Guests.fxml"); }
    @FXML public void showBilling(ActionEvent event) { navigate("Billing.fxml"); }
    @FXML public void showRooms(ActionEvent event) { navigate("Rooms.fxml"); }
    @FXML public void showHelp(ActionEvent event) { navigate("Help.fxml"); }
    @FXML public void handleLogout(ActionEvent event) { navigate("login.fxml"); }

    @FXML public void printReport(ActionEvent event) { System.out.println("Generating Report PDF..."); }

    /**
     * පිටු මාරු කිරීමේ ප්‍රධාන ක්‍රමය (Navigation Logic)
     */
    protected void navigate(String fxmlName) {
        try {
            // NavigationUtil එකට FXML නම පමණක් ලබා දීම ප්‍රමාණවත්ය
            NavigationUtil.navigateTo(fxmlName);
            
            // අවශ්‍ය නම් window size එක වෙනස් කිරීම මෙහිදී කළ හැක
            // උදාහරණයක් ලෙස Login පිටුව සඳහා Full Screen ඉවත් කිරීම
        } catch (IOException e) {
            System.err.println("දෝෂයකි: " + fxmlName + " පූරණය කළ නොහැක.");
            e.printStackTrace();
        }
    }
}