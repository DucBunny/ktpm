package com.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CreateRevenuesController {
    @FXML
    private TextField nameRevenueField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField unitPriceField;

    @FXML
    private void handleCreateRevenues() {
        String nameRevenue = nameRevenueField.getText().trim();
        String description = descriptionField.getText().trim();
        String unitPrice = unitPriceField.getText().trim();

    }
}
