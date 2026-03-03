package com.oceanview.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationUtil {
    
    private static Stage primaryStage;

    // පද්ධතිය ආරම්භයේදී Stage එක මෙයට ලබා දිය යුතුය
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * සරලව FXML ගොනුවේ නම ලබා දීමෙන් පිටු මාරු කරයි.
     */
    public static void navigateTo(String fxml) throws IOException {
        if (primaryStage == null) {
            System.err.println("දෝෂයකි: Primary Stage එක NavigationUtil වෙත ලබා දී නැත!");
            return;
        }

        // FXML එක පූරණය කිරීම
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/com/oceanview/ui/" + fxml));
        Parent root = loader.load();

        // පවතින Window එකේම අලුත් Scene එක පෙන්වීම
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ocean View Resort - " + fxml.replace(".fxml", ""));
        primaryStage.show();
    }
}