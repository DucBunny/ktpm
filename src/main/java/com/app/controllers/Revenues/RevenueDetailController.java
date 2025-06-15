package com.app.controllers.Revenues;

import com.app.models.Revenues;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RevenueDetailController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextField quantityUnitField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField statusField;

    private static Revenues revenueDetail;

    public static void setRevenueDetail(Revenues revenue) {
        revenueDetail = revenue;
    }

    @FXML
    public void initialize() {
        if (revenueDetail == null) {
            return;
        }

        // Set dữ liệu
        nameField.setText(revenueDetail.getName());
        codeField.setText(revenueDetail.getCode());
        unitPriceField.setText(revenueDetail.getUnitPrice());
        quantityUnitField.setText(revenueDetail.getQuantityUnit());
        descriptionArea.setText(revenueDetail.getDescription());
        categoryField.setText(mapCategory(revenueDetail.getCategory()));
        statusField.setText(mapStatus(revenueDetail.getStatus()));
    }

    private String mapCategory(String category) {
        if (category == null)
            return "";
        return switch (category.toLowerCase()) {
            case "mandatory" -> "Bắt buộc";
            case "voluntary" -> "Tự nguyện";
            default -> category;
        };
    }

    private String mapStatus(String status) {
        if (status == null)
            return "";
        return switch (status.toLowerCase()) {
            case "active" -> "Mở";
            case "inactive" -> "Đóng";
            default -> status;
        };
    }
}