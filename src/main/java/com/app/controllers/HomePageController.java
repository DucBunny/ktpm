package com.app.controllers;

import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePageController {
    public void changeToSignUp(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/sign-up.fxml",
                    "/styles/sign-in-sign-up.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-sign-up.css",
                event, false, false);
    }
}
