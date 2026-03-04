package com.oceanview.main;

import javafx.application.Application;
import javafx.stage.Stage;
import com.oceanview.util.NavigationUtil;
import com.oceanview.network.OceanViewServer;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // 1. Web Server එක Background Thread එකක Start කිරීම
        new Thread(() -> {
            try {
                OceanViewServer.main(new String[]{});
            } catch (Exception e) {
                System.err.println("Server Error: " + e.getMessage());
            }
        }).start();

        // 2. Stage එක NavigationUtil එකට ලබා දීම
        NavigationUtil.setStage(primaryStage);

        // 3. Login පිටුව Load කිරීම
        try {
            NavigationUtil.navigateTo("login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}