package com.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AlertController {
    @FXML
    private Label messageLabel;

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
