package com.app.controllers;

import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HomePageController {
    @FXML
    private Label roleLabel;

    public void setRoleLabel(String role) {
        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
        } else if (Objects.equals(role, "cashier")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Thu ngân.");
        }
    }

    public void changeToSignUp(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/sign-up.fxml",
                    "/styles/sign-in-sign-up.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToCollectFees(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/collect-fees.fxml",
                    "/styles/collect-fees.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-sign-up.css",
                event, false, false);
    }
}
