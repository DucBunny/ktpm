package com.app.controllers;

import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HomePageController {
    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;

    @FXML
    private MenuItem MenuItem_SignUp;

    public void initialize(String role, String username) {
        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "cashier")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Thu ngân.");
        }

        nameLabel.setText("Xin chào, " + username);
    }

    //  Pop-up Cài đặt ---------------------------------------------------------
    public void changeToSignUp(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-account.fxml",
                    "/styles/sign-in-create-account.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false, false);
    }

    public void changeToRevenues(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/revenues.fxml", "/styles/revenues.css",
                event, false, true);
    }

    // Footer ------------------------------------------------------------------
    public void changeToCollectFees(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-payment.fxml",
                    "/styles/create-payment.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToCreateFees(ActionEvent event) throws Exception {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-revenues.fxml",
                    "/styles/create-revenues.css", false, owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
