package com.app.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomAlertController {
    @FXML
    private AnchorPane Pane_Alert;

    // Success
    @FXML
    private HBox Pane_Success;
    @FXML
    private Label titleSuccessLabel;

    // Error
    @FXML
    private HBox Pane_Error;
    @FXML
    private Label titleErrorLabel;
    @FXML
    private Label btnClose;

    // Confirm
    @FXML
    private AnchorPane Pane_Confirm;
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
    private double autoCloseSeconds = 0;
    private boolean isConfirm = false;
    private boolean isSuccess = false;
    private boolean isError = false;
    private boolean userChoice = false; // true: Yes, false: No

    public void setAlertStage(Stage stage) {
        this.alertStage = stage;
    }

    public void setSuccessTitle(String title) {
        titleSuccessLabel.setText(title);
    }

    public void setErrorTitle(String title) {
        titleErrorLabel.setText(title);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void enableAutoClose(double seconds) {
        this.autoClose = true;
        this.autoCloseSeconds = seconds;
    }

    public void setConfirmMode(boolean confirmMode) {
        this.isConfirm = confirmMode;
    }

    public void setSuccessMode(boolean successMode) {
        this.isSuccess = successMode;
    }

    public void setErrorMode(boolean errorMode) {
        this.isError = errorMode;
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
        Pane_Confirm.setVisible(false);
        Pane_Success.setVisible(false);
        Pane_Error.setVisible(false);

        if (isConfirm) {
            Pane_Confirm.setVisible(true);
            Pane_Confirm.toFront();
            Pane_Alert.setStyle("-fx-border-color: #586995; -fx-border-radius: 15; -fx-background-color: #FFFFFF; -fx-background-radius: 15");
        } else if (isSuccess) {
            Pane_Success.setVisible(true);
            Pane_Success.toFront();
            Pane_Alert.setStyle("-fx-border-color: #71CC2E; -fx-border-radius: 15; -fx-background-color: #FFFFFF; -fx-background-radius: 15");
        } else if (isError) {
            Pane_Error.setVisible(true);
            Pane_Error.toFront();
            btnClose.toFront();
            Pane_Alert.setStyle("-fx-border-color: #FF4040; -fx-border-radius: 15; -fx-background-color: #FFFFFF; -fx-background-radius: 15");
            btnClose.setStyle("-fx-text-fill: #00000066;");
            btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-text-fill: #00000066;"));
            btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-text-fill: #fd5556;"));
            btnClose.setOnMouseClicked(e -> {
                Stage stage = (Stage) btnClose.getScene().getWindow();
                stage.close();
            });
        }
    }
}

