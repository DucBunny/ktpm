package com.app.controllers;

import com.app.models.Revenues;
import com.app.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Statement;

public class EditRevenueController {
    @FXML private TextField nameField;
    @FXML private TextField valueField;
    @FXML private TextArea descriptionArea;
    @FXML private MenuButton statusBox;
    @FXML private MenuButton categoryBox;

    private static Revenues revenueToEdit;

    public static void setRevenueToEdit(Revenues revenue) {
        revenueToEdit = revenue;
    }

    @FXML
    public void initialize() {
        // Khởi tạo lựa chọn cho status
        MenuItem openItem = new MenuItem("Mở");
        openItem.setStyle("-fx-font-size: 16px;");
        MenuItem closeItem = new MenuItem("Đóng");
        closeItem.setStyle("-fx-font-size: 16px;");

        openItem.setOnAction(e -> statusBox.setText("Mở"));
        closeItem.setOnAction(e -> statusBox.setText("Đóng"));

        statusBox.getItems().setAll(openItem, closeItem);

        // Khởi tạo lựa chọn cho category
        MenuItem requiredItem = new MenuItem("Bắt buộc");
        requiredItem.setStyle("-fx-font-size: 16px;");
        MenuItem optionalItem = new MenuItem("Tự nguyện");
        optionalItem.setStyle("-fx-font-size: 16px;");

        requiredItem.setOnAction(e -> categoryBox.setText("Bắt buộc"));
        optionalItem.setOnAction(e -> categoryBox.setText("Tự nguyện"));

        categoryBox.getItems().setAll(requiredItem, optionalItem);

        // Hiển thị dữ liệu nếu có
        if (revenueToEdit != null) {
            nameField.setText(revenueToEdit.getName());
            valueField.setText(revenueToEdit.getValue());
            descriptionArea.setText(revenueToEdit.getDescription());
            statusBox.setText(revenueToEdit.getStatus());
            categoryBox.setText(revenueToEdit.getCategory());
        }
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String name = nameField.getText();
            String value = valueField.getText();
            String description = descriptionArea.getText();
            String statusValue = statusBox.getText().equals("Mở") ? "active" : "inactive";
            String categoryValue = categoryBox.getText().equals("Bắt buộc") ? "mandatory" : "voluntary";

            String query = String.format(
                    "UPDATE revenue_items SET name='%s', unit_price='%s', description='%s', status='%s', category='%s' WHERE id=%d",
                    name, value, description, statusValue, categoryValue, revenueToEdit.getId()
            );

            stmt.executeUpdate(query);

            // Đóng cửa sổ
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
