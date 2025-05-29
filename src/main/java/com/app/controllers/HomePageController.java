package com.app.controllers;

import com.app.utils.SceneNavigator;
import com.app.utils.StageManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HomePageController {
    private String role;
    private String username;

    @FXML
    private Label roleLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private MenuItem MenuItem_SignUp;

    public void initialize(String role, String username) {
        this.role = role;
        this.username = username;

        if (Objects.equals(role, "admin")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Quản trị viên.");
            MenuItem_SignUp.setVisible(true);
        } else if (Objects.equals(role, "cashier")) {
            roleLabel.setText("Bạn đang đăng nhập với quyền Thu ngân.");
        }

        nameLabel.setText("Xin chào, " + username);
    }

    //    Header Buton ---------------------------------------------------------
    public void changeToRooms(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/rooms.fxml", "/styles/rooms.css",
                event, true);

        RoomsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToResidents(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/residents.fxml", "/styles/residents.css",
                event, true);

        ResidentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToRevenues(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/revenues.fxml", "/styles/revenues.css",
                event, true);

        RevenuesController controller = loader.getController();
        controller.initialize(role, username);
    }

    public void changeToPayments(Event event) throws Exception {
        FXMLLoader loader = SceneNavigator.switchScene("/fxml/payments.fxml", "/styles/payments.css",
                event, true);

        PaymentsController controller = loader.getController();
        controller.initialize(role, username);
    }

    //  Pop-up Button Cài đặt --------------------------------------------------
    public void changeToSignUp() {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-account.fxml",
                    "/styles/sign-in-create-account.css", owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToSignIn(ActionEvent event) throws Exception {
        SceneNavigator.switchScene("/fxml/sign-in.fxml", "/styles/sign-in-create-account.css",
                event, false);
    }

    // Footer Button -----------------------------------------------------------
    public void changeToCreatePayments() {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-payment.fxml",
                    "/styles/create-payment.css", owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToCreateRevenues() {
        try {
            Stage owner = StageManager.getPrimaryStage();
            SceneNavigator.showPopupScene("/fxml/create-revenues.fxml",
                    "/styles/create-revenue.css", owner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
