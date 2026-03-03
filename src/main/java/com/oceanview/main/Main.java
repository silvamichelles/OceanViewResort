package com.oceanview.main;

import javafx.application.Application;
import javafx.stage.Stage;
import com.oceanview.util.NavigationUtil;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // 1. අත්‍යවශ්‍යම පියවර: ප්‍රධාන Window එක (Stage) NavigationUtil එකට ලබා දීම.
        // මෙයින් අර Terminal එකේ ආපු Error එක සම්පූර්ණයෙන්ම නැතිවී යයි.
        NavigationUtil.setStage(primaryStage);

        // 2. අලුත් Navigation ක්‍රමයට පළමු පිටුව ලෙස Login පිටුව පෙන්වීම
        NavigationUtil.navigateTo("login.fxml");
        
        // Window එකේ ප්‍රමාණය වෙනස් කිරීම (Resize) නැවැත්වීම
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}