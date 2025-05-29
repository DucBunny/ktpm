package com.app.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomAlertController {
    @FXML
    private AnchorPane Pane_Alert;
    @FXML
    private Label titleSuccessLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;

    private Stage alertStage;
    private boolean autoClose = false;
    private int autoCloseSeconds = 0;
    private boolean isConfirm = false;
    private boolean userChoice = false; // true: Yes, false: No

    public void setAlertStage(Stage stage) {
        this.alertStage = stage;
    }

    public void setSuccessTitle(String title) {
        titleSuccessLabel.setText(title);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void enableAutoClose(int seconds) {
        this.autoClose = true;
        this.autoCloseSeconds = seconds;
    }

    public void setConfirmMode(boolean confirmMode) {
        this.isConfirm = confirmMode;
    }

    public void startAutoClose() {
        if (autoClose && !isConfirm) { // Không auto-close confirm
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(autoCloseSeconds), event -> {
                if (alertStage != null)
                    alertStage.close();
            }));
            timeline.setCycleCount(1);
            timeline.play();
        }
    }

    public boolean getUserChoice() {
        return userChoice;
    }

    @FXML
    private void initialize() {
        yesButton.setOnAction(e -> {
            userChoice = true;
            alertStage.close();
        });

        noButton.setOnAction(e -> {
            userChoice = false;
            alertStage.close();
        });
    }

    public void setupMode() {
        if (isConfirm) {
            titleSuccessLabel.setVisible(false);
            titleLabel.setVisible(true);
            messageLabel.setVisible(true);
            yesButton.setVisible(true);
            noButton.setVisible(true);
            Pane_Alert.setStyle("-fx-border-color: #586995; -fx-border-radius: 15; -fx-background-color: #FFFFFF; -fx-background-radius: 15");
        } else {
            titleSuccessLabel.setVisible(true);
            titleLabel.setVisible(false);
            messageLabel.setVisible(false);
            yesButton.setVisible(false);
            noButton.setVisible(false);
            Pane_Alert.setStyle("-fx-border-color: #71CC2E; -fx-border-radius: 15; -fx-background-color: #FFFFFF; -fx-background-radius: 15");
        }
    }
}

